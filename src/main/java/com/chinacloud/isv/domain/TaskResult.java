package com.chinacloud.isv.domain;

public class TaskResult {

	private String id;
	private String cFarmId = "0";
	private String requestMethod;
	private String params;
	private String resultStatus;
	private String info;
	private String addTime;
	private String requestUrl;
	private String destinationFarmId;
	private String envId;
	private String eventType;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getcFarmId() {
		return cFarmId;
	}
	public void setcFarmId(String cFarmId) {
		this.cFarmId = cFarmId;
	}
	public String getRequestMethod() {
		return requestMethod;
	}
	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}
	public String getParams() {
		return params;
	}
	public void setParams(String params) {
		this.params = params;
	}
	public String getResultStatus() {
		return resultStatus;
	}
	public void setResultStatus(String resultStatus) {
		this.resultStatus = resultStatus;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public String getRequestUrl() {
		return requestUrl;
	}
	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}
	public String getAddTime() {
		return addTime;
	}
	public void setAddTime(String addTime) {
		this.addTime = addTime;
	}
	public String getDestinationFarmId() {
		return destinationFarmId;
	}
	public void setDestinationFarmId(String destinationFarmId) {
		this.destinationFarmId = destinationFarmId;
	}
	public String getEnvId() {
		return envId;
	}
	public void setEnvId(String envId) {
		this.envId = envId;
	}
	public String getEventType() {
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	
}
