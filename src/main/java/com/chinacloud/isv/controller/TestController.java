package com.chinacloud.isv.controller;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class TestController {
	private static final Logger logger = LogManager.getLogger(TestController.class);
	
	@RequestMapping("/greeting")
	public String greeting(){

		logger.info("----------hello world------------");
		return "hello";
	}
	
	@RequestMapping("/test_exception")
	public void exceptionTest(){
		logger.info("----------what hanppend------------");
			throw new IllegalArgumentException("dadsf");
		
	}

}
