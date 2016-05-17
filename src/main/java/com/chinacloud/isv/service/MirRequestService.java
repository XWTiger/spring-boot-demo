package com.chinacloud.isv.service;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Service;

@Service
public class MirRequestService {

	public void requestParams(String url){
		//1. request the parameters
		if(null == url || "".equals(url)){
			throw new IllegalArgumentException("url error");
		}
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);
		
		//2. convert parameters to risk entity
		
		//3. add risk entity to risk table 
		
	}
	
	
}
