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

import com.chinacloud.isv.component.VirtualMachineStatusCheck;
import com.chinacloud.isv.configuration.Configuration;
import com.chinacloud.isv.domain.TaskResult;
import com.chinacloud.isv.domain.TaskStack;
import com.chinacloud.isv.entity.Params;
import com.chinacloud.isv.entity.ResultObject;
import com.chinacloud.isv.entity.VMQeuryParam;
import com.chinacloud.isv.entity.callbackparams.DataExtend;
import com.chinacloud.isv.entity.callbackparams.Instance;
import com.chinacloud.isv.entity.callbackparams.ProcessExtend;
import com.chinacloud.isv.entity.mir.FarmInfo;
import com.chinacloud.isv.entity.mir.Farms;
import com.chinacloud.isv.entity.mir.ServerInfo;
import com.chinacloud.isv.entity.mir.Servers;
import com.chinacloud.isv.entity.mirtemplate.ComponentInfo;
import com.chinacloud.isv.entity.mirtemplate.MirTemplate;
import com.chinacloud.isv.entity.mirtemplate.ServiceTemplate;
import com.chinacloud.isv.persistance.TaskResultDao;
import com.chinacloud.isv.persistance.TaskStackDao;
import com.chinacloud.isv.service.ConfigurateFarmService;
import com.chinacloud.isv.service.LoginService;
import com.chinacloud.isv.service.MSTemplateService;
import com.chinacloud.isv.service.MirEditService;
import com.chinacloud.isv.service.UnlockService;
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
	@Autowired
	UnlockService unlockService;
	@Autowired
	MSTemplateService msTemplateService;
	@Autowired
	ConfigurateFarmService configurateFarmService;
	@Autowired
	VirtualMachineStatusCheck virtualMachineStatusCheck;
	@Autowired
	MirEditService mirEditService;
	
	private static final Logger logger = LogManager.getLogger(MirFactory.class);
	//every request Exception should be catch and return the result
	public String orderService(String farmId,TaskStack taskStack,VMQeuryParam vp){
	
		
		ArrayList<com.chinacloud.isv.entity.callbackparams.Component> att_list = new ArrayList<com.chinacloud.isv.entity.callbackparams.Component>();
		Params params = null;
		String cloneFarmId = null;
		MirTemplate mTemplate = null;
		Farms farms = null;
		
		vp.setDestinationFarmId(String.valueOf(farmId));
		vp.setParams(taskStack.getParams());
		WhiteholeFactory wFactory = new WhiteholeFactory();
		try {
			params = wFactory.getEntity(Params.class, taskStack.getParams());
			vp.setUsrName(params.getData().getCreator().getEmail());
			vp.setModelFarmId(farmId);
			vp.setSystem(params.getData().getMarketplace().getPartner());
			vp.setTenantId(params.getData().getPayload().getTenant().getId());
		} catch (Exception e) {
			logger.error("order case,convert string to object failed. task id: "+taskStack.getId());
			e.printStackTrace();
		} 
		//get service template id 
		try {
			mTemplate = wFactory.getEntity(MirTemplate.class, params.getData().getPayload().getOrder().getEditionCode());
		} catch (Exception e3) {
			logger.error("order case,convert mir template string to object failed. task id: "+taskStack.getId());
			e3.printStackTrace();
		} 
		ServiceTemplate sTemplateEntity = msTemplateService.getServiceTempalteEntity(mTemplate.getServiceTemplateId());
		if(null == sTemplateEntity){
			String result = WhiteholeFactory.getFailedMsg(params, "处理失败,服务模板信息获取失败,请稍后再试。", CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER);
			return result;
		}
		if(0 == sTemplateEntity.getServiceTemplate().getTemplate_status()){
			String result = WhiteholeFactory.getFailedMsg(params, "处理失败,服务模板已被禁用或者无法获取其状态,请稍后再试。", CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER);
			return result;
		}
		//vp add service instance id
		int totalInstance = 0;
		vp.setEnvId(sTemplateEntity.getServiceTemplate().getEnv_id());
		vp.setServiceTemplateId(mTemplate.getServiceTemplateId());
		vp.setServiceTemplateName(mTemplate.getServiceTemplateName());
		for (ComponentInfo ci : mTemplate.getComponentInfo()) {
			totalInstance += Integer.parseInt(ci.getUnitInstanceNumber());
		}
		vp.setTotalInstance(totalInstance);
		ResultObject robj= loginService.login(null, null);
		//do mir request
		logger.info("=====================申请事件,实例ID: "+taskStack.getId()+" =========totalInstance:"+totalInstance+"===========");
		if(!robj.getErrorMessage().equals("") && !robj.isSuccess()){
			logger.error("login mir plateform failed");
			String result = WhiteholeFactory.getFailedMsg(params, "处理失败,原因是登录mir系统失败。", CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER);
			unlockService.unlockMission(taskStack);
			return result;
		}else{
		//request headers
		Map<String,String> headerMap = new HashMap<String,String>();
		headerMap.put("X-Secure-Key", robj.getSecureKey());
		headerMap.put("X-Requested-Token", robj.getSpecialToken());
		//request url
		String farmCloneUrl = configuration.getMirBaseUrl()+"/mir/proxy/farms/xClone";
		//service clone	
		List<NameValuePair> params_list = new ArrayList<NameValuePair>();
		params_list.add(new BasicNameValuePair("farmId",String.valueOf(farmId)));
		CloseableHttpResponse chr = null;
		try {
			chr = MSUtil.httpClientPostUrl(headerMap, farmCloneUrl, params_list);
		} catch (Exception e) {
			logger.error("order case,clone farm stack failed, we will unlock the task. msg:"+e.getLocalizedMessage());
			unlockService.unlockMission(taskStack);
		}
		String CloneResult = null;
		try {
			CloneResult = EntityUtils.toString(chr.getEntity());
			logger.info("order case,clone farm stack request's result:"+CloneResult);
		} catch (Exception e) {
			logger.error("order case,convert clone result to string failed, msg:"+e.getLocalizedMessage());
			return WhiteholeFactory.getFailedMsg(params, "处理失败，原因是转换克隆结果成为字符串失败", CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER);
		} 
		//do analyze by result
		ResultObject resultObject = null;
		try {
			resultObject = wFactory.getEntity(ResultObject.class, CloneResult);
		} catch (Exception e) {
			logger.error("order case, convert string to result object failed, msg:"+e.getLocalizedMessage());
			return WhiteholeFactory.getFailedMsg(params, "处理失败，原因是转换克隆结果成为实例失败", CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER);
		} 
		String roles = null;
		//get the clone farm info
		if(resultObject.isSuccess()){
			String farmName = MSUtil.getFarmNameFromResult(resultObject.getSuccessMessage());
			String farmNameEncode = null;
			try {
				farmNameEncode = URLEncoder.encode(farmName, "utf-8");
			} catch (UnsupportedEncodingException e) {
				logger.error("order case,farmName encode error "+e.getLocalizedMessage());
				return WhiteholeFactory.getFailedMsg(params, "处理失败，原因是编码应用堆栈名称失败", CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER);
			}
			String farmInfoUrl = configuration.getMirBaseUrl()+"/mir/proxy/farms/xListFarms?farmId=&limit=10&page=1&query="+farmNameEncode+"&showOnlyMy=0&sort=%5B%7B%22property%22:%22id%22,%22direction%22:%22DESC%22%7D%5D&start=0";
			CloseableHttpResponse response = null;
			try {
				response = MSUtil.httpClientGetUrl(headerMap, farmInfoUrl);
			} catch (Exception e) {
				logger.error("order case,get cloned farm info fialed "+e.getLocalizedMessage());
				return WhiteholeFactory.getFailedMsg(params, "处理失败，原因是获取克隆的应用堆栈信息失败", CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER);
			}
			String farmInfo = null;
			try {
				farmInfo = EntityUtils.toString(response.getEntity());
				logger.info("order case,cloned farm info:"+farmInfo);
			} catch (Exception e) {
				logger.error("order case, get cloned farm info ,entity utils convert entity to string failed,msg:"+e.getLocalizedMessage());
				return WhiteholeFactory.getFailedMsg(params, "处理失败，原因是转换克隆的应用堆栈信息为字符串失败", CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER);
			}
			try {
				farms = wFactory.getEntity(Farms.class,farmInfo);
			} catch (Exception e) {
				logger.error("order case, convert farm info to object failed,msg:"+e.getLocalizedMessage());
				return WhiteholeFactory.getFailedMsg(params, "处理失败，原因是转换克隆的应用堆栈信息为实例失败", CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER);
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
			logger.info("the farm Info===>"+farmInfo);
			logger.info("roles =====>"+roles);
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
		vp.setcFarmId(cloneFarmId);
		/*-------------------*/
		//TODO add farm config operations
		/*-------------------*/
		if(!configurateFarmService.configClonedFarm(mTemplate, cloneFarmId, robj,att_list)){
			removeCloneFarm(cloneFarmId, robj.getSecureKey(), robj.getSpecialToken());
			String result = WhiteholeFactory.getFailedMsg(params,"处理失败,原因是配置克隆应用堆栈失败。",CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER);
			return result;
		}
		}
		String result = null;
		result = MSUtil.getResponseDataForWihtehole(true, params, farms, cloneFarmId,
						configuration.getMirMoreOperateUrl()+"/"+cloneFarmId, 
						MSUtil.getChineseName(CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER)+"处理成功", 
						taskStack, att_list);
		return result;
	}
	
	public String launchService(Params params,String cFarmId,TaskStack taskStack,VMQeuryParam vp,Params orderParams){
		String startResult = null;
		MirTemplate mTemplate = null;
		WhiteholeFactory wFactory = new WhiteholeFactory();
		logger.info("=====================启动事件,farm ID: "+cFarmId+"===============================");
		//get service template id 
		try {
			mTemplate = wFactory.getEntity(MirTemplate.class, orderParams.getData().getPayload().getOrder().getEditionCode());
		} catch (Exception e3) {
			logger.error("order case,convert mir template string to object failed. task id: "+taskStack.getId());
			e3.printStackTrace();
		} 
		//vp add service instance id
		int totalInstance = 0;
		vp.setServiceTemplateId(mTemplate.getServiceTemplateId());
		for (ComponentInfo ci : mTemplate.getComponentInfo()) {
			totalInstance += Integer.parseInt(ci.getUnitInstanceNumber());
		}
		if(0 == totalInstance){
			startResult = WhiteholeFactory.getFailedMsg(params,"处理失败,原因是虚拟机数量为0，请联系管理员。",CaseProvider.EVENT_TYPE_SUBSCRIPTION_LAUNCH);
			logger.error("launch case farmid:"+cFarmId+", virtual machine number is 0");
			return startResult;
		}
		vp.setTotalInstance(totalInstance);
		ResultObject robj = loginService.login(null, null);
		//request headers
		Map<String,String> headerMap = new HashMap<String,String>();
		headerMap.put("X-Secure-Key", robj.getSecureKey());
		headerMap.put("X-Requested-Token", robj.getSpecialToken());
		vp.setxSecurityKey(robj.getSecureKey());
		vp.setSpecialToken(robj.getSpecialToken());
		vp.setDestinationFarmId(String.valueOf(cFarmId));
		//list servers check if server number bigger than 0
		CloseableHttpResponse qRes = null;
		String queryUrl = configuration.getMirBaseUrl()+"/mir/proxy/servers/xListServers/?farmId="+cFarmId+"&imageId=&limit=10&page=1&query=&start=0";
		String sInfoJson =null;
		try {
			qRes = MSUtil.httpClientGetUrl(headerMap, queryUrl);
			sInfoJson = EntityUtils.toString(qRes.getEntity());
		} catch (Exception e) {
			logger.error("when do launch case,list servers failed. task id : "+taskStack.getId()+",farm id:"+cFarmId);
			unlockService.unlockMission(taskStack);
			e.printStackTrace();
		}
		try {
			qRes.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Servers servers = null;
		try {
			servers = wFactory.getEntity(Servers.class, sInfoJson);
		}catch (Exception e) {
			logger.error("when launch case, convert servers info to json failed,farm id:"+cFarmId+"  "+e.getLocalizedMessage());
			String listServersResult = WhiteholeFactory.getFailedMsg(params, "处理失败，转换虚拟机清单信息成JSON格式失败", CaseProvider.EVENT_TYPE_SUBSCRIPTION_LAUNCH);
			e.printStackTrace();
			return listServersResult;
		}
		if(Integer.parseInt(servers.getTotal()) > 0){
			String result = WhiteholeFactory.getFailedMsg(params,"处理失败,已经启动过该应用堆栈。",CaseProvider.EVENT_TYPE_SUBSCRIPTION_LAUNCH);
			return result;
		}
		
		//service start
		logger.info("=============begin launch farm===================");
		String farmStartUrl = configuration.getMirBaseUrl()+"/mir/proxy/farms/xLaunch";
		List<NameValuePair> params_list_2 = new ArrayList<NameValuePair>();
		vp.setcFarmId(cFarmId);
		vp.setBeginTime(new Date().getTime());
		vp.setTaskId(taskStack.getId());
		vp.setEnventId(params.getData().getEventId());
		params_list_2.add(new BasicNameValuePair("farmId",String.valueOf(cFarmId)));
		CloseableHttpResponse startFarm = null;
		try {
			startFarm = MSUtil.httpClientPostUrl(headerMap, farmStartUrl, params_list_2);
		} catch (Exception e) {
			logger.error("launch case,start clone farm failed,cloned farm id:"+cFarmId);
			String result = WhiteholeFactory.getFailedMsg(params,"处理失败,原因是启动克隆应用堆栈失败。",CaseProvider.EVENT_TYPE_SUBSCRIPTION_LAUNCH);
			e.printStackTrace();
			return result;
		}
		try {
			startResult = EntityUtils.toString(startFarm.getEntity());
			logger.info("launch case,farm id:"+cFarmId+",mir launch result:"+startResult);
		} catch (Exception e1) {
			logger.error("launch case,convert start farm entity to string failed");
			e1.printStackTrace();
		} 
		try {
			startFarm.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return startResult;
	}
	
	public String suspendService(Params params,String cFarmId,TaskStack taskStack){
		String result = null;
		logger.info("=====================挂起事件,farm ID: "+cFarmId+"===============================");
		WhiteholeFactory wFactory = new WhiteholeFactory();
		Map<String,String> headerMap = new HashMap<String,String>();
		//login
		ResultObject robj= loginService.login(null,null);
		if(!robj.getErrorMessage().equals("") && !robj.isSuccess()){
			String loginResult = WhiteholeFactory.getFailedMsg(params, "处理失败,原因是登录mir系统失败。", CaseProvider.EVENT_TYPE_SUBSCRIPTION_SUSPEND);
			logger.error("suspend case,login mir plateform failed. farm id:"+cFarmId);
			return loginResult;
		}
		headerMap.put("X-Secure-Key", robj.getSecureKey());
		headerMap.put("X-Requested-Token", robj.getSpecialToken());
		boolean b = virtualMachineStatusCheck.isAllInOneStatus(cFarmId, robj.getSecureKey(), robj.getSpecialToken(), CaseProvider.EVENT_TYPE_SUBSCRIPTION_SUSPEND, taskStack.getId(), CaseProvider.VM_STATUS_RUNNING);
		if(!b){
			logger.error("suspend case,have a virtual machine is not running status. farm id:"+cFarmId);
			return WhiteholeFactory.getFailedMsg(params, "处理失败,原因是存在虚拟机不是运行状态。", CaseProvider.EVENT_TYPE_SUBSCRIPTION_SUSPEND);
		}
		//list servers
		CloseableHttpResponse qRes = null;
		String queryUrl = configuration.getMirBaseUrl()+"/mir/proxy/servers/xListServers/?farmId="+cFarmId+"&imageId=&limit=10&page=1&query=&start=0";
		String sInfoJson =null;
		try {
			qRes = MSUtil.httpClientGetUrl(headerMap, queryUrl);
			sInfoJson = EntityUtils.toString(qRes.getEntity());
			logger.debug("when suspend case, get vm info:"+sInfoJson);
		} catch (Exception e) {
			logger.error("when do suspend case,list servers failed. farm id:"+cFarmId);
			unlockService.unlockMission(taskStack);
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
			logger.error("when suspend case, convert servers info to json failed,farm id:"+cFarmId+"  "+e.getLocalizedMessage());
			String listServersResult = WhiteholeFactory.getFailedMsg(params, "处理失败，转换虚拟机清单信息成JSON格式失败", CaseProvider.EVENT_TYPE_SUBSCRIPTION_SUSPEND);
			e.printStackTrace();
			return listServersResult;
		}
		if(0 == Integer.parseInt(servers.getTotal())){
			String listServersResult = WhiteholeFactory.getFailedMsg(params, "处理失败，虚拟机数量为0，请联系管理员", CaseProvider.EVENT_TYPE_SUBSCRIPTION_SUSPEND);
			logger.error("suspend case farmid:"+cFarmId+", virtual machine number is 0");
			return listServersResult;
		}
		String suspendUrl = configuration.getMirBaseUrl()+"/mir/proxy/servers/xSuspendServers";
		int failedNumber = 0;
		for (ServerInfo s : servers.getData()) {
			List<NameValuePair> params_list = new ArrayList<NameValuePair>();
			logger.debug("server id :"+"["+s.getServer_id()+"]");
			params_list.add(new BasicNameValuePair("servers","[\""+s.getServer_id()+"\"]"));
			logger.debug("the params_list info:"+params_list.toString());
			CloseableHttpResponse suspendR = null;
			try {
				suspendR = MSUtil.httpClientPostUrl(headerMap, suspendUrl, params_list);
				String resultBuffer = EntityUtils.toString(suspendR.getEntity());
				logger.info("suspend case, result:"+resultBuffer);
				ObjectMapper mapper = new ObjectMapper();
				JsonNode node = mapper.readTree(resultBuffer);
				boolean isSuccess = node.get("success").asBoolean();
				if(!isSuccess){
					failedNumber++;
				}
			} catch (Exception e) {
				logger.error("when suspend case, request mir to suspend vitrual machine failed， farm id:"+cFarmId+"the virtual machine server id:"+s.getServer_id());
				String listServersResult = WhiteholeFactory.getFailedMsg(params, "处理失败，挂起虚拟机失败", CaseProvider.EVENT_TYPE_SUBSCRIPTION_SUSPEND);
				e.printStackTrace();
				return listServersResult;
			}
			try {
				suspendR.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(failedNumber > 0){
			result =  WhiteholeFactory.getFailedMsg(params, "处理失败，存在至少一个虚拟机挂起失败", CaseProvider.EVENT_TYPE_SUBSCRIPTION_SUSPEND);
		}else{
			result = WhiteholeFactory.getSuccessMsg(params, CaseProvider.EVENT_TYPE_SUBSCRIPTION_SUSPEND);
		}
		return result;
	}
	
	public String cancleService(Params p,String cFarmId,VMQeuryParam vp,TaskStack taskStack){
		String result = null;
		WhiteholeFactory wFactory = new WhiteholeFactory();
		logger.debug("=====================注销事件 farmId:"+cFarmId+"====================");
		//add destination farm id
		vp.setDestinationFarmId(String.valueOf(cFarmId));
		//create mir request url
		String listSSHkeyUrl = configuration.getMirBaseUrl()+"/mir/proxy/sshkeys/xListSshKeys?farmId="+cFarmId+"&page=1&start=0";
		String terminateFarmUrl = configuration.getMirBaseUrl()+"/mir/proxy/farms/xTerminate";
		String removeSSHKeyUrl = configuration.getMirBaseUrl()+"/mir/proxy/sshkeys/xRemove";
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
					if(jsonNode.get("farm_id").asText().equals(cFarmId)){
						logger.debug("get the ssk id----->"+jsonNode.get("id").toString());
						sshKeyId = jsonNode.get("id").toString();
					};
				}
			}
			response.close();
			
		} catch (Exception e1) {
			logger.error("when do cancle case,get ssh key failed\n"+e1.getLocalizedMessage());
			unlockService.unlockMission(taskStack);
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
		if(null == sshKeyId){
			logger.warn("the ssh key id is empty,farm id:"+cFarmId);
			result = "SSH key(s) successfully removed";
			//String resultCancleCase = WhiteholeFactory.getFailedMsg(p,"处理失败,原因是获取应用堆栈的SSH KEY失败。",CaseProvider.EVENT_TYPE_SUBSCRIPTION_CANCEL);
			//return resultCancleCase;
		}else{
			try {
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
				logger.error("cancle case,remove farm ssh key failed，farm id:"+cFarmId+"  "+e.getLocalizedMessage());
				String resultCancleCase = WhiteholeFactory.getFailedMsg(p,"处理失败,原因是删除应用堆栈SSH KEY 异常。",CaseProvider.EVENT_TYPE_SUBSCRIPTION_CANCEL);
				e.printStackTrace();
				return resultCancleCase;
			}
		}
		//remove farm stack move to mission queue
		return result;
	}
	
	public String activeService(Params p,String cFarmId,VMQeuryParam vp,TaskStack taskStack){
		logger.info("=====================激活事件,farm ID: "+cFarmId+"===============================");
		String result = null;
		WhiteholeFactory wFactory = new WhiteholeFactory();
		Map<String,String> headerMap = new HashMap<String,String>();
		vp.setDestinationFarmId(String.valueOf(cFarmId));
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
		boolean b = virtualMachineStatusCheck.isAllInOneStatus(cFarmId, robj.getSecureKey(), robj.getSpecialToken(), CaseProvider.EVENT_TYPE_SUBSCRIPTION_ACTIVE, taskStack.getId(), CaseProvider.VM_STATUS_SUSPENEDED);
		if(!b){
			logger.error("active case, have a vitrual machine is not suspend status, farm id:"+cFarmId);
			return WhiteholeFactory.getFailedMsg(p, "处理失败,有虚拟机存在非挂起状态。", CaseProvider.EVENT_TYPE_SUBSCRIPTION_ACTIVE);
		}
		//list servers
		CloseableHttpResponse qRes = null;
		String queryUrl = configuration.getMirBaseUrl()+"/mir/proxy/servers/xListServers/?farmId="+cFarmId+"&imageId=&limit=10&page=1&query=&start=0";
		String sInfoJson =null;
		try {
			qRes = MSUtil.httpClientGetUrl(headerMap, queryUrl);
			sInfoJson = EntityUtils.toString(qRes.getEntity());
		} catch (Exception e) {
			logger.error("when do active case,list servers failed. farm id:"+cFarmId);
			unlockService.unlockMission(taskStack);
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
		String activeUrl = configuration.getMirBaseUrl()+"/mir/proxy/servers/xResumeServers";
		int failedNumber = 0 ;
		for (ServerInfo s : servers.getData()) {
			List<NameValuePair> params_list = new ArrayList<NameValuePair>();
			params_list.add(new BasicNameValuePair("servers","[\""+s.getServer_id()+"\"]"));
			CloseableHttpResponse activeR = null;
			try {
				activeR = MSUtil.httpClientPostUrl(headerMap, activeUrl, params_list);
				String resultBuffer = EntityUtils.toString(activeR.getEntity());
				ObjectMapper mapper = new ObjectMapper();
				JsonNode node = mapper.readTree(resultBuffer);
				boolean isSuccess = node.get("success").asBoolean();
				if(!isSuccess){
					failedNumber++;
				}
				count++;
			} catch (Exception e) {
				logger.error("when active vitrual case, request mir to suspend vitrual machine failed， farm id:"+cFarmId);
				String listServersResult = WhiteholeFactory.getFailedMsg(p, "处理失败，挂机虚拟机失败", CaseProvider.EVENT_TYPE_SUBSCRIPTION_ACTIVE);
				e.printStackTrace();
				return listServersResult;
			}
			try {
				activeR.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(0 != total){ 
			if(total == count){
				if(failedNumber > 0){
					result = WhiteholeFactory.getFailedMsg(p, "处理失败，存在一个虚拟机激活失败", CaseProvider.EVENT_TYPE_SUBSCRIPTION_ACTIVE);
				}else{
					result = CaseProvider.ACTIVE_FIRST_STEP;
				}
			}else{
				logger.warn("active number less than total");
				result = WhiteholeFactory.getFailedMsg(p, "处理失败，原因是激活虚拟机数量异常", CaseProvider.EVENT_TYPE_SUBSCRIPTION_ACTIVE);
			}
		}else{
			result = WhiteholeFactory.getFailedMsg(p, "处理失败，原因是虚拟机数量为0，请联系管理员", CaseProvider.EVENT_TYPE_SUBSCRIPTION_ACTIVE);
			logger.error("active case farmid:"+cFarmId+", virtual machine number is 0");
		}
		return result;
	}
	
	public String queryService(Params p){
		String result = null;
		
		ArrayList<com.chinacloud.isv.entity.callbackparams.Component> att_list = new ArrayList<com.chinacloud.isv.entity.callbackparams.Component>();
		String instanceId = p.getData().getPayload().getInstance().getInstanceId();
		logger.info("=====================查询事件,实例 ID: "+instanceId+"===============================");
		logger.info("QUERY　CASE: the instance id---->"+instanceId);
		logger.info("test the  taskResultDao--->"+taskResultDao==null);
		TaskResult tr = taskResultDao.getOrderTaskResultById(instanceId);
		if(null == tr){
			logger.error("when do query case,get clone farm id failed because of database return null");
			result = WhiteholeFactory.getFailedMsg(p, "处理失败,原因是获取不到对应实例的处理结果,请联系管理员", CaseProvider.EVENT_TYPE_SUBSCRIPTION_QUERY);
			result = MSUtil.encode(result);
			return result;
		}
		//task result the order mission is time out
		if(tr.getInfo().contains(CaseProvider.STATUS_TIME_OUT)){
			logger.error("task result id:"+tr.getId()+",farm id:"+tr.getcFarmId()+"task time out");
		}
		
		Map<String,String> headerMap = new HashMap<String,String>();
		//login
		ResultObject robj= loginService.login(null,null);
		if(!robj.getErrorMessage().equals("") && !robj.isSuccess()){
			String loginResult = WhiteholeFactory.getFailedMsg(p, "处理失败,原因是登录mir系统失败。", CaseProvider.EVENT_TYPE_SUBSCRIPTION_QUERY);
			logger.error("login mir plateform failed");
			return loginResult;
		}
		headerMap.put("X-Secure-Key", robj.getSecureKey());
		headerMap.put("X-Requested-Token", robj.getSpecialToken());
		//get edit info 
		String editInfo = mirEditService.getFarmEditInfo(tr.getcFarmId(), robj);
		if(null == editInfo){
			logger.error("query case,get edit info failed");
			result = WhiteholeFactory.getFailedMsg(p,  "处理失败,原因是查询应用堆栈信息请求失败。", CaseProvider.EVENT_TYPE_SUBSCRIPTION_QUERY);
			return result;
		}
		String [] keys = {"farm","roles"};
		String [] fkeys = {"farm","farm","name"};
		JsonNode jNode = MSUtil.getDirectedValueFromJson(keys, editInfo);
		JsonNode fNode = MSUtil.getDirectedValueFromJson(fkeys, editInfo);
		MSUtil.getComponentsList(jNode.toString(), att_list);
		DataExtend data = new DataExtend();
		ProcessExtend process = new ProcessExtend();
		data.setSuccess(true);
		data.setMessage(MSUtil.getChineseName(CaseProvider.EVENT_TYPE_SUBSCRIPTION_QUERY)+"处理成功");
		process.setEventId(p.getData().getEventId());
		process.setInstanceId(tr.getId());
		process.setStatus(CaseProvider.SUCESS_STATUS);
		process.setExtensionUrl(configuration.getMirMoreOperateUrl()+"/"+tr.getcFarmId());
		HashMap<String, Object> metadata = new HashMap<>();
		metadata.put("system", "Mir");
		process.setMetadata(metadata);
		data.setProcess(process);
		if(null != att_list){
			HashMap<String, Object> map = new HashMap<>();
			map.put("farmName", fNode.toString());
			map.put("farmId", tr.getcFarmId());
			map.put("componentInfo", att_list);
			Instance instance = new Instance();
			instance.setMetadata(map);
			process.setInstance(instance);
		}
		try {
			result = WhiteholeFactory.getJsonString(data);
		} catch (JsonProcessingException e) {
			logger.error("convert failed info to json failed \n"+e.getLocalizedMessage());
			return WhiteholeFactory.getFailedMsg(p, "处理失败,原因是转换成功信息为json失败。", CaseProvider.EVENT_TYPE_SUBSCRIPTION_QUERY);
		}
		return result;
		
	}
	
	public String rebootService(String cFarmId,Params p,TaskStack taskStack,VMQeuryParam vp){
		String allRebootResult = null;
		logger.info("=====================重启事件,farm ID: "+cFarmId+", task id:"+taskStack.getId()+"===============================");
		String rebootUrl =configuration.getMirBaseUrl()+"/mir/proxy/servers/xServerRebootServers";
		String queryUrl = configuration.getMirBaseUrl()+"/mir/proxy/servers/xListServers/?farmId="+cFarmId+"&imageId=&limit=10&page=1&query=&start=0";
		vp.setDestinationFarmId(String.valueOf(cFarmId));
		WhiteholeFactory wFactory = new WhiteholeFactory();
		Map<String,String> headerMap = new HashMap<String,String>();
		ResultObject robj= loginService.login(null,null);
		if(!robj.getErrorMessage().equals("") && !robj.isSuccess()){
			String loginResult = WhiteholeFactory.getFailedMsg(p, "处理失败,原因是登录mir系统失败。", CaseProvider.EVENT_TYPE_SUBSCRIPTION_QUERY);
			logger.error("login mir plateform failed");
			return loginResult;
		}
		headerMap.put("X-Secure-Key", robj.getSecureKey());
		headerMap.put("X-Requested-Token", robj.getSpecialToken());
		vp.setxSecurityKey(robj.getSecureKey());
		vp.setSpecialToken(robj.getSpecialToken());
		CloseableHttpResponse qResult = null;
		try {
			qResult = MSUtil.httpClientGetUrl(headerMap, queryUrl);
		} catch (Exception e1) {
			logger.error("reboot case,when get farm servers info list ,request mir plate failed. errorMsg:"+e1.getLocalizedMessage());
			unlockService.unlockMission(taskStack);
			e1.printStackTrace();
		}
		String serverInfo = null;
		try {
			serverInfo = EntityUtils.toString(qResult.getEntity());
		} catch (Exception e) {
			logger.error("reboot case,convert server entity to string failed.task id: "+taskStack.getId()+" errorMsg:"+e.getLocalizedMessage());
			e.printStackTrace();
		}
		Servers s = null;
		try {
			s= wFactory.getEntity(Servers.class, serverInfo);
		} catch (Exception e1) {
			logger.error("reboot case,convert server info string to object failed.task id: "+taskStack.getId()+" errorMsg:"+e1.getLocalizedMessage());
			e1.printStackTrace();
		} 
		int flagNotRunning = 0;
		if(0 == Integer.parseInt(s.getTotal())){
			allRebootResult = WhiteholeFactory.getFailedMsg(p, "处理失败，原因是虚拟机数量为0，请联系管理员", CaseProvider.EVENT_TYPE_SUBSCRIPTION_REBOOT);
			logger.error("suspend case farmid:"+cFarmId+", virtual machine number is 0");
			return allRebootResult;
		}
		ArrayList<ServerInfo> sList = s.getData();
		for (int i = 0 ;i < Integer.parseInt(s.getTotal()); i++) {
			if(!sList.get(i).getStatus().equals(CaseProvider.VM_STATUS_RUNNING)){
				flagNotRunning++;
			}
		}
		if(flagNotRunning > 0){
			logger.warn("reboot case,there is a virtual machine have not running status");
			String result = WhiteholeFactory.getFailedMsg(p,  "处理失败,原因是对应应用堆栈的虚拟机有存在非运行状态。", CaseProvider.EVENT_TYPE_SUBSCRIPTION_REBOOT);
			return result;
		}
		
		try {
			qResult.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//reboot all virtual machine
		for (int i = 0 ;i < Integer.parseInt(s.getTotal()); i++) {
			String servers = "[";
			servers = servers+"\""+sList.get(i).getServer_id()+"\"]";
			List<NameValuePair> params_list = new ArrayList<NameValuePair>();
			params_list.add(new BasicNameValuePair("servers",servers));
			params_list.add(new BasicNameValuePair("type","soft"));
			CloseableHttpResponse rebootR = null;
			try {
				rebootR = MSUtil.httpClientPostUrl(headerMap, rebootUrl, params_list);
			} catch (Exception e1) {
				logger.error("reboot case,task id: "+taskStack.getId()+" reboot request failed,errorMsg:"+e1.getLocalizedMessage());
				unlockService.unlockMission(taskStack);
				e1.printStackTrace();
			}
			String rebootResult = null;
			try {
				rebootResult = EntityUtils.toString(rebootR.getEntity());
				logger.info("reboot case,reponse result:"+rebootResult);
				if(rebootResult.contains("\"success\":false")){
					allRebootResult = WhiteholeFactory.getFailedMsg(p, "处理失败，原因是有一个虚拟机重启失败", CaseProvider.EVENT_TYPE_SUBSCRIPTION_REBOOT);
					break;
				}else{
					allRebootResult = CaseProvider.REBOOT_FIRST_STEP;
				}
			} catch (Exception e) {
				logger.error("reboot case,convert reboot result entity to string failed. task id: "+taskStack.getId()+" errorMsg:"+e.getLocalizedMessage());
				e.printStackTrace();
			} 
		}
		return allRebootResult;
	}
	
	public boolean removeCloneFarm(String cFarmId,String sercurity,String token){
		boolean b = true;
		Map<String,String> headerMap = new HashMap<String,String>();
		headerMap.put("X-Secure-Key", sercurity);
		headerMap.put("X-Requested-Token", token);
		String removeFarmUrl = configuration.getMirBaseUrl()+"/mir/proxy/farms/xRemove";
		List<NameValuePair> params_list = new ArrayList<NameValuePair>();
		logger.warn("delete farm the farm id :"+cFarmId);
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
