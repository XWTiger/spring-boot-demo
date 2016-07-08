package com.chinacloud.isv.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.chinacloud.isv.configuration.Configuration;
import com.chinacloud.isv.entity.ResultObject;
import com.chinacloud.isv.factory.WhiteholeFactory;
import com.chinacloud.isv.util.MSUtil;



@Service
@Scope
public class LoginService {
	ResultObject rObject = null;
	int times = 0 ;
	@Autowired
	Configuration configuration;
	
	private static final Logger logger = LogManager.getLogger(LoginService.class);
	
	  public ResultObject login(String username, String password){
		  	if(null == username || null == password ){
		  		username = configuration.getUserName();
		  		password = configuration.getPassword();
		  		if(times < configuration.getReLoginTimes() && null != rObject && rObject.getErrorMessage().equals("")){
			  		times++;
			  		return rObject;
			  	}
		  	}
		  	System.out.println("=========begin login mir==========");
	        CloseableHttpResponse resp = null;
	        try {
	            Map<String, String> headers = new HashMap<String, String>();
	            headers.put("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
	            headers.put("X-Scalr-Interface", "v2");
	            headers.put("X-Scalr-Token", "key");
	            String url =configuration.getMirConnectUrl()+"guest/xLogin";
	            List<NameValuePair> data = new ArrayList<NameValuePair>();
	            data.add(new BasicNameValuePair("scalrLogin", username));
	            data.add(new BasicNameValuePair("scalrPass", password));
	            data.add(new BasicNameValuePair("scalrKeepSession", "on"));
	            data.add(new BasicNameValuePair("scalrCaptchaChallenge", ""));
	            data.add(new BasicNameValuePair("userTimezone", "-480"));
	            resp = (CloseableHttpResponse) MSUtil.httpClientPostUrl(headers, url, data);
	            Header[] secureKey = resp.getHeaders("X-Secure-Key");
	            String strRet = EntityUtils.toString(resp.getEntity());
	            WhiteholeFactory whiteholeFactory = new WhiteholeFactory();
	            rObject = whiteholeFactory.getEntity(ResultObject.class, strRet);
	            logger.debug("securtiy key ======>"+secureKey[0].getValue());
	            rObject.setSecureKey(secureKey[0].getValue());
	            System.out.println("login --->strRet:"+strRet);
	        } catch ( Exception e) {
	           /* ret.setMessage("登录出现异常, " + e.getMessage());*/
	            logger.error("登录mir平台出现异常, " + e.getMessage());
	            rObject = new ResultObject();
	            rObject.setSuccess(false);
	            rObject.setErrorMessage("I can't login the mir platform");
	            return rObject;
	        }
	        try {
				resp.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	        times++;
	        return rObject;
	    }
	  
	 

}
