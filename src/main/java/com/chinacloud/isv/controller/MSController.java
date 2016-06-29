package com.chinacloud.isv.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chinacloud.isv.service.MirRequestService;

@RestController
public class MSController {

	@Autowired
	MirRequestService mirRequestService;
	@RequestMapping(value="/event_request",produces = {"application/json;charset=UTF-8"})
	public String eventRequest(@RequestParam String url){
		System.out.println("this is callback url ---->"+url);
		return mirRequestService.sendRequest(url);
	}

}
