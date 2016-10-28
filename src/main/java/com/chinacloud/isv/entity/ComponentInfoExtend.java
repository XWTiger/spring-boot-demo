package com.chinacloud.isv.entity;

import com.chinacloud.isv.entity.mirtemplate.Flavor;

public class ComponentInfoExtend {

	private String componentName;
	private String unitFlavorId;
	private String unitInstanceNumber;
	private String[] componentNet;
	private String componentIpPool;
	private String componentType;
	private Flavor flavor;
	private String componentInstanceNumber;
	private String componentFarmRoleId;
	private String farmId;
	private String farmName;
	private String type;
	
	
	public String getComponentInstanceNumber() {
		return componentInstanceNumber;
	}
	public void setComponentInstanceNumber(String componentInstanceNumber) {
		this.componentInstanceNumber = componentInstanceNumber;
	}
	public String getComponentFarmRoleId() {
		return componentFarmRoleId;
	}
	public void setComponentFarmRoleId(String componentFarmRoleId) {
		this.componentFarmRoleId = componentFarmRoleId;
	}
	public String getFarmId() {
		return farmId;
	}
	public void setFarmId(String farmId) {
		this.farmId = farmId;
	}
	public String getFarmName() {
		return farmName;
	}
	public void setFarmName(String farmName) {
		this.farmName = farmName;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Flavor getFlavor() {
		return flavor;
	}
	public void setFlavor(Flavor flavor) {
		this.flavor = flavor;
	}
	public String getComponentName() {
		return componentName;
	}
	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}
	public String getUnitFlavorId() {
		return unitFlavorId;
	}
	public void setUnitFlavorId(String unitFlavorId) {
		this.unitFlavorId = unitFlavorId;
	}
	public String getUnitInstanceNumber() {
		return unitInstanceNumber;
	}
	public void setUnitInstanceNumber(String unitInstanceNumber) {
		this.unitInstanceNumber = unitInstanceNumber;
	}
	public String[] getComponentNet() {
		return componentNet;
	}
	public void setComponentNet(String[] componentNet) {
		this.componentNet = componentNet;
	}
	public String getComponentIpPool() {
		return componentIpPool;
	}
	public void setComponentIpPool(String componentIpPool) {
		this.componentIpPool = componentIpPool;
	}
	public String getComponentType() {
		return componentType;
	}
	public void setComponentType(String componentType) {
		this.componentType = componentType;
	}
	

}
