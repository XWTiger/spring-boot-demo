package com.chinacloud.isv.entity.callbackparams;

public class DataExtend {

	private boolean success;
	private String errorCode;
	private String message;
	private ProcessExtend process;
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public ProcessExtend getProcess() {
		return process;
	}
	public void setProcess(ProcessExtend process) {
		this.process = process;
	}

	
}
