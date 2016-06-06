package com.chinacloud.isv.entity;

public class VMQeuryParam {

	private String requestUrl;
	private String callbackUrl;
	private int roles;
	private String taskId;
	private int cFarmId;
	private long beginTime;
	
	public String getRequestUrl() {
		return requestUrl;
	}
	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}
	public String getCallbackUrl() {
		return callbackUrl;
	}
	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}
	public int getRoles() {
		return roles;
	}
	public void setRoles(int roles) {
		this.roles = roles;
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public int getcFarmId() {
		return cFarmId;
	}
	public void setcFarmId(int cFarmId) {
		this.cFarmId = cFarmId;
	}
	public long getBeginTime() {
		return beginTime;
	}
	public void setBeginTime(long l) {
		this.beginTime = l;
	}
}
