package com.chinacloud.isv.entity.mir;

import java.util.ArrayList;

public class Farms {

	private boolean success;
	private String  total;
	private ArrayList<FarmInfo> data;
	
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
	public ArrayList<FarmInfo> getData() {
		return data;
	}
	public void setData(ArrayList<FarmInfo> data) {
		this.data = data;
	}
}
