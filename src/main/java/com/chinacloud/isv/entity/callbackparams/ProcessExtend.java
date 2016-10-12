package com.chinacloud.isv.entity.callbackparams;

import java.util.HashMap;

public class ProcessExtend {

	private String eventId;
	private String status;
	private String instanceId;
	private String extensionUrl = "";
	private Instance instance;
	private HashMap<String, Object> metadata;
	public String getEventId() {
		return eventId;
	}
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	public String getExtensionUrl() {
		return extensionUrl;
	}
	public void setExtensionUrl(String extensionUrl) {
		this.extensionUrl = extensionUrl;
	}
	public Instance getInstance() {
		return instance;
	}
	public void setInstance(Instance instance) {
		this.instance = instance;
	}
	public HashMap<String, Object> getMetadata() {
		return metadata;
	}
	public void setMetadata(HashMap<String, Object> metadata) {
		this.metadata = metadata;
	}
	
}
