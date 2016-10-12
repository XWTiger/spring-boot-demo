package com.chinacloud.isv.entity.mirtemplate;

public class Flavor {
	  //"name":"小","vcpus":1,"ram":2,"disk":40,"volumeSize":128,"maxConNumber":400
	private String name;
	private int vcpus;
	private int ram;
	private int disk;
	private int volumeSize;
	private int maxConNumber;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getVcpus() {
		return vcpus;
	}
	public void setVcpus(int vcpus) {
		this.vcpus = vcpus;
	}
	public int getRam() {
		return ram;
	}
	public void setRam(int ram) {
		this.ram = ram;
	}
	public int getDisk() {
		return disk;
	}
	public void setDisk(int disk) {
		this.disk = disk;
	}
	public int getVolumeSize() {
		return volumeSize;
	}
	public void setVolumeSize(int volumeSize) {
		this.volumeSize = volumeSize;
	}
	public int getMaxConNumber() {
		return maxConNumber;
	}
	public void setMaxConNumber(int maxConNumber) {
		this.maxConNumber = maxConNumber;
	}
	
	
}
