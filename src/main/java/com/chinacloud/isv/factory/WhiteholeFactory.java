package com.chinacloud.isv.factory;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.chinacloud.isv.entity.callbackparams.Data;
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
}
