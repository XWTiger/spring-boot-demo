package com.chinacloud.isv.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chinacloud.isv.domain.TaskStack;
import com.chinacloud.isv.entity.Params;
import com.chinacloud.isv.factory.MirFactory;
import com.chinacloud.isv.factory.WhiteholeFactory;
import com.chinacloud.isv.persistance.TaskStackDao;
import com.chinacloud.isv.util.MSUtil;

@Service
public class MirRequestService {
	private static final Logger logger = LogManager.getLogger(MirRequestService.class);
	
	@Autowired
	private TaskStackDao riskStackDao;
	
	
	public String  sendRequest(String url){
		String message = null;
		//do login or get x-request-token
		if(MSUtil.isTestParameter(url)){
			return "{\"code\":200, \"msg\" : \"success\"}";
		}
		
		if(!MSUtil.httpUrlCheck(url)){
			return "{\"code\":500, \"msg\" : \"fail\"}";
		}
		//1. request the parameters
		if(null == url || "".equals(url)){
			throw new IllegalArgumentException("url error");
		}
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);
		try {
			CloseableHttpResponse response = httpclient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			InputStream iStream = entity.getContent();
			if(null == iStream){
				logger.error("get params failed from server that use the service broker");
				throw new IllegalArgumentException("获取参数失败");
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
			StringBuilder sb = new StringBuilder();   
	        String line = null;
	        while ((line = br.readLine()) != null) {   
                sb.append(line);   
            }   
			System.out.println("response entity content--->"+sb.toString());
			WhiteholeFactory wf = new WhiteholeFactory();
			Params params = wf.getEntity(Params.class,sb.toString());
			System.out.println("entity --->"+params.getData().getCallBackUrl());
			System.out.println("type --->"+params.getData().getType());
			//2. convert parameters to risk entity
			//is synchronization?
			boolean isSyn = MSUtil.isSynchronzation(params.getData().getType());
			logger.info("isSyn=====>"+isSyn);
			if(isSyn){
				//syn this is case query
				logger.info("--------- syn-----------");
				MirFactory mFactory = new MirFactory();
				String result = mFactory.queryService(params);
				logger.info(result);
				logger.info("------------------------");
				return result;
			}else{
				//not syn
				String uuid = UUID.randomUUID().toString();
				TaskStack riskStack = new TaskStack();
				riskStack.setCallBackUrl(params.getData().getCallBackUrl());
				riskStack.setId(uuid);
				riskStack.setParams(sb.toString());
				int farmId = WhiteholeFactory.getFarmId(params);
				riskStack.setFarmId(farmId);
				riskStack.setLock(0);
				//it is useful to add a analyzation method to decide witch request method to use
				riskStack.setRequestMethod("post");
				riskStack.setRequestUrl(url);
				//3. add risk entity to risk table 
				riskStackDao.addTask(riskStack);
				
				// return result json
				System.out.println("envent type---------"+params.getData().getType()+"--------");
				message = WhiteholeFactory.getAsynReturnJson(params.getData().getEventId(), params.getData().getType());
				
			}
			//distinguish case
			
			response.close();
		} catch (ClientProtocolException e) {
			logger.error("httpclient ClientProtocolException: \n"+e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("httpclient IOException: \n"+e.getMessage());
		}catch (Exception e) {
			logger.error("httpclient error: \n"+e.getMessage());
			e.printStackTrace();
		}
		return message;
	}
	
	
}
