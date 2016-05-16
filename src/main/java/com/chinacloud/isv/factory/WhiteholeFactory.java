package com.chinacloud.isv.factory;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
	
	
	public String getJsonString(Object obj) throws JsonProcessingException{
		ObjectMapper om = new ObjectMapper();
		return om.writeValueAsString(obj);
	}
	
	
	
	private String  getClassName(String fullName){
		StringBuilder stringBuilder = new StringBuilder(fullName);
		String cTail = stringBuilder.substring(stringBuilder.lastIndexOf(".")+1,stringBuilder.length());
		return cTail;
	}
}
