package com.chinacloud.isv.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.chinacloud.isv.configuration.Configuration;
import com.chinacloud.isv.domain.TaskStack;
import com.chinacloud.isv.entity.Params;
import com.chinacloud.isv.entity.ResultObject;
import com.chinacloud.isv.entity.callbackparams.Attribute;
import com.chinacloud.isv.entity.callbackparams.Data;
import com.chinacloud.isv.entity.callbackparams.Process;
import com.chinacloud.isv.service.LoginService;
import com.chinacloud.isv.util.CaseProvider;
import com.chinacloud.isv.util.MSUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
@Component
public class MirFactory {

	@Autowired
	Configuration configuration;
	@Autowired
	LoginService loginService;
	
	private static final Logger logger = LogManager.getLogger(MirFactory.class);
	
	public String orderService(int farmId,TaskStack taskStack) throws Exception{
		Data data = new Data();
		Process process = new Process();
		ResultObject robj= loginService.login("xiaweihu@chinacloud.com.cn", "!@#$QWERasdfzxcv*&POIUjklmbn");
		WhiteholeFactory wFactory = new WhiteholeFactory();
		Params params = wFactory.getEntity(Params.class, taskStack.getParams());
		
		//do mir request
		if(robj.isSuccess()){
			System.out.println("special key--->"+robj.getSpecialToken());
		}
		
		if(robj.getErrorMessage().equals("")){
			data.setSuccess(false);
			data.setErrorCode("10001");
			data.setMessage(MSUtil.getChineseName(CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER)+"处理失败,原因是登录mir系统失败。");
			process.setEventId(params.getData().getEventId());
			process.setStatus("FAILED");
			logger.error("login mir plateform failed");
		}
		//request headers
		Map<String,String> headerMap = new HashMap<String,String>();
		headerMap.put("X-Secure-Key", "");
		headerMap.put("X-Requested-Token", "");
		//request url
		String farmCloneUrl = configuration.getMirConnectUrl()+"farms/xClone";
		//service clone	
		String body = "farmId="+farmId;
		CloseableHttpResponse chr = MSUtil.httpClientPostUrl(headerMap, farmCloneUrl, body);
		String CloneResult = EntityUtils.toString(chr.getEntity());
		//do analyze by result
		chr.close();
		//service start
		String farmStartUrl = configuration.getMirConnectUrl()+"farms/xLaunch";
		Map<String,String> startHeaderMap = new HashMap<String,String>();
		headerMap.put("X-Secure-Key", "");
		headerMap.put("X-Requested-Token", "");
		String startFarmBody = "farmId=";//get farm info by before
		CloseableHttpResponse startFarm = MSUtil.httpClientPostUrl(startHeaderMap, farmStartUrl, startFarmBody);
		String startResult = EntityUtils.toString(startFarm.getEntity());
		//do analyze by result
		startFarm.close();
		data.setSuccess(true);
		data.setMessage(MSUtil.getChineseName(CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER)+"处理成功");
		process.setEventId(params.getData().getEventId());
		process.setStatus("SUCCESS");
		process.setInstanceId(taskStack.getId());
		ArrayList<Attribute> att_list = new ArrayList<Attribute>();
		Attribute att = new Attribute();
		att.setKey("cpu");
		att.setValue("12");
		Attribute att2 = new Attribute();
		att2.setKey("ip");
		att2.setValue("127.0.0.1");
		att_list.add(att);
		att_list.add(att2);
		process.setAttribute(att_list);
		data.setProcess(process);
		String result = WhiteholeFactory.getJsonString(data);
		return result;
	}
	
	public String suspendService(){
		return "";
	}
	
	public String cancleService(int farmId){
		
		return "";
	}
	
	public String activeService(){
		return "";
	}
	
	public String queryService(){
		//WhiteholeFactory wFactory = new WhiteholeFactory();
		//Params params = wFactory.getEntity(Params.class, taskStack.getParams());
		Data data = new Data();
		data.setSuccess(true);
		data.setMessage(MSUtil.getChineseName(CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER)+"处理成功");
		com.chinacloud.isv.entity.callbackparams.Process process = new com.chinacloud.isv.entity.callbackparams.Process();
		process.setEventId("36a743f7-da51-4344-947b-82d58b0d3323");
		process.setStatus("SUCCESS");
		process.setInstanceId("4bf9ab03-112a-401c-9c80-4cab21d8ed82");
		ArrayList<Attribute> att_list = new ArrayList<Attribute>();
		Attribute att = new Attribute();
		att.setKey("cpu");
		att.setValue("8");
		Attribute att2 = new Attribute();
		att2.setKey("ip");
		att2.setValue("127.0.0.1");
		att_list.add(att);
		att_list.add(att2);
		process.setAttribute(att_list);
		data.setProcess(process);
		String result = null;
		try {
			result = WhiteholeFactory.getJsonString(data);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return result;
	}
}
