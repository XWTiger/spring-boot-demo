package com.chinacloud.isv.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.chinacloud.isv.component.VtrualMachineQuery;
import com.chinacloud.isv.domain.TaskResult;
import com.chinacloud.isv.domain.TaskStack;
import com.chinacloud.isv.entity.Params;
import com.chinacloud.isv.entity.VMQeuryParam;
import com.chinacloud.isv.factory.MirFactory;
import com.chinacloud.isv.factory.WhiteholeFactory;
import com.chinacloud.isv.persistance.TaskResultDao;
import com.chinacloud.isv.persistance.TaskStackDao;
import com.chinacloud.isv.util.CaseProvider;
import com.chinacloud.isv.util.MSUtil;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

@Service
public class TaskConsumeService {
	private static final Logger logger = LogManager.getLogger(TaskConsumeService.class);
	@Autowired
	TaskStackDao riskStackDao;
	@Autowired
	TaskResultDao taskResultDao;
	@Autowired
	MirFactory mirFactory;
	@Autowired
	VtrualMachineQuery vtrualMachineQuery;
	
	@Scheduled(fixedRate = 1000)
	public void riskRunning(){
		
		
		ArrayList<TaskStack> RiskList = riskStackDao.getTasks();
		// lock task item
		
		//consume task and insert result
		for (TaskStack taskStack : RiskList) {
		
			//lock
			Integer status = riskStackDao.lockTask(taskStack.getId());
			if(null != status && 1 == status ){
				//consume
				WhiteholeFactory whiteholeFactory = new WhiteholeFactory();
				try {
					Params params = whiteholeFactory.getEntity(Params.class, taskStack.getParams());
					TaskResult taskResult = new TaskResult();
					
					String result = null;
					switch (params.getData().getType()) {
						case CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER:{
							VMQeuryParam vParam = new VMQeuryParam();
							//request mir server
							try {
								result = mirFactory.orderService(taskStack.getFarmId(), taskStack,vParam);
								if(result.contains("Farm successfully launched")){//TODO maybe update.
									vParam.setCallbackUrl(params.getData().getCallBackUrl());
									vtrualMachineQuery.addQueryTask(vParam);
									vtrualMachineQuery.start();
								}else{// call back return result
									logger.info("order case, call back params---->"+result);
									Map<String,String> map = new HashMap<String,String >();
									map.put("Content-Type", "application/json");
									System.out.println(params.getData().getCallBackUrl());
									String newResult = MSUtil.encode(result);
									CloseableHttpResponse response = MSUtil.httpClientPostUrl(map, params.getData().getCallBackUrl(), newResult);
									HttpEntity entity = response.getEntity();
									logger.info("response entity content--->"+EntityUtils.toString(entity));
									response.close();
									taskResult = MSUtil.getTaskResult(1, taskStack, result, EntityUtils.toString(entity));
									//delete the row record of task 
									riskStackDao.deleteTask(taskStack.getId());
									taskResultDao.addResult(taskResult);
								}
							} catch (Exception e) {
								logger.error("Order case,Consume task error\n"+e.getLocalizedMessage());
								//unlock the task
								riskStackDao.unLockTask(taskStack.getId());
								e.printStackTrace();
							}
							break;
						}
						case CaseProvider.EVENT_TYPE_SUBSCRIPTION_CANCEL:{
							String instanceId = params.getData().getPayload().getInstance().getInstanceId();
							VMQeuryParam vmQeuryParam = new VMQeuryParam();
							CloseableHttpResponse response = null;
							logger.debug("CANCEL　CASE: the instance id---->"+instanceId);
							TaskResult tr = taskResultDao.getOrderTaskResultById(instanceId);
							if(null == tr){
								logger.error("when do cancle case,get clone farm id failed because of database return null");
							}
							result = mirFactory.cancleService(params,tr.getcFarmId(),vmQeuryParam,taskStack);
							logger.info("cancle case, call back params---->"+result);
							if(result.contains("SSH key(s) successfully removed")){//TODO maybe update.
								vmQeuryParam.setcFarmId(tr.getcFarmId());
								vmQeuryParam.setType(1);
								vmQeuryParam.setEnventId(params.getData().getEventId());
								vmQeuryParam.setCallbackUrl(params.getData().getCallBackUrl());
								vmQeuryParam.setTaskId(taskStack.getId());
								vmQeuryParam.setBeginTime(new Date().getTime());
								vmQeuryParam.setInstanceId(instanceId);
								vtrualMachineQuery.addQueryTask(vmQeuryParam);
								vtrualMachineQuery.start();
							}else{
								Map<String,String> map = new HashMap<String,String >();
								map.put("Content-Type", "application/json");
								System.out.println(params.getData().getCallBackUrl());
								try {
								String newResult = MSUtil.encode(result);
								response = MSUtil.httpClientPostUrl(map, params.getData().getCallBackUrl(), newResult);
								HttpEntity entity = response.getEntity();
								String comebackResult = EntityUtils.toString(entity);
								logger.info("response entity content--->"+comebackResult);
								//add result
								taskResult = MSUtil.getTaskResult(0, taskStack, result,comebackResult);
								//delete the row record of task and the order case result
								riskStackDao.deleteTask(taskStack.getId());
								taskResultDao.deleteResultById(instanceId);
								taskResultDao.addResult(taskResult);
								} catch (Exception e) {
									logger.error("Consume task error\n"+e.getMessage());
									//add result
									taskResult = MSUtil.getTaskResult(0, taskStack, result,"call back response cancle case success result faild ,error info:"+e.getLocalizedMessage());
									//delete the row record of task 
									riskStackDao.deleteTask(taskStack.getId());
									taskResultDao.addResult(taskResult);
									e.printStackTrace();
								}
								response.close();
							}
							break;
						}
						case CaseProvider.EVENT_TYPE_SUBSCRIPTION_SUSPEND:{
							String instanceId = params.getData().getPayload().getInstance().getInstanceId();
							logger.debug("SUSPEND　CASE: the instance id---->"+instanceId);
							TaskResult tr = taskResultDao.getOrderTaskResultById(instanceId);
							if(null == tr){
								logger.error("when do suspend case,get cloned farm id failed because of database return null");
								String  suspendResult = WhiteholeFactory.getFailedMsg(params, "处理失败,原因是克隆的应用堆栈已经被删除。", CaseProvider.EVENT_TYPE_SUBSCRIPTION_SUSPEND);
								CloseableHttpResponse response  = WhiteholeFactory.callBackReturnResult(suspendResult, params);
								HttpEntity entity = response.getEntity();
								String comebackResult = EntityUtils.toString(entity);
								logger.info("response entity content--->"+comebackResult);
								taskResult = MSUtil.getTaskResult(0, taskStack, suspendResult, comebackResult);
								riskStackDao.deleteTask(taskStack.getId());
								taskResultDao.addResult(taskResult);
								break;
							}
							String suspendResult = mirFactory.suspendService(params,tr.getcFarmId(),taskStack);
							if(null == suspendResult){
								logger.error("suspend case, suspend service return null");
								break;
							}
							CloseableHttpResponse response = WhiteholeFactory.callBackReturnResult(suspendResult, params);
							if(null == response){
								logger.error("suspend case, call back return result failed");
							}
							HttpEntity entity = response.getEntity();
							String comebackResult = EntityUtils.toString(entity);
							logger.info("response entity content--->"+comebackResult);
							//add result
							taskResult = MSUtil.getTaskResult(1, taskStack, suspendResult, comebackResult);
							riskStackDao.deleteTask(taskStack.getId());
							taskResultDao.addResult(taskResult);
							response.close();
							break;
						}
						case CaseProvider.EVENT_TYPE_SUBSCRIPTION_QUERY:{
							break;
						}
						case CaseProvider.EVENT_TYPE_SUBSCRIPTION_ACTIVE:{
							String instanceId = params.getData().getPayload().getInstance().getInstanceId();
							VMQeuryParam vmQeuryParam = new VMQeuryParam();
							logger.debug("ACTIVE VITRUAL MACHINE　CASE: the instance id---->"+instanceId);
							TaskResult tr = taskResultDao.getOrderTaskResultById(instanceId);
							if(null == tr){
								logger.error("when do active virtual machine case,get clone farm id failed because of database return null");
							}
							result = mirFactory.activeService(params,tr.getcFarmId(),vmQeuryParam,taskStack);
							if(null == result){
								break;
							}
							if(result.equals(CaseProvider.ACTIVE_FIRST_STEP)){
								vmQeuryParam.setCallbackUrl(taskStack.getCallBackUrl());
								vmQeuryParam.setcFarmId(tr.getcFarmId());
								vmQeuryParam.setEnventId(params.getData().getEventId());
								vmQeuryParam.setTaskId(taskStack.getId());
								vmQeuryParam.setType(2);
								vmQeuryParam.setInstanceId(instanceId);
								vmQeuryParam.setBeginTime(new Date().getTime());
								vtrualMachineQuery.addQueryTask(vmQeuryParam);
								vtrualMachineQuery.start();
							}
							break;
						}
						default:{
							logger.warn("case type don't exist");
							throw new IllegalArgumentException("case type don't exist");
						}
					}
				} catch (JsonParseException e) {
					logger.error("when consume task ,json JsonParseException\n"+e.getMessage());
					e.printStackTrace();
				} catch (JsonMappingException e) {
					logger.error("when consume task ,json JsonMappingException\n"+e.getMessage());
					e.printStackTrace();
				} catch (IOException e) {
					logger.error("when consume task ,json IOException\n"+e.getMessage());
					e.printStackTrace();
				}
				
			}
		}
	}

}
