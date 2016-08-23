package com.chinacloud.isv.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chinacloud.isv.configuration.Configuration;
import com.chinacloud.isv.entity.ResultObject;
import com.chinacloud.isv.util.MSUtil;

@Service
public class ChangeEnvironmentService {

	@Autowired
	Configuration configuration;
	
	private static final Logger logger = LogManager.getLogger(ChangeEnvironmentService.class);
	
	public boolean changeEnv(String envId,ResultObject robj){
		boolean b = true;
		String envUrl = configuration.getMirBaseUrl()+"/mir/proxy/core/xChangeEnvironment";
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("X-Secure-Key", robj.getSecureKey());
		headers.put("X-Requested-Token", robj.getSpecialToken());
		List<NameValuePair> params_list = new ArrayList<NameValuePair>();
		params_list.add(new BasicNameValuePair("envId",envId));
		CloseableHttpResponse  chr= null;
		String result = null;
		try {
			chr = MSUtil.httpClientPostUrl(headers, envUrl, params_list);
			result = EntityUtils.toString(chr.getEntity());
			if(!result.contains("\"success\":true")){
				b = false;
			}
		} catch (Exception e) {
			logger.error("change environment failed, errorMsg:"+ e.getLocalizedMessage());
			b = false;
			return b;
		}
		return b;
	}
}
