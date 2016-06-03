package com.chinacloud.isv.entity;

public class ResultObject {

	private boolean success;
	private String userId;
	private String specialToken;
	private String errorMessage = "";
	private String successMessage = "";
	private String secureKey = "";
	
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getSpecialToken() {
		return specialToken;
	}
	public void setSpecialToken(String specialToken) {
		this.specialToken = specialToken;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String getSuccessMessage() {
		return successMessage;
	}
	public void setSuccessMessage(String successMessage) {
		this.successMessage = successMessage;
	}
	public String getSecureKey() {
		return secureKey;
	}
	public void setSecureKey(String secureKey) {
		this.secureKey = secureKey;
	}
}
