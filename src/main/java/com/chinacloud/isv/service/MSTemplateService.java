package com.chinacloud.isv.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chinacloud.isv.configuration.Configuration;
import com.chinacloud.isv.entity.ResultObject;
import com.chinacloud.isv.util.MSUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MSTemplateService {

	@Autowired
	Configuration configuration;
	@Autowired
	LoginService loginService;
	
	private static final Logger logger = LogManager.getLogger(MSTemplateService.class);
	
	public boolean getServiceStatus(String serviceTemplateId){
		boolean b = true;
		//call the interface and get status
		logger.info("serviceTemplateId :"+serviceTemplateId);
		logger.debug("template url:"+configuration.getServiceTemplateUrl());
		ResultObject robj= loginService.login(null, null);
		if(!robj.getErrorMessage().equals("") && !robj.isSuccess()){
			logger.error("login mir plateform failed");
			b = false;
			return b; 
		}
		Map<String,String> map = new HashMap<String,String >();
		map.put("Content-Type", "application/json");
		map.put("X-Secure-Key", robj.getSecureKey());
		map.put("X-Requested-Token", robj.getSpecialToken());
		String url = configuration.getServiceTemplateUrl()+"/"+serviceTemplateId;
		CloseableHttpResponse response = null;
		try {
			response = MSUtil.httpClientGetUrl(map, url);
			String reponseStr =EntityUtils.toString(response.getEntity());
			logger.info("service template info: "+reponseStr);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.readTree(reponseStr);
			JsonNode cNode = node.get("serviceTemplate");
			int status = cNode.get("template_status").asInt();
			if(0 == status){
				b = false;
			}
		} catch (Exception e) {
			logger.error("order case, get service template status failed,errorMsg: "+e.getLocalizedMessage());
			b = false;
			e.printStackTrace();
			return b;
		}
		
		try {
			response.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return b;
	}

}
