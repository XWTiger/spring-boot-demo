package com.chinacloud.isv.service;

import java.io.IOException;
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
import com.chinacloud.isv.entity.callbackparams.Attribute;
import com.chinacloud.isv.entity.mirtemplate.MirTemplate;
import com.chinacloud.isv.factory.WhiteholeFactory;
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
	public boolean configClonedFarm(MirTemplate mirTemplate,String cloneFarmId,ResultObject robj,ArrayList<Attribute> att_list){
		boolean b = true;
		String [] confList = null;
		if(null == mirTemplate){
			logger.error("order case,service template info is null");
			b = false;
			return b;
		}
		//get the configuration
		String infoUrl =  configuration.getMirBaseUrl()+"/mir/proxy/farms/"+cloneFarmId+"/edit";
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("X-Secure-Key", robj.getSecureKey());
		headers.put("X-Requested-Token", robj.getSpecialToken());
		CloseableHttpResponse infoResponse = null;
		try {
			infoResponse = MSUtil.httpClientGetUrl(headers, infoUrl);
			String conf = EntityUtils.toString(infoResponse.getEntity());
			confList = MSUtil.getConfiguratedString(mirTemplate, conf);
			if(null == confList){
				logger.error("get configuration failed,please get connection with manager");
				b = false;
				return b;
			}
			logger.debug("new configuration :");
			if(null != att_list){
				MSUtil.getComponentsList(confList[1], att_list);
			}else{
				logger.warn("the attribute list is null");
			}
			for (String string : confList) {
				logger.debug(string);
			}
		} catch (Exception e) {
			b = false;
			logger.error("get cloned farm configuration failed,errorMsg:"+e.getLocalizedMessage());
			e.printStackTrace();
			return b ;
		}
		
		try {
			infoResponse.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//save configuration
		String saveConfUrl = configuration.getMirBaseUrl()+"/mir/proxy/farms/builder/xBuild";
		String encodeStrFarm = confList[0];
		String encodeStrRole = confList[1];
		List<NameValuePair> params_list = new ArrayList<NameValuePair>();
		params_list.add(new BasicNameValuePair("farmId",String.valueOf(cloneFarmId)));
		params_list.add(new BasicNameValuePair("farm",encodeStrFarm));
		params_list.add(new BasicNameValuePair("roles",encodeStrRole));
		params_list.add(new BasicNameValuePair("v2","1"));
		params_list.add(new BasicNameValuePair("changed",confList[2]));
		CloseableHttpResponse saveResponse = null;
		try {
			saveResponse = MSUtil.httpClientPostUrl(headers, saveConfUrl, params_list);
			String saveR = EntityUtils.toString(saveResponse.getEntity());
			logger.info("save farm configuration response:"+saveR);
			WhiteholeFactory whiteholeFactory = new WhiteholeFactory();
			ResultObject rObject = whiteholeFactory.getEntity(ResultObject.class, saveR);
			if(!rObject.isSuccess()){
				b = false;
				return b;
			}
		} catch (Exception e) {
			b = false;
			logger.error("save farm configuration to mir falied,errorMsg:"+e.getLocalizedMessage());
			e.printStackTrace();
			return b;
		}
		try {
			saveResponse.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return b;
	}
}
