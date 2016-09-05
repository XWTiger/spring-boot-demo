package com.chinacloud.isv.service;

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

@Service
public class MirEditService {

	@Autowired
	Configuration configuration;
	
	private static final Logger logger = LogManager.getLogger(MirEditService.class);
	
	/**
	 * get edit info  
	 * @param farmId
	 * @param robj
	 * @return json string. if error return null
	 */
	public String getFarmEditInfo(String farmId,ResultObject robj){
		String infoUrl =  configuration.getMirBaseUrl()+"/mir/proxy/farms/"+farmId+"/edit";
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("X-Secure-Key", robj.getSecureKey());
		headers.put("X-Requested-Token", robj.getSpecialToken());
		CloseableHttpResponse infoResponse = null;
		String conf = null;
		try {
			infoResponse = MSUtil.httpClientGetUrl(headers, infoUrl);
			conf = EntityUtils.toString(infoResponse.getEntity());
		} catch (Exception e) {
			logger.error("get edit info failed, errorMsg:"+e.getLocalizedMessage());
			e.printStackTrace();
			return null;
		}
		return conf;
	}
	

}
