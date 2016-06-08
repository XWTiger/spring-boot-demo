package com.chinacloud.isv.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.chinacloud.isv.configuration.Configuration;
import com.chinacloud.isv.domain.TaskResult;
import com.chinacloud.isv.entity.VMQeuryParam;
import com.chinacloud.isv.entity.callbackparams.Attribute;
import com.chinacloud.isv.entity.callbackparams.Data;
import com.chinacloud.isv.entity.callbackparams.Process;
import com.chinacloud.isv.entity.mir.ServerInfo;
import com.chinacloud.isv.entity.mir.Servers;
import com.chinacloud.isv.factory.WhiteholeFactory;
import com.chinacloud.isv.persistance.TaskResultDao;
import com.chinacloud.isv.persistance.TaskStackDao;
import com.chinacloud.isv.util.CaseProvider;
import com.chinacloud.isv.util.MSUtil;
import com.fasterxml.jackson.core.JsonProcessingException;

@Component
@Scope
public class VtrualMachineQuery extends Thread{

	@Autowired
	TaskStackDao riskStackDao;
	@Autowired
	TaskResultDao taskResultDao;
	@Autowired
	Configuration configuration;
	
	private static Set<VMQeuryParam> queryList = new HashSet<VMQeuryParam>();
	private static final Logger logger = LogManager.getLogger(VtrualMachineQuery.class);
	private int status = 0;
	@Override
	public void run() {
		super.run();
		try {
			currentThread();
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		while(true){
			logger.debug("=========begin a task query==============");
			logger.debug("queue size:"+queryList.size());
			logger.debug("------------------------------------");
			try {
				ArrayList<VMQeuryParam> task_list = getQueryTaskLine();
				for (VMQeuryParam vp : task_list){
					//create url
					String queryUrl = configuration.getMirConnectUrl()+"servers/xListServers/?farmId="+vp.getcFarmId()+"&imageId=&limit=10&page=1&query=&start=0";
					Map<String,String> headerMap = new HashMap<String,String>();
					headerMap.put("X-Secure-Key", vp.getxSecurityKey());
					headerMap.put("X-Requested-Token", vp.getSpecialToken());
					Data data = new Data();
					Process process = new Process();
					try {
						CloseableHttpResponse qResult = MSUtil.httpClientGetUrl(headerMap, queryUrl);
						String queryR = EntityUtils.toString(qResult.getEntity());
						WhiteholeFactory wf = new WhiteholeFactory();
						Servers server = wf.getEntity(Servers.class,queryR);
						ArrayList<ServerInfo> sList = server.getData();
						//do analyze
						int total = Integer.parseInt(server.getTotal());
						logger.debug("total : "+total+"\n roles : "+vp.getRoles());
						if(total >= vp.getRoles()){
							String [] nameList = new String[total];
							int number = 0;
							for (ServerInfo serverInfo : sList) {
								nameList[number] = serverInfo.getRole_name();
								number++;
							}
							number = 0;
							for(int i = 0; i < total; i++){
								String name = nameList[i];
								if(!name.equals("")){
									for(int j = i+1; j < total; j++){
										if(name.equals(nameList[j])){
											nameList[j] = "";
											number++;
										}
									}
								}
							}
							logger.debug("repeat name number : "+number);
							logger.debug("real number: "+(total - number));
							if((total - number) == vp.getRoles()){
								int flag = 0;
								for (ServerInfo serverInfo : sList) {
									if(!serverInfo.getStatus().equals("Running")){
										flag++;
										break;
									}
								}
								if(flag > 0)
									continue;
								data.setSuccess(true);
								data.setMessage(MSUtil.getChineseName(CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER)+"处理成功");
								process.setEventId(vp.getEnventId());
								process.setStatus("SUCCESS");
								process.setInstanceId(vp.getTaskId());
								ArrayList<Attribute> att_list = new ArrayList<Attribute>();
								for (ServerInfo serverInfo : sList) {
									Attribute att = new Attribute();
									att.setKey("role_name");
									att.setValue(serverInfo.getRole_name());
									Attribute att2 = new Attribute();
									att2.setKey("flavor");
									att2.setValue(serverInfo.getFlavor());
									Attribute att3 = new Attribute();
									att3.setKey("farm_id");
									att3.setValue(serverInfo.getFarm_id());
									Attribute att4 = new Attribute();
									att4.setKey("local_ip");
									att4.setValue(serverInfo.getLocal_ip());
									Attribute att5 = new Attribute();
									att5.setKey("remote_ip");
									att5.setValue(serverInfo.getRemote_ip());
									att_list.add(att3);
									att_list.add(att);
									att_list.add(att2);
									att_list.add(att4);
									att_list.add(att5);
								}
								process.setAttribute(att_list);
								data.setProcess(process);
								//delete query task
								removeQueryTask(vp);
							}else{//the server number error
								//check time 
								boolean b = timeOutCheck(vp);
								if(b){
									removeQueryTask(vp);
								}
							}
						}else{// server number less than total
							boolean b = timeOutCheck(vp);
							if(b){
								removeQueryTask(vp);
							}
						}
						qResult.close();
					} catch (Exception e) {
						logger.error("get Server list error\n"+e.getLocalizedMessage());
						data.setSuccess(false);
						data.setMessage(MSUtil.getChineseName(CaseProvider.EVENT_TYPE_SUBSCRIPTION_ORDER)+"处理失败，原因是查询应用堆栈启动的虚拟机时请求失败。");
						data.setErrorCode("10001");
						process.setEventId(vp.getEnventId());
						process.setStatus("FAILED");
						process.setInstanceId(vp.getTaskId());
						data.setProcess(process);
						e.printStackTrace();
					}finally{
						try {
							String result = null;
							if(null != data.getProcess()){
								Map<String,String> map = new HashMap<String,String >();
								map.put("Content-Type", "application/json");
								result = WhiteholeFactory.getJsonString(data);
								CloseableHttpResponse response = null;
								logger.info("call back return result: "+result);
								try {
									response = MSUtil.httpClientPostUrl(map,vp.getCallbackUrl(), result);
								} catch (Exception e) {
									TaskResult taskResult = new TaskResult();
									taskResult.setResultStatus("FAILED");
									//add result
									taskResult.setId(vp.getTaskId());
									taskResult.setRequestMethod("POST");
									taskResult.setParams(result);
									taskResult.setErrorInfo("call back return result failed\n"+e.getLocalizedMessage());
									taskResult.setRequestUrl(vp.getCallbackUrl());
									//TODO delete the row record of task 
									//riskStackDao.deleteTask(vp.getTaskId());
									taskResultDao.addResult(taskResult);
									e.printStackTrace();
								}
								HttpEntity entity = response.getEntity();
								String respCall = null;
								try {
									respCall = EntityUtils.toString(entity);
									logger.info("response entity content--->"+EntityUtils.toString(entity));
								} catch (ParseException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}
								TaskResult taskResult = new TaskResult();
								taskResult.setResultStatus("SUCCESS");
								//add result
								taskResult.setId(vp.getTaskId());
								taskResult.setRequestMethod("POST");
								taskResult.setParams(result);
								taskResult.setErrorInfo(respCall);
								taskResult.setcFarmId(vp.getcFarmId());
								taskResult.setRequestUrl(vp.getCallbackUrl());
								//TODO delete the row record of task 
								//riskStackDao.deleteTask(vp.getTaskId());
								taskResultDao.addResult(taskResult);
								try {
									response.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						} catch (JsonProcessingException e) {
							logger.error("convert result to json failed");
							e.printStackTrace();
						}
						logger.debug(new Date().getTime() - vp.getBeginTime());
					}
				}
				logger.debug("================end query================");
				System.out.println("\n");
				Thread.currentThread();
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error("thread error\n"+e.getLocalizedMessage());
				e.printStackTrace();
				
			}
			
		}
	}

	public synchronized void addQueryTask(VMQeuryParam vmQeuryParam){
		queryList.add(vmQeuryParam);
	}
	
	public void removeQueryTask(VMQeuryParam vp){
		queryList.remove(vp);
	}
	
	
	private ArrayList<VMQeuryParam> getQueryTaskLine(){
		ArrayList<VMQeuryParam> taskList = new ArrayList<VMQeuryParam>();
		Iterator<VMQeuryParam> its = queryList.iterator();
		while(its.hasNext()){
			taskList.add(its.next());
		}
		return taskList;
	}
	
	private boolean timeOutCheck(VMQeuryParam vmQeuryParam){
		boolean b = false;
		long time = new Date().getTime() - vmQeuryParam.getBeginTime();
		logger.debug("the time------>"+configuration.getTimeOut());
		if(time > configuration.getTimeOut()*60000){
			b = true;
			logger.debug("time out remove node id:"+vmQeuryParam.getTaskId());
		}
		return b;
	}
	
	
	@Override
	public synchronized void start(){
		if(0 == this.status){
			this.status = 1;
			super.start();
		}
	}
}
