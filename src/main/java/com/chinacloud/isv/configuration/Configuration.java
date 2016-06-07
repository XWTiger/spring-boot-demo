package com.chinacloud.isv.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
@Component
@EnableAutoConfiguration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "mir")
public class Configuration {

	@Value("${mir.connectUrl}")
	private String mirConnectUrl;
	@Value("${mir.reLoginTimes}")
	private int reLoginTimes;
	@Value("${mir.serverQueryOutTime}")
	private int timeOut;

	public String getMirConnectUrl() {
		return mirConnectUrl;
	}

	public void setMirConnectUrl(String mirConnectUrl) {
		this.mirConnectUrl = mirConnectUrl;
	}

	public int getReLoginTimes() {
		return reLoginTimes;
	}

	public void setReLoginTimes(int reLoginTimes) {
		this.reLoginTimes = reLoginTimes;
	}

	public int getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}
}
