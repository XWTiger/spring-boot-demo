package com.chinacloud.isv.entity.mir;

import java.util.ArrayList;

public class FarmInfo {

	private String clientid;
	private String id;
	private String name;
	private String status;
	private String dtadded;
	private String created_by_id;
	private String created_by_email;
	private String running_servers;
	private String suspended_servers;
	private String non_running_servers;
	private String roles;
	private String zones;
	private String alerts;
	private String lock;
	private boolean havemysqlrole;
	private boolean havemysql2role;
	private boolean havepgrole;
	private boolean haveredisrole;
	private boolean haverabbitmqrole;
	private boolean havemongodbrole;
	private boolean haveperconarole;
	private boolean havemariadbrole;
	private boolean havedockerrole;
	private String status_txt;
	private ArrayList<?> shortcuts;
	
	public String getClientid() {
		return clientid;
	}
	public void setClientid(String clientid) {
		this.clientid = clientid;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getDtadded() {
		return dtadded;
	}
	public void setDtadded(String dtadded) {
		this.dtadded = dtadded;
	}
	public String getCreated_by_id() {
		return created_by_id;
	}
	public void setCreated_by_id(String created_by_id) {
		this.created_by_id = created_by_id;
	}
	public String getCreated_by_email() {
		return created_by_email;
	}
	public void setCreated_by_email(String created_by_email) {
		this.created_by_email = created_by_email;
	}
	public String getRunning_servers() {
		return running_servers;
	}
	public void setRunning_servers(String running_servers) {
		this.running_servers = running_servers;
	}
	public String getNon_running_servers() {
		return non_running_servers;
	}
	public void setNon_running_servers(String non_running_servers) {
		this.non_running_servers = non_running_servers;
	}
	public String getRoles() {
		return roles;
	}
	public void setRoles(String roles) {
		this.roles = roles;
	}
	public String getZones() {
		return zones;
	}
	public void setZones(String zones) {
		this.zones = zones;
	}
	public String getAlerts() {
		return alerts;
	}
	public void setAlerts(String alerts) {
		this.alerts = alerts;
	}
	public String getLock() {
		return lock;
	}
	public void setLock(String lock) {
		this.lock = lock;
	}
	public boolean isHavemysqlrole() {
		return havemysqlrole;
	}
	public void setHavemysqlrole(boolean havemysqlrole) {
		this.havemysqlrole = havemysqlrole;
	}
	public boolean isHavemysql2role() {
		return havemysql2role;
	}
	public void setHavemysql2role(boolean havemysql2role) {
		this.havemysql2role = havemysql2role;
	}
	public boolean isHavepgrole() {
		return havepgrole;
	}
	public void setHavepgrole(boolean havepgrole) {
		this.havepgrole = havepgrole;
	}
	public boolean isHaveredisrole() {
		return haveredisrole;
	}
	public void setHaveredisrole(boolean haveredisrole) {
		this.haveredisrole = haveredisrole;
	}
	public boolean isHaverabbitmqrole() {
		return haverabbitmqrole;
	}
	public void setHaverabbitmqrole(boolean haverabbitmqrole) {
		this.haverabbitmqrole = haverabbitmqrole;
	}
	public boolean isHavemongodbrole() {
		return havemongodbrole;
	}
	public void setHavemongodbrole(boolean havemongodbrole) {
		this.havemongodbrole = havemongodbrole;
	}
	public boolean isHaveperconarole() {
		return haveperconarole;
	}
	public void setHaveperconarole(boolean haveperconarole) {
		this.haveperconarole = haveperconarole;
	}
	public boolean isHavemariadbrole() {
		return havemariadbrole;
	}
	public void setHavemariadbrole(boolean havemariadbrole) {
		this.havemariadbrole = havemariadbrole;
	}
	public String getStatus_txt() {
		return status_txt;
	}
	public void setStatus_txt(String status_txt) {
		this.status_txt = status_txt;
	}
	
	public ArrayList<?> getShortcuts() {
		return shortcuts;
	}
	public void setShortcuts(ArrayList<?> shortcuts) {
		this.shortcuts = shortcuts;
	}
	public String getSuspended_servers() {
		return suspended_servers;
	}
	public void setSuspended_servers(String suspended_servers) {
		this.suspended_servers = suspended_servers;
	}
	public boolean isHavedockerrole() {
		return havedockerrole;
	}
	public void setHavedockerrole(boolean havedockerrole) {
		this.havedockerrole = havedockerrole;
	}
	
}
