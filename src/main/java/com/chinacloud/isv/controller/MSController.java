package com.chinacloud.isv.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chinacloud.isv.domain.RiskStack;
import com.chinacloud.isv.persistance.RiskStackDao;

@RestController
public class MSController {
	@Autowired
	private RiskStackDao riskStackDao;
	
	@RequestMapping("/event_request")
	public void eventRequest(@RequestParam String url){
		RiskStack rStack = new RiskStack();
		rStack.setRequestUrl("www.baidu.com");
		rStack.setCallBackUrl("www.tiger.test");
		rStack.setParams("{hello}");
		rStack.setId("asdfadflkjl");
		rStack.setRequestMethod("post");
		riskStackDao.addRisk(rStack);
	}

}
