package com.chinacloud.isv.controller;


import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.chinacloud.isv.entity.Creator;
import com.chinacloud.isv.entity.Params;
import com.chinacloud.isv.factory.WhiteholeFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;



@RestController
public class TestController {
	private static final Logger logger = LogManager.getLogger(TestController.class);
	
	String Json = "{ \"status\": 200,\"data\": {\"type\": \"SUBSCRIPTION_ORDER\",\"eventId\": \"324e8a16-6b06-465b-84c1-be762dd9fea0\",\"marketplace\": {\"baseUrl\": \"http://www.baidu.com\",\"partner\": \"whitehole\"},\"creator\": {\"id\": \"cfc46de5-05c4-4774-aae6-6839d03113cb\",\"email\": \"zjw186@qq.com\",\"firstName\": \"zhang\",\"lastName\": \"san\"},\"payload\": {\"tenant\": {\"name\": \"whitehole_tenant\",\"id\": \"ee32f443-6c65-4c0b-a0ae-d69fd92eeb56\"},\"order\": {\"editionCode\": \"m.tiny\"}}, \"callBackUrl\": \"http://172.16.80.170:8080/business/order/tasks/callback\"}}";
	
	@RequestMapping(value="/callback",method=RequestMethod.POST)
	@ResponseBody
	public String greeting(){
		logger.info("----------hello world------------");
		logger.error("-------------error test--------------");
		return Json;
	}
	
	@RequestMapping("/test_exception")
	public void exceptionTest(){
		logger.info("----------what hanppend------------");
		WhiteholeFactory wFactor = new WhiteholeFactory();
		Params params = null;
		try {
			params = wFactor.getEntity(Params.class,Json);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("good things --->"+params.getStatus());
		Creator creator = new Creator();
		creator.setEmail("xiaweihu@qq.com");
		creator.setFirstName("tiger");
		try {
			wFactor.getJsonString(creator);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
			//throw new IllegalArgumentException("dadsf");
		
	}

}
