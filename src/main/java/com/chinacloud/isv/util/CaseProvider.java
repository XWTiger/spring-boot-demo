package com.chinacloud.isv.util;

public class CaseProvider {

	public static final String EVENT_TYPE_SUBSCRIPTION_ORDER = "SUBSCRIPTION_ORDER";
	public static final String EVENT_TYPE_SUBSCRIPTION_CANCEL = "SUBSCRIPTION_CANCEL";
	public static final String EVENT_TYPE_SUBSCRIPTION_ACTIVE = "SUBSCRIPTION_ACTIVE";
	public static final String EVENT_TYPE_SUBSCRIPTION_SUSPEND = "SUBSCRIPTION_SUSPEND";
	public static final String EVENT_TYPE_SUBSCRIPTION_QUERY = "SUBSCRIPTION_QUERY";
	public static final String EVENT_TYPE_SUBSCRIPTION_REBOOT = "SUBSCRIPTION_REBOOT";
	public static final String EVENT_TYPE_SUBSCRIPTION_LAUNCH = "SUBSCRIPTION_LAUNCH";
	public static final String EVENT_TYPE_WAIT_FOR_RESULT = "WAIT_FOR_RESULT";
	public static final String EVENT_TYPE_WAIT_FOR_RESULT_MESSAGE = "正在处理中";
	
	public static final int EVENT_NUMBER_TYPE_REBOOT = 3;
	public static final int EVENT_NUMBER_TYPE_ACTIVE = 2;
	public static final int EVENT_NUMBER_TYPE_CANCEL = 1;
	public static final int EVENT_NUMBER_TYPE_LAUNCH = 0;
	
	public static final String ACTIVE_FIRST_STEP = "SUCCESS";
	public static final String REBOOT_FIRST_STEP = "SUCCESS";
	public static final String SUCESS_STATUS = "SUCCESS";
	public static final String STATUS_TIME_OUT = "TIME OUT";
	public static final String STATUS_DEAD_LOCK = "DEAD LOCK"; 
	public static final String FAILED_STATUS = "FAILD";
	public static final String ERROR_CODE = "10001";
	public static final String HTTP_STATUS_POST = "POST";
	
	public static final String VM_STATUS_RUNNING = "Running";
	public static final String VM_STATUS_SUSPENEDED = "Suspended";
}
