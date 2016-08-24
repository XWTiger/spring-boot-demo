package com.chinacloud.isv.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.chinacloud.isv.configuration.Configuration;
import com.chinacloud.isv.domain.TaskResult;
import com.chinacloud.isv.entity.ResultObject;
import com.chinacloud.isv.entity.VMQeuryParam;
import com.chinacloud.isv.entity.callbackparams.Data;
import com.chinacloud.isv.entity.callbackparams.Process;
import com.chinacloud.isv.entity.mir.ServerInfo;
import com.chinacloud.isv.entity.mir.Servers;
import com.chinacloud.isv.factory.WhiteholeFactory;
import com.chinacloud.isv.persistance.OrderRecordDao;
import com.chinacloud.isv.persistance.TaskResultDao;
import com.chinacloud.isv.persistance.TaskStackDao;
import com.chinacloud.isv.util.CaseProvider;
import com.chinacloud.isv.util.MSUtil;
import com.fasterxml.jackson.core.JsonProcessingException;

@Component
public class CancelEvent {

	@Autowired
	TaskStackDao riskStackDao;
	@Autowired
	TaskResultDao taskResultDao;
	@Autowired
	Configuration configuration;
	@Autowired
	OrderRecordDao orderRecordDao;
	@Autowired
	VirtualMachineStatusCheck virtualMachineStatusCheck;
	@Autowired
	VtrualMachineQuery vtrualMachineQuery;

	private static final Logger logger = LogManager.getLogger(CancelEvent.class);
	private VMQeuryParam vp;
	private Map<String, String> headerMap;
	private String queryUrl;

	public void addParameters(VMQeuryParam vp, Map<String, String> headerMap, String queryUrl) {
		this.vp = vp;
		this.headerMap = headerMap;
		this.queryUrl = queryUrl;
	}
	@Transactional
	public void go() {
		Data data = new Data();
		Process process = new Process();
		logger.debug("type: cancel case");
		logger.debug("------------------------------------");
		// wait for remove farm stack mission
		String result = null;
		String removeFarmUrl = configuration.getMirBaseUrl() + "/mir/proxy/farms/xRemove";
		// query if all virtual machine is terminated
		CloseableHttpResponse qResult;
		try {
			qResult = MSUtil.httpClientGetUrl(headerMap, queryUrl);
			String queryR = EntityUtils.toString(qResult.getEntity());
			logger.debug("vm info:" + queryR);
			WhiteholeFactory wf = new WhiteholeFactory();
			Servers server = wf.getEntity(Servers.class, queryR);
			qResult.close();
			logger.debug("total :" + server.getTotal());
			if (Integer.parseInt(server.getTotal()) > 0) {
				int count = 0;
				for (ServerInfo si : server.getData()) {
					if (si.getStatus().equals("Terminated")) {
						count++;
					}
				}
				logger.debug("Terminated number:" + count);
				if (count == Integer.parseInt(server.getTotal())) {
					logger.info("all virtual machine Terminated farmid:" + vp.getcFarmId());
				} else {
					if (vtrualMachineQuery.timeOutCheck(vp)) {
						TaskResult taskResult = MSUtil.getResultInstance(vp.getTaskId(), CaseProvider.FAILED_STATUS, "",
								"Cancle Case,Farm Id" + vp.getcFarmId() + " TIME OUT", "0", "", "",
								vp.getDestinationFarmId(),vp.getEventType());
						riskStackDao.deleteTask(vp.getTaskId());
						taskResultDao.addResult(taskResult);
						vtrualMachineQuery.removeQueryTask(vp);
					}
					return;
				}
			}
		} catch (Exception e1) {
			logger.error("when cancle farm stack id:" + vp.getTaskId()
					+ " falied,because of query virtual machine number error");
			e1.printStackTrace();
		}
		// close farm stack
		try {
			List<NameValuePair> params_list = new ArrayList<NameValuePair>();
			params_list.add(new BasicNameValuePair("farmId", String.valueOf(vp.getcFarmId())));
			CloseableHttpResponse response = MSUtil.httpClientPostUrl(headerMap, removeFarmUrl, params_list);
			String requestR = EntityUtils.toString(response.getEntity());
			ResultObject ro = new WhiteholeFactory().getEntity(ResultObject.class, requestR);
			if (!ro.isSuccess()) {
				logger.error("remove farm failed");
				data.setSuccess(false);
				data.setErrorCode(CaseProvider.ERROR_CODE);
				data.setMessage(
						MSUtil.getChineseName(CaseProvider.EVENT_TYPE_SUBSCRIPTION_CANCEL) + "处理失败,原因是删除应用堆栈失败。");
				process.setEventId(vp.getEnventId());
				process.setStatus(CaseProvider.FAILED_STATUS);
				data.setProcess(process);
			}
			response.close();
		} catch (Exception e) {
			logger.error("order case,remove farm failed\n" + e.getLocalizedMessage());
			e.printStackTrace();
		}
		// return success result
		if (null == data.getErrorCode() || data.getErrorCode().equals("")) {
			logger.info("===========package success info of cancle case==========");
			data.setSuccess(true);
			data.setMessage(MSUtil.getChineseName(CaseProvider.EVENT_TYPE_SUBSCRIPTION_CANCEL) + "处理成功。");
			process.setEventId(vp.getEnventId());
			process.setStatus("SUCCESS");
			process.setInstanceId(vp.getInstanceId());
			data.setProcess(process);
		}

		try {
			result = WhiteholeFactory.getJsonString(data);
			logger.debug("when cancle case,callback return result:" + result);
		} catch (JsonProcessingException e) {
			logger.error("convert to json failed\n" + e.getLocalizedMessage());
			e.printStackTrace();
		}
		// call back return result
		TaskResult taskResult = null;
		Map<String, String> map = new HashMap<String, String>();
		map.put("Content-Type", "application/json;charset=utf-8");
		try {
			String newResult = MSUtil.encode(result);
			CloseableHttpResponse response = MSUtil.httpClientPostUrl(map, vp.getCallbackUrl(), newResult);
			HttpEntity entity = response.getEntity();
			String comebackResult = EntityUtils.toString(entity);
			logger.info("response entity content--->" + comebackResult);
			taskResult = MSUtil.getResultInstance(vp.getTaskId(), "SUCCESS", CaseProvider.HTTP_STATUS_POST,
					comebackResult, "0", vp.getCallbackUrl(), result, vp.getDestinationFarmId(),vp.getEventType());
			// delete the row record of task and the order case result
			riskStackDao.deleteTask(vp.getTaskId());
			// delete order case result
			logger.info("cancle case,delete order case result raw,instance id:" + vp.getInstanceId(),"farm id :"+vp.getcFarmId());
			orderRecordDao.deleteByCloneFarmId(vp.getcFarmId());
			taskResultDao.deleteResultById(vp.getInstanceId());
			taskResultDao.addResult(taskResult);
			vtrualMachineQuery.removeQueryTask(vp);
		} catch (Exception e) {
			taskResult = MSUtil.getResultInstance(vp.getTaskId(), CaseProvider.FAILED_STATUS,
					CaseProvider.HTTP_STATUS_POST, "call back return result failed." + e.getLocalizedMessage(), "0",
					vp.getCallbackUrl(), "", vp.getDestinationFarmId(),vp.getEventType());
			// delete the row record of task
			riskStackDao.deleteTask(vp.getTaskId());
			taskResultDao.addResult(taskResult);
			e.printStackTrace();
		}
	}
}
