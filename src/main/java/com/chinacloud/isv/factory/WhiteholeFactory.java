package com.chinacloud.isv.factory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.chinacloud.isv.entity.Params;
import com.chinacloud.isv.entity.callbackparams.Data;
import com.chinacloud.isv.entity.callbackparams.Process;
import com.chinacloud.isv.util.CaseProvider;
import com.chinacloud.isv.util.MSUtil;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class WhiteholeFactory {
	private static final Logger logger = LogManager.getLogger(WhiteholeFactory.class);
	
	/**
	 * convert json string to instance
	 * @param Class 
	 * @return
	 * @author Tiger
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	@SuppressWarnings("unchecked")
	public <T> T  getEntity(Class<?> obj,String params) throws JsonParseException, JsonMappingException, IOException{
		if(null == params || "".equals(params) ){
			logger.error("the json string is empty");
			return null;
		}else{
			String cTail = this.getClassName(obj.getName());
			if(cTail.contains("Params")){
				//add analyze method
			}else if(cTail.contains("CallbackParams")){
				
			}
			ObjectMapper om = new ObjectMapper();
			return (T) om.readValue(params, obj);
		}
		
	}
	
	
	public static String getJsonString(Object obj) throws JsonProcessingException{
		ObjectMapper om = new ObjectMapper();
		return om.writeValueAsString(obj);
	}
	
	
	
	private String  getClassName(String fullName){
		StringBuilder stringBuilder = new StringBuilder(fullName);
		String cTail = stringBuilder.substring(stringBuilder.lastIndexOf(".")+1,stringBuilder.length());
		return cTail;
	}
	/**
	 * return asyn message
	 * @param eventId
	 * @param caseName
	 * @return
	 * @throws JsonProcessingException
	 */
	public static String getAsynReturnJson(String eventId,String caseName) throws JsonProcessingException{
		String jsonObj = null;
		Data data = new Data();
		data.setSuccess(true);
		com.chinacloud.isv.entity.callbackparams.Process process = new com.chinacloud.isv.entity.callbackparams.Process();
		process.setEventId(eventId);
		process.setStatus(CaseProvider.EVENT_TYPE_WAIT_FOR_RESULT);
		String name = MSUtil.getChineseName(caseName);
		if(null == name){
			logger.warn("the case name "+caseName+" don't exist");
		}
		data.setMessage(name+CaseProvider.EVENT_TYPE_WAIT_FOR_RESULT_MESSAGE);
		data.setProcess(process);
		jsonObj = getJsonString(data);
		return jsonObj;
	}
	
	/**
	 * get call back failed result
	 * @param p
	 * @param type
	 * @return string
	 */
	public static String getFailedMsg(Params p,String msgTail,String caseType){
		Data data = new Data();
		Process process = new Process();
		String result = null;
		data.setSuccess(false);
		data.setErrorCode("10001");
		//data.setMessage(MSUtil.getChineseName(CaseProvider.EVENT_TYPE_SUBSCRIPTION_CANCEL)+"处理失败,原因是删除应用堆栈SSH KEY 失败。");
		data.setMessage(MSUtil.getChineseName(caseType)+msgTail);
		process.setEventId(p.getData().getEventId());
		process.setInstanceId(p.getData().getPayload().getInstance().getInstanceId());
		process.setStatus("FAILED");
		data.setProcess(process);
		try {
			result = WhiteholeFactory.getJsonString(data);
		} catch (JsonProcessingException e) {
			logger.error("convert failed info to json failed \n"+e.getLocalizedMessage());
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * get call back success result
	 * @param p
	 * @param type
	 * @return string
	 */
	public static String getSuccessMsg(Params p,String type){
		Data data = new Data();
		Process process = new Process();
		String result = null;
		data.setSuccess(true);
		//data.setMessage(MSUtil.getChineseName(CaseProvider.EVENT_TYPE_SUBSCRIPTION_CANCEL)+"处理失败,原因是删除应用堆栈SSH KEY 失败。");
		data.setMessage(MSUtil.getChineseName(type)+"处理成功");
		process.setEventId(p.getData().getEventId());
		process.setInstanceId(p.getData().getPayload().getInstance().getInstanceId());
		process.setStatus("SUCCESS");
		data.setProcess(process);
		try {
			result = WhiteholeFactory.getJsonString(data);
		} catch (JsonProcessingException e) {
			logger.error("convert failed info to json failed \n"+e.getLocalizedMessage());
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * get farmId by parameters
	 * @param p
	 * @return bigger than 0 is valid value
	 */
	public static int getFarmId(Params p){
		int id = 0;
		String value = p.getData().getPayload().getOrder().getEditionCode();
		int index = value.indexOf("farmId");
		int begin = index+9;
		int end = 0;
		System.out.println("begin:"+begin);
		for(int i = begin ; i < value.length() - 1 ; i++){
			if(value.substring(i, i+1).equals("\"")){
				end = i;
				break;
			}
		}
		id = Integer.parseInt(value.substring(begin, end));
		return id;
	}
	/**
	 * call back return the result of whitehole
	 * @param result
	 * @param params
	 * @return
	 */
	public static CloseableHttpResponse callBackReturnResult(String result,Params params){
		CloseableHttpResponse response = null;
		Map<String,String> map = new HashMap<String,String >();
		map.put("Content-Type", "application/json");
		try {
			String newResult = MSUtil.encode(result);
			response = MSUtil.httpClientPostUrl(map, params.getData().getCallBackUrl(), newResult);
		} catch (Exception e) {
			logger.error("call back return result failed");
			e.printStackTrace();
		}
		return response;
	}
}
