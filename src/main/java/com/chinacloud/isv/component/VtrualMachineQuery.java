package com.chinacloud.isv.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.chinacloud.isv.configuration.Configuration;
import com.chinacloud.isv.domain.OrderRecord;
import com.chinacloud.isv.domain.TaskResult;
import com.chinacloud.isv.entity.ResultObject;
import com.chinacloud.isv.entity.VMQeuryParam;
import com.chinacloud.isv.entity.callbackparams.Attribute;
import com.chinacloud.isv.entity.callbackparams.Data;
import com.chinacloud.isv.entity.callbackparams.Process;
import com.chinacloud.isv.entity.mir.ServerInfo;
import com.chinacloud.isv.entity.mir.Servers;
import com.chinacloud.isv.factory.WhiteholeFactory;
import com.chinacloud.isv.persistance.OrderRecordDao;
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
	@Autowired
	OrderRecordDao orderRecordDao;
	@Autowired
	VirtualMachineStatusCheck virtualMachineStatusCheck;
	
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
			try {
				ArrayList<VMQeuryParam> task_list = getQueryTaskLine();
				if(task_list.size() > 0){
					logger.debug("=========begin a task query==============");
					logger.debug("queue size:"+queryList.size());
				}else{
					try {
						currentThread();
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					continue;
				}
				for (VMQeuryParam vp : task_list){
					//create url
					String queryUrl = configuration.getMirConnectUrl()+"servers/xListServers/?farmId="+vp.getcFarmId()+"&imageId=&limit=10&page=1&query=&start=0";
					Map<String,String> headerMap = new HashMap<String,String>();
					headerMap.put("X-Secure-Key", vp.getxSecurityKey());
					headerMap.put("X-Requested-Token", vp.getSpecialToken());
					Data data = new Data();
					Process process = new Process();
					String requestResponse = null;
					if(3 == vp.getType()){//reboot case
						Map<String,String> map = new HashMap<String,String >();
						map.put("Content-Type", "application/json;charset=utf-8");
						boolean b = virtualMachineStatusCheck.isAllInOneStatus(vp.getcFarmId(), vp.getxSecurityKey(), vp.getSpecialToken(), CaseProvider.EVENT_TYPE_SUBSCRIPTION_REBOOT+" ", vp.getTaskId(),"Running");
						if(b){
							data.setSuccess(true);
							data.setMessage(MSUtil.getChineseName(CaseProvider.EVENT_TYPE_SUBSCRIPTION_REBOOT)+"处理成功。");
							process.setEventId(vp.getEnventId());
							process.setStatus("SUCCESS");
							process.setInstanceId(vp.getInstanceId());
							data.setProcess(process);
							String result = null;
							try {
								result = WhiteholeFactory.getJsonString(data);
								logger.info("reboot case,call back return result msg:"+result);
							} catch (JsonProcessingException e) {
								logger.error("reboot case,waiting result,convert object to string failed");
								e.printStackTrace();
							}
							String newResult = MSUtil.encode(result);
							TaskResult taskResult = null;
							CloseableHttpResponse response = null;
							try {
								response = MSUtil.httpClientPostUrl(map, vp.getCallbackUrl(), newResult);
							} catch (Exception e) {
								taskResult = this.getResultInstance(vp.getTaskId(), "FAILED", vp.getCallbackUrl(), "call back return result failed,errorMsg:"+e.getLocalizedMessage(), vp.getcFarmId(), vp.getCallbackUrl(), result,vp.getDestinationFarmId());
								//delete the row record of task 
								riskStackDao.deleteTask(vp.getTaskId());
								taskResultDao.addResult(taskResult);
								removeQueryTask(vp);
								e.printStackTrace();
							}
							try {
								requestResponse = EntityUtils.toString(response.getEntity());
							} catch (Exception e) {
								logger.error("reboot case,convert result entity to string failed");
								e.printStackTrace();
							} 
							taskResult = this.getResultInstance(vp.getTaskId(), "SUCCESS", vp.getCallbackUrl(), requestResponse,0, vp.getCallbackUrl(), result,vp.getDestinationFarmId());
							riskStackDao.deleteTask(vp.getTaskId());
							taskResultDao.addResult(taskResult);
							removeQueryTask(vp);
						}else{
							if(timeOutCheck(vp)){
								TaskResult taskResult = this.getResultInstance(vp.getTaskId(), "FAILED", "", "Reboot Case,Farm Id="+vp.getcFarmId()+" TIME OUT,", vp.getcFarmId(), "", "",vp.getDestinationFarmId());
								riskStackDao.deleteTask(vp.getTaskId());
								taskResultDao.addResult(taskResult);
								removeQueryTask(vp);
							}
						}
					}else if(2 == vp.getType()){//active case
					logger.debug("type: active case");
					logger.debug("------------------------------------");
					//get all servers status 
					CloseableHttpResponse qResult = null;
					try {
						qResult = MSUtil.httpClientGetUrl(headerMap, queryUrl);
						String queryR = EntityUtils.toString(qResult.getEntity());
						logger.debug("vm info:"+queryR);
						WhiteholeFactory wf = new WhiteholeFactory();
						Servers server = wf.getEntity(Servers.class,queryR);
						qResult.close();
						logger.debug("total :"+server.getTotal());
						if(Integer.parseInt(server.getTotal()) > 0){
							int count =0;
							for (ServerInfo si : server.getData()) {
								if(si.getStatus().equals("Running")){
									count++;
								}
							}
							logger.debug("Running number:"+count);
							if(count == Integer.parseInt(server.getTotal())){
								logger.info("all virtual machine are Running, farmid:"+vp.getcFarmId());
								data.setSuccess(true);
								data.setMessage(MSUtil.getChineseName(CaseProvider.EVENT_TYPE_SUBSCRIPTION_ACTIVE)+"处理成功。");
								process.setEventId(vp.getEnventId());
								process.setStatus("SUCCESS");
								process.setInstanceId(vp.getInstanceId());
								data.setProcess(process);
								removeQueryTask(vp);
							}else{
								logger.warn("active case,running vitrual machine error,we need "+server.getTotal()+" machines,but it just have "+count);
								if(timeOutCheck(vp)){
									TaskResult taskResult = this.getResultInstance(vp.getTaskId(), "FAILED", "", "Active Case,Farm Id"+vp.getcFarmId()+" TIME OUT",0, "", "",vp.getDestinationFarmId());
									riskStackDao.deleteTask(vp.getTaskId());
									taskResultDao.addResult(taskResult);
									removeQueryTask(vp);
								}
								continue;
							}
						}
						} catch (Exception e1) {
							logger.error("when active farm stack id:"+vp.getTaskId()+" falied,because of query virtual machine number error");
							e1.printStackTrace();
						}
						//call back return result 
						Map<String,String> map = new HashMap<String,String >();
						map.put("Content-Type", "application/json;charset=utf-8");
						TaskResult taskResult = null;
						String result = null;
						try {
							result = WhiteholeFactory.getJsonString(data);
						} catch (JsonProcessingException e1) {
							logger.error("convert to json failed\n"+e1.getLocalizedMessage());
							e1.printStackTrace();
						}
						try {
							String newResult = MSUtil.encode(result);
							CloseableHttpResponse response = MSUtil.httpClientPostUrl(map, vp.getCallbackUrl(), newResult);
							HttpEntity entity = response.getEntity();
							String comebackResult = EntityUtils.toString(entity);
							logger.info("response entity content--->"+comebackResult);
							taskResult = this.getResultInstance(vp.getTaskId(), "SUCCESS", "POST", comebackResult, 0, vp.getCallbackUrl(), result,vp.getDestinationFarmId());
							//delete the row record of task and the order case result
							riskStackDao.deleteTask(vp.getTaskId());
							taskResultDao.addResult(taskResult);
						} catch (Exception e) {
							taskResult = this.getResultInstance(vp.getTaskId(), "FAILED", "POST", "call back return result failed:"+e.getMessage(), 0, vp.getCallbackUrl(), result,vp.getDestinationFarmId());
							//delete the row record of task 
							riskStackDao.deleteTask(vp.getTaskId());
							taskResultDao.addResult(taskResult);
							removeQueryTask(vp);
							e.printStackTrace();
						}
					}else if(1 == vp.getType()){
						logger.debug("type: cancle case");
						logger.debug("------------------------------------");
						//wait for remove farm stack mission
						String result = null;
						String removeFarmUrl = configuration.getMirConnectUrl()+"farms/xRemove";
						//query if all virtual machine is terminated
						CloseableHttpResponse qResult;
						try {
							qResult = MSUtil.httpClientGetUrl(headerMap, queryUrl);
							String queryR = EntityUtils.toString(qResult.getEntity());
							logger.debug("vm info:"+queryR);
							WhiteholeFactory wf = new WhiteholeFactory();
							Servers server = wf.getEntity(Servers.class,queryR);
							qResult.close();
							logger.debug("total :"+server.getTotal());
							if(Integer.parseInt(server.getTotal()) > 0){
								int count =0;
								for (ServerInfo si : server.getData()) {
									if(si.getStatus().equals("Terminated")){
										count++;
									}
								}
								logger.debug("Terminated number:"+count);
								if(count == Integer.parseInt(server.getTotal())){
									logger.info("all virtual machine Terminated farmid:"+vp.getcFarmId());
								}else{
									if(timeOutCheck(vp)){
										TaskResult taskResult = this.getResultInstance(vp.getTaskId(), "FAILED", "", "Cancle Case,Farm Id"+vp.getcFarmId()+" TIME OUT", 0, "", "",vp.getDestinationFarmId());
										riskStackDao.deleteTask(vp.getTaskId());
										taskResultDao.addResult(taskResult);
										removeQueryTask(vp);
									}
									continue;
								}
							}
						} catch (Exception e1) {
							logger.error("when cancle farm stack id:"+vp.getTaskId()+" falied,because of query virtual machine number error");
							e1.printStackTrace();
						}
						// close farm stack
						try {
							List<NameValuePair> params_list = new ArrayList<NameValuePair>();
							params_list.add(new BasicNameValuePair("farmId",String.valueOf(vp.getcFarmId())));
							CloseableHttpResponse response = MSUtil.httpClientPostUrl(headerMap, removeFarmUrl, params_list);
							String requestR = EntityUtils.toString(response.getEntity());
							ResultObject ro= new WhiteholeFactory().getEntity(ResultObject.class, requestR);
							if(!ro.isSuccess()){
								logger.error("remove farm failed");
								data.setSuccess(false);
								data.setErrorCode("10001");
								data.setMessage(MSUtil.getChineseName(CaseProvider.EVENT_TYPE_SUBSCRIPTION_CANCEL)+"处理失败,原因是删除应用堆栈失败。");
								process.setEventId(vp.getEnventId());
								process.setStatus("FAILED");
								data.setProcess(process);
							}
							response.close();
						} catch (Exception e) {
							logger.error("order case,remove farm failed\n"+e.getLocalizedMessage());
							e.printStackTrace();
						}
						//return success result
						if(null == data.getErrorCode() || data.getErrorCode().equals("")){
							logger.info("===========package success info of cancle case==========");
							data.setSuccess(true);
							data.setMessage(MSUtil.getChineseName(CaseProvider.EVENT_TYPE_SUBSCRIPTION_CANCEL)+"处理成功。");
							process.setEventId(vp.getEnventId());
							process.setStatus("SUCCESS");
							process.setInstanceId(vp.getInstanceId());
							data.setProcess(process);
						}
						
						try {
							result = WhiteholeFactory.getJsonString(data);
							logger.debug("when cancle case,callback return result:"+result);
						} catch (JsonProcessingException e) {
							logger.error("convert to json failed\n"+e.getLocalizedMessage());
							e.printStackTrace();
						}
						//call back return result
						TaskResult taskResult = null;
						Map<String,String> map = new HashMap<String,String >();
						map.put("Content-Type", "application/json;charset=utf-8");
						try {
							String newResult = MSUtil.encode(result);
							CloseableHttpResponse response = MSUtil.httpClientPostUrl(map, vp.getCallbackUrl(), newResult);
							HttpEntity entity = response.getEntity();
							String comebackResult = EntityUtils.toString(entity);
							logger.info("response entity content--->"+comebackResult);
							taskResult = this.getResultInstance(vp.getTaskId(), "SUCCESS", "POST", comebackResult, 0, vp.getCallbackUrl(), result,vp.getDestinationFarmId());
							//delete the row record of task and the order case result
							riskStackDao.deleteTask(vp.getTaskId());
							//delete order case result
							logger.info("cancle case,delete order case result raw,instance id:"+vp.getInstanceId());
							orderRecordDao.deleteByCloneFarmId(vp.getcFarmId());
							taskResultDao.deleteResultById(vp.getInstanceId());
							taskResultDao.addResult(taskResult);
							removeQueryTask(vp);
						} catch (Exception e) {
							taskResult = this.getResultInstance(vp.getTaskId(), "FAILED", "POST", "call back return result failed."+e.getLocalizedMessage(), 0, vp.getCallbackUrl(),"",vp.getDestinationFarmId());
							//delete the row record of task 
							riskStackDao.deleteTask(vp.getTaskId());
							taskResultDao.addResult(taskResult);
							e.printStackTrace();
						}
					}else if(0 == vp.getType()){//wait for create farm stack
						logger.debug("type: order case");
						logger.debug("------------------------------------");
						try {
							CloseableHttpResponse qResult = MSUtil.httpClientGetUrl(headerMap, queryUrl);
							String queryR = EntityUtils.toString(qResult.getEntity());
							WhiteholeFactory wf = new WhiteholeFactory();
							Servers server = wf.getEntity(Servers.class,queryR);
							ArrayList<ServerInfo> sList = server.getData();
							//do analyze
							int total = Integer.parseInt(server.getTotal());
							logger.debug("total : "+total+" roles : "+vp.getRoles()+" virtual machine number :"+vp.getTotalInstance());
							if(total >= vp.getTotalInstance()){
								if(total == vp.getTotalInstance()){
									int flag = 0;
									for (ServerInfo serverInfo : sList) {
										if(!serverInfo.getStatus().equals("Running")){
											flag++;
											break;
										}
									}
									if(flag > 0){
										//check time 
										boolean b = timeOutCheck(vp);
										if(b){
											TaskResult taskResult = this.getResultInstance(vp.getTaskId(), "FAILED", "", "Order Case,Farm Id"+vp.getcFarmId()+" TIME OUT", vp.getcFarmId(), "", "",vp.getDestinationFarmId());
											riskStackDao.deleteTask(vp.getTaskId());
											taskResultDao.addResult(taskResult);
											removeQueryTask(vp);
										}
										continue;
									}
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
										TaskResult taskResult = this.getResultInstance(vp.getTaskId(), "FAILED", "", "Order Case,Farm Id="+vp.getcFarmId()+" TIME OUT", vp.getcFarmId(), "", "",vp.getDestinationFarmId());
										riskStackDao.deleteTask(vp.getTaskId());
										taskResultDao.addResult(taskResult);
										removeQueryTask(vp);
									}
								}
							}else{// server number less than total
								boolean b = timeOutCheck(vp);
								if(b){
									TaskResult taskResult = this.getResultInstance(vp.getTaskId(), "FAILED", "", "Order Case,Farm Id="+vp.getcFarmId()+" TIME OUT", vp.getcFarmId(), "", "",vp.getDestinationFarmId());
									riskStackDao.deleteTask(vp.getTaskId());
									taskResultDao.addResult(taskResult);
									removeQueryTask(vp);
								}
							}
							qResult.close();
						} catch (Exception e) {
							logger.error("order case,get Server list error\n"+e.getLocalizedMessage());
							boolean b = timeOutCheck(vp);
							if(b){
								TaskResult taskResult = this.getResultInstance(vp.getTaskId(), "FAILED", "", "Order Case,Farm Id"+vp.getcFarmId()+" TIME OUT", vp.getcFarmId(), "", "",vp.getDestinationFarmId());
								riskStackDao.deleteTask(vp.getTaskId());
								taskResultDao.addResult(taskResult);
								removeQueryTask(vp);
							}
							e.printStackTrace();
						}
						try {
							String result = null;
							if(null != data.getProcess()){
								Map<String,String> map = new HashMap<String,String >();
								map.put("Content-Type", "application/json;charset=utf-8");
								result = WhiteholeFactory.getJsonString(data);
								logger.info("call back return result: "+ result);
								String newResult = MSUtil.encode(result);
								logger.info("call back return result: "+ result);
								CloseableHttpResponse callbackResponse = null;
								try {
									callbackResponse = MSUtil.httpClientPostUrl(map,vp.getCallbackUrl(), newResult);
								} catch (Exception e) {
									logger.error("order case,response order result failed");
									TaskResult taskResult = this.getResultInstance(vp.getTaskId(), "FAILED", "POST", "", vp.getcFarmId(), vp.getCallbackUrl(), e.getLocalizedMessage(),vp.getDestinationFarmId());
									//delete the row record of task 
									riskStackDao.deleteTask(vp.getTaskId());
									taskResultDao.addResult(taskResult);
									removeQueryTask(vp);
									e.printStackTrace();
								}
								HttpEntity entity = callbackResponse.getEntity();
								String respCall = null;
								try {
									respCall = EntityUtils.toString(entity);
									logger.info("response entity content--->"+respCall);
								} catch (ParseException e) {
									logger.error("after call back return result,get resopnse error\n"+e.getLocalizedMessage());
									e.printStackTrace();
								} catch (IOException e) {
									logger.error("after call back return result,get resopnse error\n"+e.getLocalizedMessage());
									e.printStackTrace();
								}
								TaskResult taskResult = this.getResultInstance(vp.getTaskId(), "SUCCESS", "POST", respCall, vp.getcFarmId(), vp.getCallbackUrl(), result,vp.getDestinationFarmId());
								//add order service instance row
								orderRecordDao.addRecord(getOrderRecordInstance(vp));
								//delete the row record of task 
								riskStackDao.deleteTask(vp.getTaskId());
								taskResultDao.addResult(taskResult);
								try {
									callbackResponse.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						} catch (JsonProcessingException e) {
							logger.error("convert result to json failed");
							e.printStackTrace();
						}
					
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
	/**
	 * 
	 * @param vmQeuryParam
	 * @return time out return true
	 */
	private boolean timeOutCheck(VMQeuryParam vmQeuryParam){
		boolean b = false;
		long time = new Date().getTime() - vmQeuryParam.getBeginTime();
		logger.debug(time+"ms have gone.");
		if(time > configuration.getTimeOut()*60000){
			b = true;
			logger.debug("time out remove node id:"+vmQeuryParam.getTaskId());
		}
		return b;
	}
	/**
	 * get result instance 
	 * type not 0 success, 0 filed
	 * @return
	 */
	private TaskResult getResultInstance(String id,String status,String requestMthod,String requestResponse,int cFarmId,String callBackUrl,String parameters,String destinationFarmId){
		TaskResult tResult = new TaskResult();
		tResult.setResultStatus(status);
		tResult.setId(id);
		tResult.setRequestMethod(requestMthod);
		tResult.setParams(parameters);
		tResult.setInfo(requestResponse);
		tResult.setcFarmId(cFarmId);
		logger.debug("===========cFarmId=======>"+cFarmId+"===destinationFarmId==>"+destinationFarmId);
		tResult.setDestinationFarmId(destinationFarmId);
		tResult.setRequestUrl(callBackUrl);
		return tResult;
	}
	/**
	 * get order record instance 
	 * @param vp
	 * @return OrderRecord
	 */
	private OrderRecord getOrderRecordInstance(VMQeuryParam vp){
		OrderRecord orderRecord = new OrderRecord();
		orderRecord.setId(UUID.randomUUID().toString());//service instance id
		orderRecord.setServiceTemplateId(vp.getServiceTemplateId());
		orderRecord.setSysName(vp.getSystem());
		orderRecord.setcFarmId(vp.getcFarmId());
		orderRecord.setModelFarmId(vp.getModelFarmId());
		orderRecord.setUsrName(vp.getUsrName());
		return orderRecord;
	}
	
	@Override
	public synchronized void start(){
		if(0 == this.status){
			this.status = 1;
			super.start();
		}
	}
}
