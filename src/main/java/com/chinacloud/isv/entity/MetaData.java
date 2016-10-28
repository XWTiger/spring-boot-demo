package com.chinacloud.isv.entity;

import java.util.ArrayList;

public class MetaData {

	private String envId;
	private ArrayList<ComponentInfoExtend> componentInfo;
	public String getEnvId() {
		return envId;
	}
	public void setEnvId(String envId) {
		this.envId = envId;
	}
	public ArrayList<ComponentInfoExtend> getComponentInfo() {
		return componentInfo;
	}
	public void setComponentInfo(ArrayList<ComponentInfoExtend> componentInfo) {
		this.componentInfo = componentInfo;
	}
}
