package com.chinacloud.isv.component;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.chinacloud.isv.configuration.Configuration;
import com.chinacloud.isv.domain.TaskResult;
import com.chinacloud.isv.entity.VMQeuryParam;
import com.chinacloud.isv.entity.callbackparams.Data;
import com.chinacloud.isv.entity.callbackparams.Process;
import com.chinacloud.isv.factory.WhiteholeFactory;
import com.chinacloud.isv.persistance.OrderRecordDao;
import com.chinacloud.isv.persistance.TaskResultDao;
import com.chinacloud.isv.persistance.TaskStackDao;
import com.chinacloud.isv.util.CaseProvider;
import com.chinacloud.isv.util.MSUtil;
import com.fasterxml.jackson.core.JsonProcessingException;

@Component
public class RebootEvent {

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
	
	private static final Logger logger = LogManager.getLogger(RebootEvent.class);
	private VMQeuryParam vp;
	
	public void addParameters(VMQeuryParam vp){
		this.vp = vp;
	}
	
	public void go(){
		Data data = new Data();
		Process process = new Process();
		String requestResponse = null;
		Map<String,String> map = new HashMap<String,String >();
		logger.debug("type:reboot case");
		logger.debug("------------------------");
		map.put("Content-Type", "application/json;charset=utf-8");
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		boolean b = virtualMachineStatusCheck.isAllInOneStatus(vp.getcFarmId(), vp.getxSecurityKey(), vp.getSpecialToken(), CaseProvider.EVENT_TYPE_SUBSCRIPTION_REBOOT+" ", vp.getTaskId(),CaseProvider.VM_STATUS_RUNNING);
		if(b){
			data.setSuccess(true);
			data.setMessage(MSUtil.getChineseName(CaseProvider.EVENT_TYPE_SUBSCRIPTION_REBOOT)+"处理成功。");
			process.setEventId(vp.getEnventId());
			process.setStatus(CaseProvider.SUCESS_STATUS);
			process.setInstanceId(vp.getInstanceId());
			data.setProcess(process);
			String result = null;
			try {
				result = WhiteholeFactory.getJsonString(data);
				logger.info("reboot case,call back return result msg:"+result);
			} catch (JsonProcessingException e) {
				logger.error("reboot case,farm id:"+vp.getcFarmId()+", waiting result,convert object to string failed");
				e.printStackTrace();
			}
			String newResult = MSUtil.encode(result);
			TaskResult taskResult = null;
			CloseableHttpResponse response = null;
			try {
				response = MSUtil.httpClientPostUrl(map, vp.getCallbackUrl(), newResult);
			} catch (Exception e) {
				taskResult = MSUtil.getResultInstance(vp.getTaskId(), CaseProvider.FAILED_STATUS, CaseProvider.HTTP_STATUS_POST, "call back return result failed,errorMsg:"+e.getLocalizedMessage(), vp.getcFarmId(), vp.getCallbackUrl(), result,vp.getDestinationFarmId());
				//delete the row record of task 
				riskStackDao.deleteTask(vp.getTaskId());
				taskResultDao.addResult(taskResult);
				vtrualMachineQuery.removeQueryTask(vp);
				e.printStackTrace();
			}
			try {
				requestResponse = EntityUtils.toString(response.getEntity());
				logger.info("reboot case,farm id:"+vp.getcFarmId()+",call back content:"+requestResponse);
			} catch (Exception e) {
				logger.error("reboot case,convert result entity to string failed");
				e.printStackTrace();
			} 
			taskResult = MSUtil.getResultInstance(vp.getTaskId(), CaseProvider.SUCESS_STATUS,CaseProvider.HTTP_STATUS_POST, requestResponse,"0", vp.getCallbackUrl(), result,vp.getDestinationFarmId());
			riskStackDao.deleteTask(vp.getTaskId());
			taskResultDao.addResult(taskResult);
			vtrualMachineQuery.removeQueryTask(vp);
		}else{
			if(vtrualMachineQuery.timeOutCheck(vp)){
				TaskResult taskResult = MSUtil.getResultInstance(vp.getTaskId(), CaseProvider.FAILED_STATUS, "", "Reboot Case,Farm Id="+vp.getcFarmId()+" TIME OUT,", vp.getcFarmId(), "", "",vp.getDestinationFarmId());
				riskStackDao.deleteTask(vp.getTaskId());
				taskResultDao.addResult(taskResult);
				vtrualMachineQuery.removeQueryTask(vp);
			}
		}
	}
}
