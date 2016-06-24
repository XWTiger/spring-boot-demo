package com.chinacloud.isv.factory;

import java.io.IOException;
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
import com.chinacloud.isv.entity.mir.ServerInfo;
import com.chinacloud.isv.entity.mir.Servers;
import com.chinacloud.isv.service.LoginService;
import com.chinacloud.isv.util.CaseProvider;
import com.chinacloud.isv.util.MSUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
@Component
public class MirFactory {

	@Autowired
	Configuration configuration;
	@Autowired
	LoginService loginService;
	
	private static final Logger logger = LogManager.getLogger(MirFactory.class);
	//TODO every request Exception should be catch and return the result
	public String orderService(int farmId,TaskStack taskStack,VMQeuryParam vp) throws Exception{
		String startResult;
		ResultObject robj= loginService.login("xiaweihu@chinacloud.com.cn", "!@#$QWERasdfzxcv*&POIUjklmbn");
		WhiteholeFactory wFactory = new WhiteholeFactory();
		Params params = wFactory.getEntity(Params.class, taskStack.getParams());
		//do mir request
		logger.info("=====================申请事件,事件ID: "+params.getData().getEventId()+" ====================");
		if(!robj.getErrorMessage().equals("") && !robj.isSuccess()){
			logger.error("login mir plateform failed");
			String result = WhiteholeFactory.getFailedMsg(params, "处理失败,原因是登录mir系统失败。", CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER);
			return result;
		}else{
		//request headers
		Map<String,String> headerMap = new HashMap<String,String>();
		headerMap.put("X-Secure-Key", robj.getSecureKey());
		headerMap.put("X-Requested-Token", robj.getSpecialToken());
		vp.setxSecurityKey(robj.getSecureKey());
		vp.setSpecialToken(robj.getSpecialToken());
		//request url
		String farmCloneUrl = configuration.getMirConnectUrl()+"farms/xClone";
		//service clone	
		List<NameValuePair> params_list = new ArrayList<NameValuePair>();
		params_list.add(new BasicNameValuePair("farmId",String.valueOf(farmId)));
		CloseableHttpResponse chr = MSUtil.httpClientPostUrl(headerMap, farmCloneUrl, params_list);
		String CloneResult = EntityUtils.toString(chr.getEntity());
		//do analyze by result
		//String cloneR = "{\"success\":true,\"successMessage\":\"Farm successfully cloned. New farm: 'mir-pack-deploy-test (clone #4)'\"}";
		ResultObject resultObject = wFactory.getEntity(ResultObject.class, CloneResult);
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
			vp.setEnventId(params.getData().getEventId());
			logger.info("the farm Info===>"+farmInfo);
			response.close();
		}else{
			logger.error("clone farm failed farmid="+farmId+",errorMessage:"+resultObject.getErrorMessage());
			String result = WhiteholeFactory.getFailedMsg(params, "处理失败,原因是克隆应用堆栈失败。", CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER);
			return result;
		}
		//logger.info("clone Result======>"+CloneResult);
		//chr.close();
		if(null == cloneFarmId){
			logger.error("get cloned farm id failed");
			String result = WhiteholeFactory.getFailedMsg(params,"处理失败,原因是获取克隆应用堆栈ID失败。",CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER);
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
		}
		return startResult;
	}
	
