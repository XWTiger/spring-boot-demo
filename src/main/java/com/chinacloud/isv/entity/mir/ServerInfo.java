package com.chinacloud.isv.entity.mir;

public class ServerInfo {

	private String id;
	private String server_id;
	private String farm_id;
	private String farm_roleid;
	private String client_id;
	private String env_id;
	private String platform;
	private String status;
	private String remote_ip;
	private String local_ip;
	private String dtadded;
	private String index;
	private String cloud_location;
	private String cloud_location_zone;
	private String image_id;
	private String dtshutdownscheduled;
	private String dtrebootstart;
	private String replace_server_id;
	private String dtlastsync;
	private String os_type;
	private String farm_name;
	private String role_name;
	private String role_alias;
	private String termination_error;
	private String cloud_server_id;
	private String hostname;
	private int is_locked;
	private boolean is_szr;
	private boolean initDetailsSupported;
	private String agent_version;
	private boolean agent_update_needed;
	private boolean agent_update_manual;
	private String os_family;
	private String flavor;
	private String instance_type_name;
	private int alerts;
	private String uptime;
	private boolean excluded_from_dns;
	private String cluster_role;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getServer_id() {
		return server_id;
	}
	public void setServer_id(String server_id) {
		this.server_id = server_id;
	}
	public String getFarm_id() {
		return farm_id;
	}
	public void setFarm_id(String farm_id) {
		this.farm_id = farm_id;
	}
	public String getFarm_roleid() {
		return farm_roleid;
	}
	public void setFarm_roleid(String farm_roleid) {
		this.farm_roleid = farm_roleid;
	}
	public String getClient_id() {
		return client_id;
	}
	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}
	public String getEnv_id() {
		return env_id;
	}
	public void setEnv_id(String env_id) {
		this.env_id = env_id;
	}
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getRemote_ip() {
		return remote_ip;
	}
	public void setRemote_ip(String remote_ip) {
		this.remote_ip = remote_ip;
	}
	public String getLocal_ip() {
		return local_ip;
	}
	public void setLocal_ip(String local_ip) {
		this.local_ip = local_ip;
	}
	public String getDtadded() {
		return dtadded;
	}
	public void setDtadded(String dtadded) {
		this.dtadded = dtadded;
	}
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
	public String getCloud_location() {
		return cloud_location;
	}
	public void setCloud_location(String cloud_location) {
		this.cloud_location = cloud_location;
	}
	public String getCloud_location_zone() {
		return cloud_location_zone;
	}
	public void setCloud_location_zone(String cloud_location_zone) {
		this.cloud_location_zone = cloud_location_zone;
	}
	public String getImage_id() {
		return image_id;
	}
	public void setImage_id(String image_id) {
		this.image_id = image_id;
	}
	public String getDtshutdownscheduled() {
		return dtshutdownscheduled;
	}
	public void setDtshutdownscheduled(String dtshutdownscheduled) {
		this.dtshutdownscheduled = dtshutdownscheduled;
	}
	public String getDtrebootstart() {
		return dtrebootstart;
	}
	public void setDtrebootstart(String dtrebootstart) {
		this.dtrebootstart = dtrebootstart;
	}
	public String getReplace_server_id() {
		return replace_server_id;
	}
	public void setReplace_server_id(String replace_server_id) {
		this.replace_server_id = replace_server_id;
	}
	public String getDtlastsync() {
		return dtlastsync;
	}
	public void setDtlastsync(String dtlastsync) {
		this.dtlastsync = dtlastsync;
	}
	public String getOs_type() {
		return os_type;
	}
	public void setOs_type(String os_type) {
		this.os_type = os_type;
	}
	public String getFarm_name() {
		return farm_name;
	}
	public void setFarm_name(String farm_name) {
		this.farm_name = farm_name;
	}
	public String getRole_name() {
		return role_name;
	}
	public void setRole_name(String role_name) {
		this.role_name = role_name;
	}
	public String getRole_alias() {
		return role_alias;
	}
	public void setRole_alias(String role_alias) {
		this.role_alias = role_alias;
	}
	public String getTermination_error() {
		return termination_error;
	}
	public void setTermination_error(String termination_error) {
		this.termination_error = termination_error;
	}
	public String getCloud_server_id() {
		return cloud_server_id;
	}
	public void setCloud_server_id(String cloud_server_id) {
		this.cloud_server_id = cloud_server_id;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public int getIs_locked() {
		return is_locked;
	}
	public void setIs_locked(int is_locked) {
		this.is_locked = is_locked;
	}
	public boolean isIs_szr() {
		return is_szr;
	}
	public void setIs_szr(boolean is_szr) {
		this.is_szr = is_szr;
	}
	public boolean isInitDetailsSupported() {
		return initDetailsSupported;
	}
	public void setInitDetailsSupported(boolean initDetailsSupported) {
		this.initDetailsSupported = initDetailsSupported;
	}
	public String getAgent_version() {
		return agent_version;
	}
	public void setAgent_version(String agent_version) {
		this.agent_version = agent_version;
	}
	public boolean isAgent_update_needed() {
		return agent_update_needed;
	}
	public void setAgent_update_needed(boolean agent_update_needed) {
		this.agent_update_needed = agent_update_needed;
	}
	public boolean isAgent_update_manual() {
		return agent_update_manual;
	}
	public void setAgent_update_manual(boolean agent_update_manual) {
		this.agent_update_manual = agent_update_manual;
	}
	public String getOs_family() {
		return os_family;
	}
	public void setOs_family(String os_family) {
		this.os_family = os_family;
	}
	public String getFlavor() {
		return flavor;
	}
	public void setFlavor(String flavor) {
		this.flavor = flavor;
	}
	public String getInstance_type_name() {
		return instance_type_name;
	}
	public void setInstance_type_name(String instance_type_name) {
		this.instance_type_name = instance_type_name;
	}
	public int getAlerts() {
		return alerts;
	}
	public void setAlerts(int alerts) {
		this.alerts = alerts;
	}
	public String getUptime() {
		return uptime;
	}
	public void setUptime(String uptime) {
		this.uptime = uptime;
	}
	public boolean isExcluded_from_dns() {
		return excluded_from_dns;
	}
	public void setExcluded_from_dns(boolean excluded_from_dns) {
		this.excluded_from_dns = excluded_from_dns;
	}
	public String getCluster_role() {
		return cluster_role;
	}
	public void setCluster_role(String cluster_role) {
		this.cluster_role = cluster_role;
	}
	
}
