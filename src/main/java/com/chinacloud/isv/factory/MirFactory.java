package com.chinacloud.isv.factory;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
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
import org.springframework.stereotype.Component;

import com.chinacloud.isv.configuration.Configuration;
import com.chinacloud.isv.domain.TaskStack;
import com.chinacloud.isv.entity.Params;
import com.chinacloud.isv.entity.ResultObject;
import com.chinacloud.isv.entity.VMQeuryParam;
import com.chinacloud.isv.entity.callbackparams.Attribute;
import com.chinacloud.isv.entity.callbackparams.Data;
import com.chinacloud.isv.entity.callbackparams.Process;
import com.chinacloud.isv.entity.mir.FarmInfo;
import com.chinacloud.isv.entity.mir.Farms;
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
	
	public String orderService(int farmId,TaskStack taskStack,VMQeuryParam vp) throws Exception{
		Data data = new Data();
		Process process = new Process();
		String startResult;
		ResultObject robj= loginService.login("xiaweihu@chinacloud.com.cn", "!@#$QWERasdfzxcv*&POIUjklmbn");
		WhiteholeFactory wFactory = new WhiteholeFactory();
		Params params = wFactory.getEntity(Params.class, taskStack.getParams());
		
		//do mir request
		logger.info("=====================申请事件,事件ID: "+params.getData().getEventId()+" ====================");
		
		if(!robj.getErrorMessage().equals("") && !robj.isSuccess()){
			data.setSuccess(false);
			data.setErrorCode("10001");
			data.setMessage(MSUtil.getChineseName(CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER)+"处理失败,原因是登录mir系统失败。");
			process.setEventId(params.getData().getEventId());
			process.setStatus("FAILED");
			logger.error("login mir plateform failed");
			data.setProcess(process);
			String result = WhiteholeFactory.getJsonString(data);
			return result;
		}else{
		//request headers
		Map<String,String> headerMap = new HashMap<String,String>();
		headerMap.put("X-Secure-Key", robj.getSecureKey());
		headerMap.put("X-Requested-Token", robj.getSpecialToken());
		//request url
		String farmCloneUrl = configuration.getMirConnectUrl()+"farms/xClone";
		//service clone	
		List<NameValuePair> params_list = new ArrayList<NameValuePair>();
		params_list.add(new BasicNameValuePair("farmId",String.valueOf(farmId)));
		/*CloseableHttpResponse chr = MSUtil.httpClientPostUrl(headerMap, farmCloneUrl, params_list);
		String CloneResult = EntityUtils.toString(chr.getEntity());*/
		//do analyze by result
		String cloneR = "{\"success\":true,\"successMessage\":\"Farm successfully cloned. New farm: 'mir-pack-deploy-test (clone #4)'\"}";
		ResultObject resultObject = wFactory.getEntity(ResultObject.class, cloneR);
		String cloneFarmId = null;
		String roles = null;
		//get the clone farm info
		if(resultObject.isSuccess()){
			String farmName = MSUtil.getFarmNameFromResult(resultObject.getSuccessMessage());
			String farmNameEncode = URLEncoder.encode(farmName, "utf-8");
			String farmInfoUrl = configuration.getMirConnectUrl()+"farms/xListFarms?farmId=&limit=10&page=1&query="+farmNameEncode+"&showOnlyMy=0&sort=%5B%7B%22property%22:%22id%22,%22direction%22:%22DESC%22%7D%5D&start=0";
			CloseableHttpResponse response = MSUtil.httpClientGetUrl(headerMap, farmInfoUrl);
			String farmInfo = EntityUtils.toString(response.getEntity());
			Farms farms = wFactory.getEntity(Farms.class,farmInfo);
			if(farms.getData().size() == 1){
				cloneFarmId = farms.getData().get(0).getId();
				roles = farms.getData().get(0).getRoles();
			}else{
				for (FarmInfo farminfo : farms.getData()) {
					if(farminfo.equals(farmName)){
						cloneFarmId = farminfo.getId();
						roles = farminfo.getRoles();
					}
				}
			}
			vp.setcFarmId(Integer.parseInt(cloneFarmId));
			vp.setRoles(Integer.parseInt(roles));
			vp.setBeginTime(new Date().getTime());
			vp.setTaskId(taskStack.getId());
			logger.info("the farm Info===>"+farmInfo);
			response.close();
		}else{
			logger.error("clone farm failed farmid="+farmId+",errorMessage:"+resultObject.getErrorMessage());
			data.setSuccess(false);
			data.setErrorCode("10001");
			data.setMessage(MSUtil.getChineseName(CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER)+"处理失败,原因是克隆应用堆栈失败。");
			process.setEventId(params.getData().getEventId());
			process.setStatus("FAILED");
			data.setProcess(process);
			String result = WhiteholeFactory.getJsonString(data);
			return result;
		}
		//logger.info("clone Result======>"+CloneResult);
		//chr.close();
		if(null == cloneFarmId){
			logger.error("get cloned farm id failed");
			data.setSuccess(false);
			data.setErrorCode("10001");
			data.setMessage(MSUtil.getChineseName(CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER)+"处理失败,原因是获取克隆应用堆栈ID失败。");
			process.setEventId(params.getData().getEventId());
			process.setStatus("FAILED");
			data.setProcess(process);
			String result = WhiteholeFactory.getJsonString(data);
			return result;
		}
		
		/*-------------------*/
		//TODO add farm config operations
		/*-------------------*/
		
		//service start
		String farmStartUrl = configuration.getMirConnectUrl()+"farms/xLaunch";
		List<NameValuePair> params_list_2 = new ArrayList<NameValuePair>();
		params_list_2.add(new BasicNameValuePair("farmId",cloneFarmId));
		CloseableHttpResponse startFarm = MSUtil.httpClientPostUrl(headerMap, farmStartUrl, params_list_2);
		startResult = EntityUtils.toString(startFarm.getEntity());
		//do analyze by result
		
		logger.debug("the roles number--->"+roles);
		startFarm.close();
		
		/*data.setSuccess(true);
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
		Attribute att3 = new Attribute();
		att3.setKey("farmId");
		att3.setValue(cloneFarmId);
		att_list.add(att3);
		att_list.add(att);
		att_list.add(att2);
		process.setAttribute(att_list);
		data.setProcess(process);*/
		}
		return startResult;
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