	public String suspendService(Params params,int cFarmId){
		String result = null;
		WhiteholeFactory wFactory = new WhiteholeFactory();
		Map<String,String> headerMap = new HashMap<String,String>();
		//login
		ResultObject robj= loginService.login(null,null);
		if(!robj.getErrorMessage().equals("") && !robj.isSuccess()){
			String loginResult = WhiteholeFactory.getFailedMsg(params, "处理失败,原因是登录mir系统失败。", CaseProvider.EVENT_TYPE_SUBSCRIPTION_SUSPEND);
			logger.error("login mir plateform failed");
			return loginResult;
		}
		headerMap.put("X-Secure-Key", robj.getSecureKey());
		headerMap.put("X-Requested-Token", robj.getSpecialToken());
		//list servers
		CloseableHttpResponse qRes = null;
		String queryUrl = configuration.getMirConnectUrl()+"servers/xListServers/?farmId="+cFarmId+"&imageId=&limit=10&page=1&query=&start=0";
		String sInfoJson =null;
		try {
			qRes = MSUtil.httpClientGetUrl(headerMap, queryUrl);
			sInfoJson = EntityUtils.toString(qRes.getEntity());
		} catch (Exception e) {
			logger.error("when do suspend case,list servers failed case id");
			e.printStackTrace();
		}
		try {
			qRes.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//get server_id and suspend all
		Servers servers = null;
		try {
			servers = wFactory.getEntity(Servers.class, sInfoJson);
		}catch (Exception e) {
			logger.error("when suspend case, convert servers info to json failed");
			String listServersResult = WhiteholeFactory.getFailedMsg(params, "处理失败，转换虚拟机清单信息成JSON格式失败", CaseProvider.EVENT_TYPE_SUBSCRIPTION_SUSPEND);
			e.printStackTrace();
			return listServersResult;
		}
		String suspendUrl = configuration.getMirConnectUrl()+"servers/xSuspendServers";
		for (ServerInfo s : servers.getData()) {
			List<NameValuePair> params_list = new ArrayList<NameValuePair>();
			params_list.add(new BasicNameValuePair("servers","["+s.getServer_id()+"]"));
			try {
				MSUtil.httpClientPostUrl(headerMap, suspendUrl, params_list);
			} catch (Exception e) {
				logger.error("when suspend case, request mir to suspend vitrual machine failed， farm id:"+s.getServer_id());
				String listServersResult = WhiteholeFactory.getFailedMsg(params, "处理失败，挂机虚拟机失败", CaseProvider.EVENT_TYPE_SUBSCRIPTION_SUSPEND);
				e.printStackTrace();
				return listServersResult;
			}
		}
		result = WhiteholeFactory.getSuccessMsg(params, CaseProvider.EVENT_TYPE_SUBSCRIPTION_SUSPEND);
		return result;
	}
	
	public String cancleService(Params p,int cFarmId,VMQeuryParam vp){
		String result = null;
		WhiteholeFactory wFactory = new WhiteholeFactory();
		logger.debug("=====================注销事件 farmId:"+cFarmId+"====================");
		//create mir request url
		String listSSHkeyUrl = configuration.getMirConnectUrl()+"sshkeys/xListSshKeys?farmId="+cFarmId+"&page=1&start=0";
		String terminateFarmUrl = configuration.getMirConnectUrl()+"farms/xTerminate";
		String removeSSHKeyUrl = configuration.getMirConnectUrl()+"sshkeys/xRemove";
		ResultObject robj= loginService.login(null,null);
		if(!robj.getErrorMessage().equals("") && !robj.isSuccess()){
			String loginResult = WhiteholeFactory.getFailedMsg(p,"处理失败,原因是登录mir系统失败。", CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER);
			return loginResult;
		}
		Map<String,String> headerMap = new HashMap<String,String>();
		headerMap.put("X-Secure-Key", robj.getSecureKey());
		headerMap.put("X-Requested-Token", robj.getSpecialToken());
		vp.setxSecurityKey(robj.getSecureKey());
		vp.setSpecialToken(robj.getSpecialToken());
		//request mir
			//query ssh key id 
		String sshKeyId = null;
		try {
			CloseableHttpResponse response = MSUtil.httpClientGetUrl(headerMap, listSSHkeyUrl);
			String list = EntityUtils.toString(response.getEntity());
			ObjectMapper mapper = new ObjectMapper();
			logger.debug("the ssh list :"+list);
			JsonNode node = mapper.readTree(list);
			JsonNode rNode = node.get("data");
			if(rNode.isArray()){
				for (JsonNode jsonNode : rNode) {
					logger.debug("ssk's farm id :"+jsonNode.get("farm_id").toString());
					logger.debug("get the ssk id----->"+jsonNode.get("id").toString());
					logger.debug("the cFarmId--->"+cFarmId+","+String.valueOf(cFarmId));
					if(jsonNode.get("farm_id").asInt() == cFarmId){
						logger.debug("get the ssk id----->"+jsonNode.get("id").toString());
						sshKeyId = jsonNode.get("id").toString();
					};
				}
			}
			response.close();
			
		} catch (Exception e1) {
			logger.error("when do cancle case,get ssh key failed\n"+e1.getLocalizedMessage());
			e1.printStackTrace();
		}
			//terminate farm;
		if(null != sshKeyId){
			try {
				List<NameValuePair> params_list = new ArrayList<NameValuePair>();
				params_list.add(new BasicNameValuePair("farmId",String.valueOf(cFarmId)));
				CloseableHttpResponse response = MSUtil.httpClientPostUrl(headerMap, terminateFarmUrl, params_list);
				String requestR = EntityUtils.toString(response.getEntity());
				ResultObject ro= wFactory.getEntity(ResultObject.class, requestR);
				if(!ro.isSuccess()){
					logger.error("terminate farm failed");
					String resultCancleCase = WhiteholeFactory.getFailedMsg(p, "处理失败,原因是停止应用堆栈的失败。", CaseProvider.EVENT_TYPE_SUBSCRIPTION_CANCEL);
					return resultCancleCase;
				}
				response.close();
			} catch (Exception e) {
				logger.error("when do cancle case,request mir platform failed\n"+e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		//remove ssh key
		try {
			if(null == sshKeyId){
				logger.error("the ssh key id is empty");
				String resultCancleCase = WhiteholeFactory.getFailedMsg(p,"处理失败,原因是获取应用堆栈的SSH KEY失败。",CaseProvider.EVENT_TYPE_SUBSCRIPTION_CANCEL);
				return resultCancleCase;
			}
			List<NameValuePair> params_list = new ArrayList<NameValuePair>();
			params_list.add(new BasicNameValuePair("sshKeyId","[\""+sshKeyId+"\"]"));
			CloseableHttpResponse response = MSUtil.httpClientPostUrl(headerMap, removeSSHKeyUrl, params_list);
			String requestR = EntityUtils.toString(response.getEntity());
			ResultObject ro= wFactory.getEntity(ResultObject.class, requestR);
			if(!ro.isSuccess()){
				logger.error("remove farm ssh key failed");
				String resultCancleCase = WhiteholeFactory.getFailedMsg(p,"处理失败,原因是删除应用堆栈SSH KEY 失败。",CaseProvider.EVENT_TYPE_SUBSCRIPTION_CANCEL);
				return resultCancleCase;
			}
			result = requestR;
			response.close();
		} catch (Exception e) {
			logger.error("remove farm ssh key failed\n"+e.getLocalizedMessage());
			e.printStackTrace();
		}
		//remove farm stack move to mission queue
		
		return result;
	}
	
	public String activeService(Params p,int cFarmId,VMQeuryParam vp){
		
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
