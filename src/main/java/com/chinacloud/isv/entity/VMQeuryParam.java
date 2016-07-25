package com.chinacloud.isv.entity;

public class VMQeuryParam {

	private String requestUrl;
	private String callbackUrl;
	private int roles;
	private String taskId;
	private String cFarmId;
	private String xSecurityKey;
	private String specialToken;
	private String enventId;
	private String modelFarmId;
	private String system;
	private String serviceTemplateId;
	private String usrName;
	private long beginTime;
	private int type = 0;
	private String destinationFarmId;
	private int totalInstance;//a farm have virtual machine total number
	private String params;//this is order case's parameters
	private String serviceTemplateName;
	//the instance id is order case result id
	
	
	public String getSystem() {
		return system;
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
	public void setSystem(String system) {
		this.system = system;
	}
	public String getServiceTemplateId() {
		return serviceTemplateId;
	}
	public void setServiceTemplateId(String serviceTemplateId) {
		this.serviceTemplateId = serviceTemplateId;
	}
	public String getUsrName() {
		return usrName;
	}
	public void setUsrName(String usrName) {
		this.usrName = usrName;
	}
	private String instanceId;
	/**
	 * whitehole will use the id ,when order case, it crate by my self.
	 * @return
	 */
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
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
	public long getBeginTime() {
		return beginTime;
	}
	public void setBeginTime(long l) {
		this.beginTime = l;
	}
	public String getxSecurityKey() {
		return xSecurityKey;
	}
	public void setxSecurityKey(String xSecurityKey) {
		this.xSecurityKey = xSecurityKey;
	}
	public String getSpecialToken() {
		return specialToken;
	}
	public void setSpecialToken(String specialToken) {
		this.specialToken = specialToken;
	}
	public String getEnventId() {
		return enventId;
	}
	public void setEnventId(String enventId) {
		this.enventId = enventId;
	}
	public String getDestinationFarmId() {
		return destinationFarmId;
	}
	public void setDestinationFarmId(String destinationFarmId) {
		this.destinationFarmId = destinationFarmId;
	}
	public int getTotalInstance() {
		return totalInstance;
	}
	public void setTotalInstance(int totalInstance) {
		this.totalInstance = totalInstance;
	}
	public String getParams() {
		return params;
	}
	public void setParams(String params) {
		this.params = params;
	}
	public String getServiceTemplateName() {
		return serviceTemplateName;
	}
	public void setServiceTemplateName(String serviceTemplateName) {
		this.serviceTemplateName = serviceTemplateName;
	}
}
