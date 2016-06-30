package com.chinacloud.isv.domain;

public class OrderRecord {

	private String id;
	private String sysName;
	private int modelFarmId;
	private int cFarmId;
	private String appStackServiceId;
	private String usrName;
	private String addTime;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSysName() {
		return sysName;
	}
	public void setSysName(String sysName) {
		this.sysName = sysName;
	}
	public int getModelFarmId() {
		return modelFarmId;
	}
	public void setModelFarmId(int modelFarmId) {
		this.modelFarmId = modelFarmId;
	}
	public int getcFarmId() {
		return cFarmId;
	}
	public void setcFarmId(int cFarmId) {
		this.cFarmId = cFarmId;
	}
	public String getUsrName() {
		return usrName;
	}
	public void setUsrName(String usrName) {
		this.usrName = usrName;
	}
	public String getAddTime() {
		return addTime;
	}
	public void setAddTime(String addTime) {
		this.addTime = addTime;
	}
	public String getAppStackServiceId() {
		return appStackServiceId;
	}
	public void setAppStackServiceId(String appStackServiceId) {
		this.appStackServiceId = appStackServiceId;
	}
}
