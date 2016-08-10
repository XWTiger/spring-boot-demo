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
import com.chinacloud.isv.util.MSUtil;

@Service
public class MirGetContextService {

	@Autowired
	Configuration configuration;
	
	private static final Logger logger = LogManager.getLogger(MirGetContextService.class);
	/**
	 * get mir context
	 * @param securityKey
	 * @param specialToken
	 * @return
	 */
	public String getContext(String securityKey,String specialToken){
		CloseableHttpResponse resp = null;
		String checkUrl = configuration.getMirBaseUrl()+"/mir/proxy/guest/xGetContext";
		Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        headers.put("X-Secure-Key", securityKey);
        headers.put("X-Requested-Token", specialToken);
        try {
    	   resp = (CloseableHttpResponse) MSUtil.httpClientPostUrl(headers, checkUrl, "");
		} catch (Exception e) {
			logger.error("get mir context failed,msg:"+e.getLocalizedMessage());
			return null;
		}
        String result = null;
		try {
			result = EntityUtils.toString(resp.getEntity());
		} catch (Exception e) {
			logger.error("convert mir context to string falied,msg:"+result);
			return null;
		}
		return result;	
	}
	
}
