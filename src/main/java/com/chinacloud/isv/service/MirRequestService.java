package com.chinacloud.isv.service;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Service;

@Service
public class MirRequestService {

	CloseableHttpClient httpclient = HttpClients.createDefault();
	HttpPost httpPost = new HttpPost("http://targethost/login");
	
}
