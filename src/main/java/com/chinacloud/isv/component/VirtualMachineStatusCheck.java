package com.chinacloud.isv.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.chinacloud.isv.configuration.Configuration;
import com.chinacloud.isv.entity.mir.ServerInfo;
import com.chinacloud.isv.entity.mir.Servers;
import com.chinacloud.isv.factory.WhiteholeFactory;
import com.chinacloud.isv.service.TaskConsumeService;
import com.chinacloud.isv.util.MSUtil;

@Component
public class VirtualMachineStatusCheck {
	private static final Logger logger = LogManager.getLogger(TaskConsumeService.class);
	@Autowired
	Configuration configuration;
	/**
	 * check if all of the virtual machine are the saem status
	 * @param farmId
	 * @param securtyKey
	 * @param specialToken
	 * @param caseType
	 * @param taskId
	 * @param status
	 * @return
	 */
	public boolean isAllInOneStatus(int farmId,String securtyKey,String specialToken,String caseType,String taskId,String status){
		boolean b = true;
		String queryUrl = configuration.getMirConnectUrl()+"servers/xListServers/?farmId="+farmId+"&imageId=&limit=10&page=1&query=&start=0";
		WhiteholeFactory wFactory = new WhiteholeFactory();
		Map<String,String> headerMap = new HashMap<String,String>();
		headerMap.put("X-Secure-Key", securtyKey);
		headerMap.put("X-Requested-Token", specialToken);
		CloseableHttpResponse qResult = null;
		try {
			qResult = MSUtil.httpClientGetUrl(headerMap, queryUrl);
		} catch (Exception e1) {
			logger.error(caseType+"query status,when get farm servers info list ,request mir plate failed. errorMsg:"+e1.getLocalizedMessage());
			b = false;
			e1.printStackTrace();
			return b;
		}
		String serverInfo = null;
		try {
			serverInfo = EntityUtils.toString(qResult.getEntity());
		} catch (Exception e) {
			logger.error("convert server entity to string failed.task id: "+taskId+" errorMsg:"+e.getLocalizedMessage());
			e.printStackTrace();
			b = false;
			return b;
		}
		Servers s = null;
		try {
			s= wFactory.getEntity(Servers.class, serverInfo);
		} catch (Exception e1) {
			logger.error(caseType+" waiting status,convert server info string to object failed.task id: "+taskId+" errorMsg:"+e1.getLocalizedMessage());
			e1.printStackTrace();
			b = false;
			return b;
		} 
		int flagNotRunning = 0;
		String servers = "[";
		ArrayList<ServerInfo> sList = s.getData();
		for (int i = 0 ;i < Integer.parseInt(s.getTotal()); i++) {
			if(!sList.get(i).equals(status)){
				flagNotRunning++;
			}
			if((i + 1) == Integer.parseInt(s.getTotal())){
				servers = servers+"\""+sList.get(i).getServer_id()+"\"]";
			}else{
				servers = servers+"\""+sList.get(i).getServer_id()+"\",";
			}
		}
		if(flagNotRunning > 0){
			logger.warn(caseType + "query status,there is a virtual machine have other status");
			b = false;
		}
		
		try {
			qResult.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return b;
	}

}
