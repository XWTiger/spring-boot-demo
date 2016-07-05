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
import com.chinacloud.isv.entity.mirtemplate.MirTemplate;
import com.chinacloud.isv.util.MSUtil;

@Service
public class ConfigurateFarmService {
	
	private static final Logger logger = LogManager.getLogger(MirRequestService.class);
	
	@Autowired
	Configuration configuration;
	
	/**
	 * configuration cloned farm 
	 * @param mirTemplate
	 * @param cloneFarmId
	 * @param robj
	 * @return
	 */
	public boolean configClonedFarm(MirTemplate mirTemplate,String cloneFarmId,ResultObject robj){
		boolean b = true;
		if(null == mirTemplate){
			logger.error("order case,service template info is null");
			b = false;
			return b;
		}
		//get the configuration
		String infoUrl =  configuration.getMirConnectUrl()+"farms/"+cloneFarmId+"/edit";
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("X-Secure-Key", robj.getSecureKey());
		headers.put("X-Requested-Token", robj.getSpecialToken());
		CloseableHttpResponse infoResponse = null;
		try {
			infoResponse = MSUtil.httpClientGetUrl(headers, infoUrl);
			String conf = EntityUtils.toString(infoResponse.getEntity());
			logger.info("cloned configuration: "+conf);
			
		} catch (Exception e) {
			b = false;
			logger.error("get cloned farm configuration failed");
			e.printStackTrace();
			return b ;
		}
		
		try {
			infoResponse.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//save configuration
		String saveConfUrl = configuration.getMirConnectUrl()+"farms/builder/xBuild";
		return b;
	}

}
