package com.chinacloud.isv.factory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import com.chinacloud.isv.domain.TaskResult;
import com.chinacloud.isv.domain.TaskStack;
import com.chinacloud.isv.entity.Params;
import com.chinacloud.isv.entity.ResultObject;
import com.chinacloud.isv.entity.VMQeuryParam;
import com.chinacloud.isv.entity.callbackparams.Attribute;
import com.chinacloud.isv.entity.callbackparams.Data;
import com.chinacloud.isv.entity.mir.FarmInfo;
import com.chinacloud.isv.entity.mir.Farms;
import com.chinacloud.isv.entity.mir.ServerInfo;
import com.chinacloud.isv.entity.mir.Servers;
import com.chinacloud.isv.persistance.TaskResultDao;
import com.chinacloud.isv.persistance.TaskStackDao;
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
	@Autowired
	TaskResultDao taskResultDao;
	@Autowired
	TaskStackDao taskStackDao;
	
	private static final Logger logger = LogManager.getLogger(MirFactory.class);
	//TODO every request Exception should be catch and return the result
	public String orderService(int farmId,TaskStack taskStack,VMQeuryParam vp){
		String startResult = null;
		ResultObject robj= loginService.login("xiaweihu@chinacloud.com.cn", "!@#$QWERasdfzxcv*&POIUjklmbn");
		WhiteholeFactory wFactory = new WhiteholeFactory();
		Params params = null;
		try {
			params = wFactory.getEntity(Params.class, taskStack.getParams());
			vp.setUsrName(params.getData().getCreator().getEmail());
			vp.setModelFarmId(farmId);
			vp.setSystem(params.getData().getPayload().getTenant().getName());
			//TODO vp add service instance id
			
		} catch (Exception e) {
			logger.error("order case,convert string to object failed.");
			e.printStackTrace();
		} 
		//do mir request
		logger.info("=====================申请事件,事件ID: "+params.getData().getEventId()+" ====================");
		if(!robj.getErrorMessage().equals("") && !robj.isSuccess()){
			logger.error("login mir plateform failed");
			String result = WhiteholeFactory.getFailedMsg(params, "处理失败,原因是登录mir系统失败。", CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER);
			taskStackDao.unLockTask(taskStack.getId());
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
		CloseableHttpResponse chr = null;
		try {
			chr = MSUtil.httpClientPostUrl(headerMap, farmCloneUrl, params_list);
		} catch (Exception e) {
			logger.error("order case,clone farm stack failed.");
			taskStackDao.unLockTask(taskStack.getId());
			e.printStackTrace();
		}
		String CloneResult = null;
		try {
			CloneResult = EntityUtils.toString(chr.getEntity());
			logger.info("order case,clone farm stack request's result:"+CloneResult);
		} catch (Exception e) {
			logger.error("order case,convert clone result to string failed");
			e.printStackTrace();
		} 
		//do analyze by result
		ResultObject resultObject = null;
		try {
			resultObject = wFactory.getEntity(ResultObject.class, CloneResult);
		} catch (Exception e) {
			logger.error("order case, convert string to result object failed");
			e.printStackTrace();
		} 
		String cloneFarmId = null;
		String roles = null;
		//get the clone farm info
		if(resultObject.isSuccess()){
			String farmName = MSUtil.getFarmNameFromResult(resultObject.getSuccessMessage());
			String farmNameEncode = null;
			try {
				farmNameEncode = URLEncoder.encode(farmName, "utf-8");
			} catch (UnsupportedEncodingException e) {
				logger.error("order case,farmName encode error "+e.getLocalizedMessage());
				e.printStackTrace();
			}
			String farmInfoUrl = configuration.getMirConnectUrl()+"farms/xListFarms?farmId=&limit=10&page=1&query="+farmNameEncode+"&showOnlyMy=0&sort=%5B%7B%22property%22:%22id%22,%22direction%22:%22DESC%22%7D%5D&start=0";
			CloseableHttpResponse response = null;
			try {
				response = MSUtil.httpClientGetUrl(headerMap, farmInfoUrl);
			} catch (Exception e) {
				logger.error("order case,get cloned farm info fialed "+e.getLocalizedMessage());
				e.printStackTrace();
			}
			String farmInfo = null;
			try {
				farmInfo = EntityUtils.toString(response.getEntity());
				logger.info("order case,cloned farm info:"+farmInfo);
			} catch (Exception e) {
				logger.error("order case, get cloned farm info ,entity utils convert entity to string failed");
				e.printStackTrace();
			}
			Farms farms = null;
			try {
				farms = wFactory.getEntity(Farms.class,farmInfo);
			} catch (Exception e) {
				logger.error("order case, convert farm info to object failed");
				e.printStackTrace();
			} 
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
			try {
				response.close();
			} catch (IOException e) {
				logger.error("close ");
				e.printStackTrace();
			}
		}else{//clone farm failed 
			logger.error("clone farm failed farmid="+farmId+",errorMessage:"+resultObject.getErrorMessage());
			String result = WhiteholeFactory.getFailedMsg(params, "处理失败,原因是克隆应用堆栈失败。", CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER);
			return result;
		}
		
		try {
			chr.close();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
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
		vp.setcFarmId(Integer.parseInt(cloneFarmId));
		params_list_2.add(new BasicNameValuePair("farmId",cloneFarmId));
		CloseableHttpResponse startFarm = null;
		try {
			startFarm = MSUtil.httpClientPostUrl(headerMap, farmStartUrl, params_list_2);
		} catch (Exception e) {
			logger.error("order case,start clone farm failed,cloned farm id:"+cloneFarmId);
			String result = WhiteholeFactory.getFailedMsg(params,"处理失败,原因是启动克隆应用堆栈失败。",CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER);
			e.printStackTrace();
			return result;
		}
		try {
			startResult = EntityUtils.toString(startFarm.getEntity());
		} catch (Exception e1) {
			logger.error("order case,convert start farm entity to string failed");
			e1.printStackTrace();
		} 
		//do analyze by result
		logger.debug("the roles number--->"+roles);
		try {
			startFarm.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		}
		return startResult;
	}
	
	public String suspendService(Params params,int cFarmId,TaskStack taskStack){
		String result = null;
		WhiteholeFactory wFactory = new WhiteholeFactory();
		Map<String,String> headerMap = new HashMap<String,String>();
		//login
		ResultObject robj= loginService.login(null,null);
		if(!robj.getErrorMessage().equals("") && !robj.isSuccess()){
			String loginResult = WhiteholeFactory.getFailedMsg(params, "处理失败,原因是登录mir系统失败。", CaseProvider.EVENT_TYPE_SUBSCRIPTION_SUSPEND);
			logger.error("login mir plateform failed");
			taskStackDao.unLockTask(taskStack.getId());
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
			logger.debug("when suspend case, get vm info:"+sInfoJson);
		} catch (Exception e) {
			logger.error("when do suspend case,list servers failed. farm id:"+cFarmId);
			taskStackDao.unLockTask(taskStack.getId());
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
			taskStackDao.unLockTask(taskStack.getId());
			e.printStackTrace();
			return listServersResult;
		}
		String suspendUrl = configuration.getMirConnectUrl()+"servers/xSuspendServers";
		for (ServerInfo s : servers.getData()) {
			List<NameValuePair> params_list = new ArrayList<NameValuePair>();
			logger.debug("server id :"+"["+s.getServer_id()+"]");
			params_list.add(new BasicNameValuePair("servers","[\""+s.getServer_id()+"\"]"));
			logger.debug("the params_list info:"+params_list.toString());
			CloseableHttpResponse suspendR = null;
			try {
				suspendR = MSUtil.httpClientPostUrl(headerMap, suspendUrl, params_list);
				logger.info("suspend case, result:"+EntityUtils.toString(suspendR.getEntity()));
			} catch (Exception e) {
				logger.error("when suspend case, request mir to suspend vitrual machine failed， farm id:"+s.getServer_id());
				String listServersResult = WhiteholeFactory.getFailedMsg(params, "处理失败，挂起虚拟机失败", CaseProvider.EVENT_TYPE_SUBSCRIPTION_SUSPEND);
				taskStackDao.unLockTask(taskStack.getId());
				e.printStackTrace();
				return listServersResult;
			}
			try {
				suspendR.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		result = WhiteholeFactory.getSuccessMsg(params, CaseProvider.EVENT_TYPE_SUBSCRIPTION_SUSPEND);
		return result;
	}
	
	public String cancleService(Params p,int cFarmId,VMQeuryParam vp,TaskStack taskStack){
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
			taskStackDao.unLockTask(taskStack.getId());
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
			taskStackDao.unLockTask(taskStack.getId());
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
				taskStackDao.unLockTask(taskStack.getId());
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
			logger.error("cancle case,remove farm ssh key failed\n"+e.getLocalizedMessage());
			e.printStackTrace();
		}
		//remove farm stack move to mission queue
		
		return result;
	}
	
	public String activeService(Params p,int cFarmId,VMQeuryParam vp,TaskStack taskStack){
		String result = null;
		WhiteholeFactory wFactory = new WhiteholeFactory();
		Map<String,String> headerMap = new HashMap<String,String>();
		//login
		ResultObject robj= loginService.login(null,null);
		if(!robj.getErrorMessage().equals("") && !robj.isSuccess()){
			String loginResult = WhiteholeFactory.getFailedMsg(p, "处理失败,原因是登录mir系统失败。", CaseProvider.EVENT_TYPE_SUBSCRIPTION_ACTIVE);
			logger.error("login mir plateform failed");
			return loginResult;
		}
		headerMap.put("X-Secure-Key", robj.getSecureKey());
		headerMap.put("X-Requested-Token", robj.getSpecialToken());
		vp.setxSecurityKey(robj.getSecureKey());
		vp.setSpecialToken(robj.getSpecialToken());
		//list servers
		CloseableHttpResponse qRes = null;
		String queryUrl = configuration.getMirConnectUrl()+"servers/xListServers/?farmId="+cFarmId+"&imageId=&limit=10&page=1&query=&start=0";
		String sInfoJson =null;
		try {
			qRes = MSUtil.httpClientGetUrl(headerMap, queryUrl);
			sInfoJson = EntityUtils.toString(qRes.getEntity());
		} catch (Exception e) {
			logger.error("when do active case,list servers failed. farm id:"+cFarmId);
			taskStackDao.unLockTask(taskStack.getId());
			e.printStackTrace();
		}
		try {
			qRes.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//get server_id and active all
		Servers servers = null;
		int total = 0 ;//virtual machine number
		int count = 0;
		try {
			servers = wFactory.getEntity(Servers.class, sInfoJson);
			total = Integer.parseInt(servers.getTotal());
		}catch (Exception e) {
			logger.error("when active vitrual case, convert servers info to json failed");
			String listServersResult = WhiteholeFactory.getFailedMsg(p, "处理失败，转换虚拟机清单信息成JSON格式失败", CaseProvider.EVENT_TYPE_SUBSCRIPTION_ACTIVE);
			e.printStackTrace();
			return listServersResult;
		}
		String suspendUrl = configuration.getMirConnectUrl()+"servers/xResumeServers";
		for (ServerInfo s : servers.getData()) {
			List<NameValuePair> params_list = new ArrayList<NameValuePair>();
			params_list.add(new BasicNameValuePair("servers","[\""+s.getServer_id()+"\"]"));
			CloseableHttpResponse activeR = null;
			try {
				activeR = MSUtil.httpClientPostUrl(headerMap, suspendUrl, params_list);
				logger.info("active case,result:"+EntityUtils.toString(activeR.getEntity()));
				count++;
			} catch (Exception e) {
				logger.error("when active vitrual case, request mir to suspend vitrual machine failed， farm id:"+cFarmId);
				String listServersResult = WhiteholeFactory.getFailedMsg(p, "处理失败，挂机虚拟机失败", CaseProvider.EVENT_TYPE_SUBSCRIPTION_ACTIVE);
				taskStackDao.unLockTask(taskStack.getId());
				e.printStackTrace();
				return listServersResult;
			}
			try {
				activeR.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(total == count){
			result = CaseProvider.ACTIVE_FIRST_STEP; 
		}else{
			logger.error("active number less than total");
		}
		return result;
	}
	
	public String queryService(Params p){
		String result = null;
		Data data = new Data();
		String instanceId = p.getData().getPayload().getInstance().getInstanceId();
		logger.info("QUERY　CASE: the instance id---->"+instanceId);
		logger.info("test the  taskResultDao--->"+taskResultDao==null);
		TaskResult tr = taskResultDao.getOrderTaskResultById(instanceId);
		if(null == tr){
			logger.error("when do query case,get clone farm id failed because of database return null");
		}
		//task result the order mission is time out
		if(tr.getErrorInfo().contains(CaseProvider.STATUS_TIME_OUT)){
			
		}
		com.chinacloud.isv.entity.callbackparams.Process process = new com.chinacloud.isv.entity.callbackparams.Process();
		Map<String,String> headerMap = new HashMap<String,String>();
		String queryUrl = configuration.getMirConnectUrl()+"servers/xListServers/?farmId="+tr.getcFarmId()+"&imageId=&limit=10&page=1&query=&start=0";
		//login
		ResultObject robj= loginService.login(null,null);
		if(!robj.getErrorMessage().equals("") && !robj.isSuccess()){
			String loginResult = WhiteholeFactory.getFailedMsg(p, "处理失败,原因是登录mir系统失败。", CaseProvider.EVENT_TYPE_SUBSCRIPTION_QUERY);
			logger.error("login mir plateform failed");
			return loginResult;
		}
		headerMap.put("X-Secure-Key", robj.getSecureKey());
		headerMap.put("X-Requested-Token", robj.getSpecialToken());
		CloseableHttpResponse qResult = null;
		try {
			qResult = MSUtil.httpClientGetUrl(headerMap, queryUrl);
		} catch (Exception e1) {
			logger.error("query case,request mir plate failed");
			result = WhiteholeFactory.getFailedMsg(p,  "处理失败,原因是查询应用堆栈信息请求失败。", CaseProvider.EVENT_TYPE_SUBSCRIPTION_QUERY);
			e1.printStackTrace();
			return result;
		}
		String queryR = null;
		try {
			queryR = EntityUtils.toString(qResult.getEntity());
		} catch (Exception e1) {
			logger.error("convert to entity to string failed\n"+e1.getLocalizedMessage());
			e1.printStackTrace();
		} 
		WhiteholeFactory wf = new WhiteholeFactory();
		Servers server = null;
		try {
			server = wf.getEntity(Servers.class,queryR);
		} catch (Exception e1) {
			logger.error("convert String to object failed\n"+e1.getLocalizedMessage());
			e1.printStackTrace();
		} 
		ArrayList<ServerInfo> sList = server.getData();
		//do analyze
		int total = Integer.parseInt(server.getTotal());
		if(total <= 0){
			result = WhiteholeFactory.getFailedMsg(p, "处理失败，获取虚拟机数量小于等于0",CaseProvider.EVENT_TYPE_SUBSCRIPTION_QUERY);
		}else{
			ArrayList<Attribute> att_list = new ArrayList<Attribute>();
			data.setSuccess(true);
			data.setMessage(MSUtil.getChineseName(CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER)+"处理成功");
			process.setEventId(p.getData().getEventId());
			process.setStatus("SUCCESS");
			process.setInstanceId(instanceId);
			for (ServerInfo serverInfo : sList) {
				Attribute att = new Attribute();
				att.setKey("role_name");
				att.setValue(serverInfo.getRole_name());
				Attribute att2 = new Attribute();
				att2.setKey("flavor");
				att2.setValue(serverInfo.getFlavor());
				Attribute att3 = new Attribute();
				att3.setKey("farm_id");
				att3.setValue(serverInfo.getFarm_id());
				Attribute att4 = new Attribute();
				att4.setKey("local_ip");
				att4.setValue(serverInfo.getLocal_ip());
				Attribute att5 = new Attribute();
				att5.setKey("remote_ip");
				att5.setValue(serverInfo.getRemote_ip());
				att_list.add(att3);
				att_list.add(att);
				att_list.add(att2);
				att_list.add(att4);
				att_list.add(att5);
			}
			process.setAttribute(att_list);
			data.setProcess(process);
			try {
				result = WhiteholeFactory.getJsonString(data);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public boolean removeCloneFarm(String cFarmId,String sercurity,String token){
		boolean b = true;
		Map<String,String> headerMap = new HashMap<String,String>();
		headerMap.put("X-Secure-Key", sercurity);
		headerMap.put("X-Requested-Token", token);
		String removeFarmUrl = configuration.getMirConnectUrl()+"farms/xRemove";
		List<NameValuePair> params_list = new ArrayList<NameValuePair>();
		params_list.add(new BasicNameValuePair("farmId",String.valueOf(cFarmId)));
		try {
			CloseableHttpResponse response = MSUtil.httpClientPostUrl(headerMap, removeFarmUrl, params_list);
			logger.info("start cloned farm faled, remove cloned farm's result:"+EntityUtils.toString(response.getEntity()));
		} catch (Exception e) {
			b = false;
			e.printStackTrace();
		}
		return b;
	}
}
