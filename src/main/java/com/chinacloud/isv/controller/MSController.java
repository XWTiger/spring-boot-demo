package com.chinacloud.isv.controller;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
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
	private Logger logger = LogManager.getLogger(MSController.class);
	
	@RequestMapping(value="/isv/event_request",produces = {"application/json;charset=UTF-8"})
	public String eventRequest(@RequestParam(value="url",required=false) String url){
		System.out.println("this is callback url ---->"+url);
		return mirRequestService.sendRequest(url);
	}
	
	@ResponseBody
	@RequestMapping(value="/isv/orders",method=RequestMethod.GET)
	public HashMap<Object, Object> getOrderRecordList(
			@RequestParam(value="service_template_id",required=false) String serviceId,
			@RequestParam(value="farm_id",required=false)String farmId,
			@RequestParam(value="page",required=false)Integer page, 
	        @RequestParam(value="page_size",required=false)Integer pageSize,
			@RequestParam(value="order_by", required=false)String orderBy,
			@RequestParam(value="order", required=false) String order){
		logger.info("=======query order service instance====serviece id:"+serviceId);
		return orderRecordService.getRecordList(serviceId,farmId, page, pageSize, orderBy, order);
	}
	
	@ResponseBody
	@RequestMapping(value="/isv/instances",method=RequestMethod.POST)
	public HashMap<Object, Object>  getServiceTemplateInstanceNum(@RequestBody HashMap<String, Object> templatIdList){
		@SuppressWarnings("unchecked")
		ArrayList<String> list =  (ArrayList<String>) templatIdList.get("templatIdList");
		if(null == list){
			throw new IllegalArgumentException("服务模板ID为空");
		}
		String[] tlist = new String[list.size()];
		int i = 0;
		for (String string : list) {
			tlist[i] = string;
			i++;
			logger.debug("id:"+string);
		}
		return orderRecordService.getSTNumber(tlist);
	}
}
