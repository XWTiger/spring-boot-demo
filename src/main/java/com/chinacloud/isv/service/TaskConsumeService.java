package com.chinacloud.isv.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.chinacloud.isv.domain.TaskResult;
import com.chinacloud.isv.domain.TaskStack;
import com.chinacloud.isv.entity.Params;
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
							//request mir server
							try {
								result = mirFactory.orderService(210, taskStack);
								logger.info("call back params---->"+result);
								System.out.println("call back params---->"+result);
								Map<String,String> map = new HashMap<String,String >();
								map.put("Content-Type", "application/json");
								System.out.println(params.getData().getCallBackUrl());
								CloseableHttpResponse response = MSUtil.httpClientPostUrl(map, params.getData().getCallBackUrl(), result);
								HttpEntity entity = response.getEntity();
								InputStream iStream = entity.getContent();
								BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
								StringBuilder sb = new StringBuilder();   
						        String line = null;
						        while ((line = br.readLine()) != null) {   
					                sb.append(line);   
					            }   
								logger.info("response entity content--->"+sb.toString());
								System.out.println("response entity content--->"+sb.toString());
								response.close();
								taskResult.setResultStatus("SUCCESS");
							} catch (Exception e) {
								logger.info("Consume task error\n"+e.getMessage());
								taskResult.setResultStatus("FAILED");
								taskResult.setErrorInfo(e.getLocalizedMessage());
								e.printStackTrace();
							}
							//add result
							taskResult.setId(taskStack.getId());
							taskResult.setcFarmId(201);
							taskResult.setRequestMethod(taskStack.getRequestMethod());
							taskResult.setParams(result);
							taskResult.setRequestUrl(taskStack.getCallBackUrl());
							//delete the row record of task 
							taskResultDao.addResult(taskResult);
							
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
