package com.chinacloud.isv.service;

import java.io.IOException;
import java.util.ArrayList;
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
								}else{
									logger.info("call back params---->"+result);
									Map<String,String> map = new HashMap<String,String >();
									map.put("Content-Type", "application/json");
									System.out.println(params.getData().getCallBackUrl());
									CloseableHttpResponse response = MSUtil.httpClientPostUrl(map, params.getData().getCallBackUrl(), result);
									HttpEntity entity = response.getEntity();
									logger.info("response entity content--->"+EntityUtils.toString(entity));
									response.close();
									taskResult.setResultStatus("SUCCESS");
									//add result
									taskResult.setId(taskStack.getId());
									taskResult.setRequestMethod(taskStack.getRequestMethod());
									taskResult.setParams(result);
									taskResult.setRequestUrl(taskStack.getCallBackUrl());
									//TODO delete the row record of task 
									taskResultDao.addResult(taskResult);
								}
								/*ObjectMapper mapper = new ObjectMapper();
								JsonNode node = mapper.readTree(result);
								JsonNode rNode = node.get("process");
								rNode = rNode.get("attribute");
								if(rNode.isArray()){
									for (JsonNode jsonNode : rNode) {
										if(jsonNode.get("key").toString().equals("farmId")){
											cfarmId = jsonNode.get("value").asInt();
										};
									}
								}*/
							} catch (Exception e) {
								logger.error("Consume task error\n"+e.getMessage());
								taskResult.setResultStatus("FAILED");
								taskResult.setErrorInfo(e.getLocalizedMessage());
								//add result
								taskResult.setId(taskStack.getId());
								taskResult.setRequestMethod(taskStack.getRequestMethod());
								taskResult.setParams(result);
								taskResult.setRequestUrl(taskStack.getCallBackUrl());
								//delete the row record of task 
								taskResultDao.addResult(taskResult);
								e.printStackTrace();
							}
							break;
						}
						case CaseProvider.EVENT_TYPE_SUBSCRIPTION_CANCEL:{
							mirFactory.cancleService(210);
							break;
						}
						case CaseProvider.EVENT_TYPE_SUBSCRIPTION_SUSPEND:{
							mirFactory.suspendService();
							break;
						}
						case CaseProvider.EVENT_TYPE_SUBSCRIPTION_QUERY:{
							mirFactory.queryService();
							break;
						}
						case CaseProvider.EVENT_TYPE_SUBSCRIPTION_ACTIVE:{
							mirFactory.activeService();
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
