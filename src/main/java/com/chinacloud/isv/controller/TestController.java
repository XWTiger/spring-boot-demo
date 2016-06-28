package com.chinacloud.isv.controller;


import java.io.IOException;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.chinacloud.isv.component.VtrualMachineQuery;
import com.chinacloud.isv.entity.VMQeuryParam;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;



@RestController
public class TestController {
	private static final Logger logger = LogManager.getLogger(TestController.class);
	private VMQeuryParam VMQeuryParam;
	@Autowired
	VtrualMachineQuery vtrualMachineQuery;
	String Json = "{ \"status\": 200,\"data\": {\"type\": \"SUBSCRIPTION_ORDER\",\"eventId\": \"324e8a16-6b06-465b-84c1-be762dd9fea0\",\"marketplace\": {\"baseUrl\": \"http://www.baidu.com\",\"partner\": \"whitehole\"},\"creator\": {\"id\": \"cfc46de5-05c4-4774-aae6-6839d03113cb\",\"email\": \"zjw186@qq.com\",\"firstName\": \"zhang\",\"lastName\": \"san\"},\"payload\": {\"tenant\": {\"name\": \"whitehole_tenant\",\"id\": \"ee32f443-6c65-4c0b-a0ae-d69fd92eeb56\"},\"order\": {\"editionCode\": \"m.tiny\"}}, \"callBackUrl\": \"http://172.16.80.170:8080/business/order/tasks/callback\"}}";
	
	String json2 = "{\"success\":true,\"total\":\"3\",\"data\":[{\"clientid\":\"1\",\"id\":\"922\",\"name\":\"mir-pack-deploy-test (clone #2)\",\"status\":\"0\",\"dtadded\":\"Jun 1, 2016 13:34:09\",\"created_by_id\":\"99\",\"created_by_email\":\"xiaweihu@chinacloud.com.cn\",\"running_servers\":\"0\",\"suspended_servers\":\"0\",\"non_running_servers\":\"0\",\"roles\":\"2\",\"zones\":\"0\",\"alerts\":\"0\",\"lock\":null,\"havemysqlrole\":false,\"havemysql2role\":false,\"havepgrole\":false,\"haveredisrole\":false,\"haverabbitmqrole\":false,\"havemongodbrole\":false,\"haveperconarole\":false,\"havemariadbrole\":false,\"status_txt\":\"Terminated\",\"shortcuts\":[]}]}";
	
	
	@RequestMapping(value="/callback",method=RequestMethod.POST)
	@ResponseBody
	public String greeting(){
		logger.info("----------hello world------------");
		logger.error("-------------error test--------------");
		ObjectMapper mapper = new ObjectMapper();
		VMQeuryParam vParam = new VMQeuryParam();
		vParam.setCallbackUrl("asdfasdf");
		vParam.setcFarmId(110);
		vParam.setBeginTime(new Date().getTime());
		VMQeuryParam = vParam;
		vtrualMachineQuery.addQueryTask(vParam);
		vtrualMachineQuery.start();
		try {
			JsonNode node = mapper.readTree(json2);
			JsonNode node2 = node.get("data");
			if(node2.isArray()){
				for (JsonNode jsonNode : node2) {
					System.out.println(jsonNode.get("name").toString());
				}
			}
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Json;
	}
	
	@RequestMapping("/test_exception")
	public void exceptionTest(){
		/*logger.info("----------what hanppend------------");
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
		}*/
		
			//throw new IllegalArgumentException("dadsf");
		vtrualMachineQuery.removeQueryTask(VMQeuryParam);
	}

}
