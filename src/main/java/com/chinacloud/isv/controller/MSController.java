package com.chinacloud.isv.controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.chinacloud.isv.service.MirRequestService;
import com.chinacloud.isv.service.OrderRecordService;

@RestController
public class MSController {

	@Autowired
	MirRequestService mirRequestService;
	@Autowired
	OrderRecordService orderRecordService;
	
	@RequestMapping(value="/event_request",produces = {"application/json;charset=UTF-8"})
	public String eventRequest(@RequestParam String url){
		System.out.println("this is callback url ---->"+url);
		return mirRequestService.sendRequest(url);
	}
	
	@ResponseBody
	@RequestMapping(value="/orders",method=RequestMethod.GET)
	public HashMap<Object, Object> getOrderRecordList(
			@RequestParam("service_instance_id") String serviceId,
			@RequestParam("page")int page, 
	        @RequestParam("page_size")int pageSize,
			@RequestParam(value="order_by", required=false)String orderBy,
			@RequestParam(value="order", required=false) String order){
		return orderRecordService.getRecordList(serviceId, page, pageSize, orderBy, order);
	}
}
