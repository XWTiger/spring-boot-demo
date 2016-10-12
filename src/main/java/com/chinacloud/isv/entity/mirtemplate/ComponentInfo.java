package com.chinacloud.isv.entity.mirtemplate;

public class ComponentInfo {

	private String componentName;
	private String unitFlavorId;
	private String unitInstanceNumber;
	private String[] componentNet;
	private String componentIpPool;
	private String componentType;
	private Flavor flavor;
	
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
