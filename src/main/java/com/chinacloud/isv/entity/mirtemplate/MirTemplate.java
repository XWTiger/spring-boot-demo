package com.chinacloud.isv.entity.mirtemplate;

import java.util.ArrayList;

public class MirTemplate {
	private String serviceTemplateName;
	private String farmId;
	private String serviceTemplateId;
	private ArrayList<ComponentInfo> componentInfo;
	
	public String getServiceTemplateName() {
		return serviceTemplateName;
	}
	public void setServiceTemplateName(String serviceTemplateName) {
		this.serviceTemplateName = serviceTemplateName;
	}
	public String getFarmId() {
		return farmId;
	}
	public void setFarmId(String farmId) {
		this.farmId = farmId;
	}
	public String getServiceTemplateId() {
		return serviceTemplateId;
	}
	public void setServiceTemplateId(String serviceTemplateId) {
		this.serviceTemplateId = serviceTemplateId;
	}
	public ArrayList<ComponentInfo> getComponentInfo() {
		return componentInfo;
	}
	public void setComponentInfo(ArrayList<ComponentInfo> componentInfo) {
		this.componentInfo = componentInfo;
	}
	
}
