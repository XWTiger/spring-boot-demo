package com.chinacloud.isv.component;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.chinacloud.isv.configuration.Configuration;
import com.chinacloud.isv.domain.TaskResult;
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
public class ActiveEvent {
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
	
	private static final Logger logger = LogManager.getLogger(ActiveEvent.class);
	private VMQeuryParam vp;
	private Map<String,String> headerMap;
	String queryUrl;
	public void addParameters(VMQeuryParam vp,Map<String,String> headerMap,String queryUrl){
		this.vp = vp;
		this.headerMap = headerMap;
		this.queryUrl = queryUrl;
	}
	@Transactional
	public void go(){
		Data data = new Data();
		Process process = new Process();
		logger.debug("type: active case");
		logger.debug("------------------------------------");
		//get all servers status 
		CloseableHttpResponse qResult = null;
		try {
			qResult = MSUtil.httpClientGetUrl(headerMap, queryUrl);
			String queryR = EntityUtils.toString(qResult.getEntity());
			logger.debug("vm info:"+queryR);
			WhiteholeFactory wf = new WhiteholeFactory();
			Servers server = wf.getEntity(Servers.class,queryR);
			qResult.close();
			logger.debug("total :"+server.getTotal());
			if(Integer.parseInt(server.getTotal()) > 0){
				int count =0;
				for (ServerInfo si : server.getData()) {
					if(si.getStatus().equals("Running")){
						count++;
					}
				}
				logger.debug("Running number:"+count);
				if(count == Integer.parseInt(server.getTotal())){
					logger.info("all virtual machine are Running, farmid:"+vp.getcFarmId());
					data.setSuccess(true);
					data.setMessage(MSUtil.getChineseName(CaseProvider.EVENT_TYPE_SUBSCRIPTION_ACTIVE)+"处理成功。");
					process.setEventId(vp.getEnventId());
					process.setStatus(CaseProvider.SUCESS_STATUS);
					process.setInstanceId(vp.getInstanceId());
					data.setProcess(process);
					vtrualMachineQuery.removeQueryTask(vp);
				}else{
					logger.warn("active case,running vitrual machine error,we need "+server.getTotal()+" machines,but it just have "+count);
					if(vtrualMachineQuery.timeOutCheck(vp)){
						TaskResult taskResult = MSUtil.getResultInstance(vp.getTaskId(), CaseProvider.FAILED_STATUS, "", "Active Case,Farm Id"+vp.getcFarmId()+" TIME OUT","0", "", "",vp.getDestinationFarmId());
						riskStackDao.deleteTask(vp.getTaskId());
						taskResultDao.addResult(taskResult);
						vtrualMachineQuery.removeQueryTask(vp);
					}
					return;
				}
			}
			} catch (Exception e1) {
				logger.error("when active farm stack id:"+vp.getTaskId()+" falied,because of query virtual machine number error");
				e1.printStackTrace();
			}
			//call back return result 
			Map<String,String> map = new HashMap<String,String >();
			map.put("Content-Type", "application/json;charset=utf-8");
			TaskResult taskResult = null;
			String result = null;
			try {
				result = WhiteholeFactory.getJsonString(data);
			} catch (JsonProcessingException e1) {
				logger.error("convert to json failed\n"+e1.getLocalizedMessage());
				e1.printStackTrace();
			}
			try {
				String newResult = MSUtil.encode(result);
				CloseableHttpResponse response = MSUtil.httpClientPostUrl(map, vp.getCallbackUrl(), newResult);
				HttpEntity entity = response.getEntity();
				String comebackResult = EntityUtils.toString(entity);
				logger.info("response entity content--->"+comebackResult);
				taskResult = MSUtil.getResultInstance(vp.getTaskId(), CaseProvider.SUCESS_STATUS, CaseProvider.HTTP_STATUS_POST, comebackResult, "0", vp.getCallbackUrl(), result,vp.getDestinationFarmId());
				//delete the row record of task and the order case result
				riskStackDao.deleteTask(vp.getTaskId());
				taskResultDao.addResult(taskResult);
			} catch (Exception e) {
				taskResult = MSUtil.getResultInstance(vp.getTaskId(), CaseProvider.FAILED_STATUS, CaseProvider.HTTP_STATUS_POST, "call back return result failed:"+e.getMessage(), "0", vp.getCallbackUrl(), result,vp.getDestinationFarmId());
				//delete the row record of task 
				riskStackDao.deleteTask(vp.getTaskId());
				taskResultDao.addResult(taskResult);
				vtrualMachineQuery.removeQueryTask(vp);
				e.printStackTrace();
			}
	}

}
