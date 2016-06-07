package com.chinacloud.isv.entity.mir;

import java.util.ArrayList;

public class Servers {

	private boolean success;
	private String  total;
	private ArrayList<ServerInfo> data;
	
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getTotal() {
		return total;
	}
	public void setTotal(String total) {
		this.total = total;
	}
	public ArrayList<ServerInfo> getData() {
		return data;
	}
	public void setData(ArrayList<ServerInfo> data) {
		this.data = data;
	}
}
