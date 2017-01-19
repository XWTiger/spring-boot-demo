package com.chinacloud.isv.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
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
import com.chinacloud.isv.entity.callbackparams.Attribute;
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
public class LaunchEvent {
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

	private static final Logger logger = LogManager.getLogger(LaunchEvent.class);
	private VMQeuryParam vp;
	private Map<String, String> headerMap;
	private String queryUrl;
	
	public void addParameters(VMQeuryParam vp, Map<String, String> headerMap, String queryUrl) {
		this.vp = vp;
		this.headerMap = headerMap;
		this.queryUrl = queryUrl;
	}
	@Transactional
	public void go(){
		Data data = new Data();
		Process process = new Process();
		//wait for launch farm stack
		logger.debug("type: launch case");
		logger.debug("------------------------------------");
		try {
			CloseableHttpResponse qResult = MSUtil.httpClientGetUrl(headerMap, queryUrl);
			String queryR = EntityUtils.toString(qResult.getEntity());
			WhiteholeFactory wf = new WhiteholeFactory();
			Servers server = wf.getEntity(Servers.class,queryR);
			ArrayList<ServerInfo> sList = server.getData();
			//do analyze
			int total = Integer.parseInt(server.getTotal());
			logger.debug("total : "+total+", virtual machine number :"+vp.getTotalInstance());
			boolean isR = virtualMachineStatusCheck.isAllInOneStatus(vp.getcFarmId(), vp.getxSecurityKey(), vp.getSpecialToken(), CaseProvider.EVENT_TYPE_SUBSCRIPTION_LAUNCH+" ", vp.getTaskId(),CaseProvider.VM_STATUS_RUNNING);
			if(!isR){
				//check time 
				if(vtrualMachineQuery.timeOutCheck(vp)){
					TaskResult taskResult = MSUtil.getResultInstance(vp.getTaskId(), CaseProvider.FAILED_STATUS, "", "Order Case,Farm Id"+vp.getcFarmId()+" TIME OUT", vp.getcFarmId(), "", "",vp.getDestinationFarmId(),vp.getEventType());
					riskStackDao.deleteTask(vp.getTaskId());
					taskResultDao.addResult(taskResult);
					vtrualMachineQuery.removeQueryTask(vp);
				}
				return;
			}
			if(total >= vp.getTotalInstance()){
				if(total == vp.getTotalInstance()){
				
					data.setSuccess(true);
					data.setMessage(MSUtil.getChineseName(CaseProvider.EVENT_TYPE_SUBSCRIPTION_LAUNCH)+"处理成功");
					process.setEventId(vp.getEnventId());
					process.setStatus(CaseProvider.SUCESS_STATUS);
					process.setInstanceId(vp.getTaskId());
					ArrayList<Attribute> att_list = new ArrayList<Attribute>();
					for (ServerInfo serverInfo : sList) {
						Attribute att = new Attribute();
						att.setKey("role_name");
						att.setValue(serverInfo.getRole_alias());
						Attribute att2 = new Attribute();
						att2.setKey("flavor");
						att2.setValue(serverInfo.getFlavor());
						Attribute att3 = new Attribute();
						att3.setKey("farm_id");
						att3.setValue(serverInfo.getFarm_id());
						Attribute att4 = new Attribute();
						att4.setKey("local_ip");
						att4.setValue(serverInfo.getLocal_ip());
						Attribute att5 = new Attribute();
						att5.setKey("remote_ip");
						att5.setValue(serverInfo.getRemote_ip());
						att_list.add(att3);
						att_list.add(att);
						att_list.add(att2);
						att_list.add(att4);
						att_list.add(att5);
					}
					process.setAttribute(att_list);
					data.setProcess(process);
					//delete query task
					vtrualMachineQuery.removeQueryTask(vp);
				}else{//the server number error
					//check time 
					boolean b = vtrualMachineQuery.timeOutCheck(vp);
					if(b){
						TaskResult taskResult = MSUtil.getResultInstance(vp.getTaskId(), CaseProvider.FAILED_STATUS, "", "Launch Case,Farm Id="+vp.getcFarmId()+" TIME OUT", vp.getcFarmId(), "", "",vp.getDestinationFarmId(),vp.getEventType());
						riskStackDao.deleteTask(vp.getTaskId());
						taskResultDao.addResult(taskResult);
						vtrualMachineQuery.removeQueryTask(vp);
					}
				}
			}else{// server number less than total
				boolean b = vtrualMachineQuery.timeOutCheck(vp);
				if(b){
					TaskResult taskResult = MSUtil.getResultInstance(vp.getTaskId(), CaseProvider.FAILED_STATUS, "", "Launch Case,Farm Id="+vp.getcFarmId()+" TIME OUT", vp.getcFarmId(), "", "",vp.getDestinationFarmId(),vp.getEventType());
					riskStackDao.deleteTask(vp.getTaskId());
					taskResultDao.addResult(taskResult);
					vtrualMachineQuery.removeQueryTask(vp);
				}
			}
			qResult.close();
		} catch (Exception e) {
			logger.error("launch case,get Server list error\n"+e.getLocalizedMessage());
			boolean b = vtrualMachineQuery.timeOutCheck(vp);
			if(b){
				TaskResult taskResult = MSUtil.getResultInstance(vp.getTaskId(), CaseProvider.FAILED_STATUS, "", "Launch Case,Farm Id"+vp.getcFarmId()+" TIME OUT", vp.getcFarmId(), "", "",vp.getDestinationFarmId(),vp.getEventType());
				riskStackDao.deleteTask(vp.getTaskId());
				taskResultDao.addResult(taskResult);
				vtrualMachineQuery.removeQueryTask(vp);
			}
			e.printStackTrace();
		}
		try {
			String result = null;
			if(null != data.getProcess()){
				Map<String,String> map = new HashMap<String,String >();
				map.put("Content-Type", "application/json;charset=utf-8");
				result = WhiteholeFactory.getJsonString(data);
				logger.info("call back return result: "+ result);
				String newResult = MSUtil.encode(result);
				logger.info("call back return result: "+ result);
				CloseableHttpResponse callbackResponse = null;
				try {
					callbackResponse = MSUtil.httpClientPostUrl(map,vp.getCallbackUrl(), newResult);
				} catch (Exception e) {
					logger.error("launch case,response order result failed");
					TaskResult taskResult = MSUtil.getResultInstance(vp.getTaskId(), CaseProvider.FAILED_STATUS, CaseProvider.HTTP_STATUS_POST, "", vp.getcFarmId(), vp.getCallbackUrl(), e.getLocalizedMessage(),vp.getDestinationFarmId(),vp.getEventType());
					//delete the row record of task 
					riskStackDao.deleteTask(vp.getTaskId());
					taskResultDao.addResult(taskResult);
					vtrualMachineQuery.removeQueryTask(vp);
					e.printStackTrace();
				}
				HttpEntity entity = callbackResponse.getEntity();
				String respCall = null;
				try {
					respCall = EntityUtils.toString(entity);
					logger.info("response entity content--->"+respCall);
				} catch (ParseException e) {
					logger.error("after call back return result,get resopnse error\n"+e.getLocalizedMessage());
					e.printStackTrace();
				} catch (IOException e) {
					logger.error("after call back return result,get resopnse error\n"+e.getLocalizedMessage());
					e.printStackTrace();
				}
				TaskResult taskResult = MSUtil.getResultInstance(vp.getTaskId(), CaseProvider.SUCESS_STATUS, CaseProvider.HTTP_STATUS_POST, respCall, "0", vp.getCallbackUrl(), result,vp.getDestinationFarmId(),vp.getEventType());
				//delete the row record of task 
				riskStackDao.deleteTask(vp.getTaskId());
				taskResultDao.addResult(taskResult);
				try {
					callbackResponse.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (JsonProcessingException e) {
			logger.error("convert result to json failed");
			e.printStackTrace();
		}
	
	
	}
}
