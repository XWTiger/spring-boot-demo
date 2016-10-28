package com.chinacloud.isv.controller;


import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.chinacloud.isv.component.VtrualMachineQuery;
import com.chinacloud.isv.entity.Params;
import com.chinacloud.isv.entity.VMQeuryParam;
import com.chinacloud.isv.factory.WhiteholeFactory;
import com.chinacloud.isv.service.ConfigurateFarmService;
import com.chinacloud.isv.service.LoginService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;



@RestController
public class TestController {
	private static final Logger logger = LogManager.getLogger(TestController.class);
	@SuppressWarnings("unused")
	private VMQeuryParam VMQeuryParam;
	@Autowired
	LoginService loginService;
	@Autowired
	VtrualMachineQuery vtrualMachineQuery;
	@Autowired
	ConfigurateFarmService configurateFarmService;
	
	String Json = "{ \"status\": 200,\"data\": {\"type\": \"SUBSCRIPTION_ORDER\",\"eventId\": \"324e8a16-6b06-465b-84c1-be762dd9fea0\",\"marketplace\": {\"baseUrl\": \"http://www.baidu.com\",\"partner\": \"whitehole\"},\"creator\": {\"id\": \"cfc46de5-05c4-4774-aae6-6839d03113cb\",\"email\": \"zjw186@qq.com\",\"firstName\": \"zhang\",\"lastName\": \"san\"},\"payload\": {\"tenant\": {\"name\": \"whitehole_tenant\",\"id\": \"ee32f443-6c65-4c0b-a0ae-d69fd92eeb56\"},\"order\": {\"editionCode\": \"m.tiny\"}}, \"callBackUrl\": \"http://172.16.80.170:8080/business/order/tasks/callback\"}}";
	
	String json2 = "{\"success\":true,\"total\":\"3\",\"data\":[{\"clientid\":\"1\",\"id\":\"922\",\"name\":\"mir-pack-deploy-test (clone #2)\",\"status\":\"0\",\"dtadded\":\"Jun 1, 2016 13:34:09\",\"created_by_id\":\"99\",\"created_by_email\":\"xiaweihu@chinacloud.com.cn\",\"running_servers\":\"0\",\"suspended_servers\":\"0\",\"non_running_servers\":\"0\",\"roles\":\"2\",\"zones\":\"0\",\"alerts\":\"0\",\"lock\":null,\"havemysqlrole\":false,\"havemysql2role\":false,\"havepgrole\":false,\"haveredisrole\":false,\"haverabbitmqrole\":false,\"havemongodbrole\":false,\"haveperconarole\":false,\"havemariadbrole\":false,\"status_txt\":\"Terminated\",\"shortcuts\":[]}]}";
	
	String Json3 = "{\"status\": 200,\"data\": {\"eventId\":\"e956b3b4-11e3-4d54-9e13-5057bd0ad9e5\",\"creator\": {\"firstName\": null,\"lastName\": null,\"id\": \"bfc27112-fedf-413c-9946-4a45319c1a92\",\"email\": \"shengting-admin@test.com\"},\"callBackUrl\": \"http://172.16.80.90:8080/business/cancel/tasks/callback\",\"marketplace\": {\"baseUrl\": \"http://www.baidu.com\",\"partner\": \"whitehole\"},\"payload\": {\"Variables\": {\"metadata\":{\"envId\":\"1\",\"componentInfo\":[{\"componentName\":\"\\\"tomcat\\\"\",\"componentType\":\"\\\"base\\\"\",\"componentInstanceNumber\":\"\\\"1\\\"\",\"componentFarmRoleId\":\"\\\"15\\\"\"}],\"farmId\":\"15\",\"farmName\":\"tomcat-template (clone #3)\"},\"type\":\"Mir\"},\"instance\": {\"instanceId\":\"dfe30815-bd27-4b7d-90b9-432743f10c15\"},\"tenant\": {\"name\": \"ST_5100\",\"id\": \"51000000-0000-0000-0000-000000000000\"}},\"type\": \"SUBSCRIPTION_CANCEL\"}}";
	
	String json4 = "{\"metadata\":{\"envId\":\"1\",\"componentInfo\":[{\"componentName\":\"\\\"tomcat\\\"\",\"componentType\":\"\\\"base\\\"\",\"componentInstanceNumber\":\"\\\"1\\\"\",\"componentFarmRoleId\":\"\\\"15\\\"\"}],\"farmId\":\"15\",\"farmName\":\"tomcat-template (clone #3)\"},\"type\":\"Mir\"},\"instanceId\":\"dfe30815-bd27-4b7d-90b9-432743f10c15\"}";
	
	@RequestMapping(value="/callback",method=RequestMethod.POST)
	@ResponseBody
	public String greeting(){
		logger.info("----------hello world------------");
		logger.error("-------------error test--------------");
		ObjectMapper mapper = new ObjectMapper();
		VMQeuryParam vParam = new VMQeuryParam();
		vParam.setCallbackUrl("asdfasdf");
		vParam.setcFarmId("110");
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
		//String teString="{\"serviceTemplate\": {\"name\": \"2333\",\"description\": \"\",\"type\": \"scalr\",\"id\": \"131\",\"service_template_id\": \"8a7b6634-d2af-4864-a4ec-f5edad45df4c\",\"template_status\": 1,\"create_time\": \"2016-08-18 01:45:50.0\",\"relevant_farm_name\": \"2333\",\"creater_name\": \"mirowner@chinacloud.com.cn\",\"env_id\": \"1\",\"service_template_schema_map\": {\"serviceTemplateName\": \"2333\",\"componentInfo\": [{\"componentName\": \"redhat67-kvm-mysql51-15disk-noauto\",\"componentNet\": [\"1b6cd9ef-9753-4453-b7b1-399e6632c636\"],\"unitFlavorId\": \"vcpus_1-ram_1-disk_15\",\"componentIpPool\": \"\",\"unitInstanceNumber\": \"1\"}],\"farmId\": \"131\",\"serviceTemplateId\": \"8a7b6634-d2af-4864-a4ec-f5edad45df4c\"}}}";
		WhiteholeFactory wFactory = new WhiteholeFactory();
		
			Params mirTemplate;
			try {
				mirTemplate = wFactory.getEntity( Params.class,Json3);
				logger.debug("===================content:============================="+mirTemplate.getData().getPayload().getInstance().getInstanceId());
				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode jsonNode = objectMapper.readTree(json4);
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("Variables", jsonNode);
				logger.debug(objectMapper.writeValueAsString(map));
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
			
	
		
		/*ResultObject ro = loginService.login(null, null);
		MirTemplate mTemplate = new MirTemplate();
		ArrayList<ComponentInfo> aList = new ArrayList<>();
		mTemplate.setFarmId("3");
		mTemplate.setServiceTemplateId("asdfasdf-sddf-adsf-adf");
		ComponentInfo cInfo =new ComponentInfo();
		cInfo.setComponentName("wanghui-20160414-redhat67-kvm-mysql51-15disk");
		//cInfo.setComponentNet("b3fb2933-6422-4ed8-be97-5aaadf3d7e8c");
		cInfo.setUnitFlavorId("1");
		cInfo.setUnitInstanceNumber("1");
		aList.add(cInfo);
		mTemplate.setComponentInfo(aList);
		configurateFarmService.configClonedFarm(mTemplate, "3", ro);*/
	}

}
