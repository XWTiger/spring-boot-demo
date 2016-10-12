package com.chinacloud.isv.entity.callbackparams;

import java.util.ArrayList;

public class Process {

	private String eventId;
	private String status;
	private String instanceId;
	private String extensionUrl = "";
	private ArrayList<Attribute> attribute;
	
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
	public ArrayList<Attribute> getAttribute() {
		return attribute;
	}
	public void setAttribute(ArrayList<Attribute> attribute) {
		this.attribute = attribute;
	}
}
