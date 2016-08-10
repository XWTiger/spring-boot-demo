package com.chinacloud.isv.domain;

public class OrderRecord {

	private String id;
	private String sysName;
	private String modelFarmId;
	private String cFarmId;
	private String serviceTemplateId;
	private String usrName;
	private String addTime;
	private String serviceTemplateName;
	private String tenantId;//oneaa
	
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
	public String getModelFarmId() {
		return modelFarmId;
	}
	public void setModelFarmId(String modelFarmId) {
		this.modelFarmId = modelFarmId;
	}
	public String getcFarmId() {
		return cFarmId;
	}
	public void setcFarmId(String cFarmId) {
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
	public String getServiceTemplateId() {
		return serviceTemplateId;
	}
	public void setServiceTemplateId(String serviceTemplateId) {
		this.serviceTemplateId = serviceTemplateId;
	}
	public String getServiceTemplateName() {
		return serviceTemplateName;
	}
	public void setServiceTemplateName(String serviceTemplateName) {
		this.serviceTemplateName = serviceTemplateName;
	}
	public String getTenantId() {
		return tenantId;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	
}
