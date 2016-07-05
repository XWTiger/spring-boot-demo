package com.chinacloud.isv.entity.mirtemplate;

import java.util.ArrayList;

public class MirTemplate {
	private String serviceTemplateName;
	private String farmId;
	private String serviceTemplateId;
	private ArrayList<ComponentInfo> componentInfos;
	
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
	public ArrayList<ComponentInfo> getComponentInfos() {
		return componentInfos;
	}
	public void setComponentInfos(ArrayList<ComponentInfo> componentInfos) {
		this.componentInfos = componentInfos;
	}
}
