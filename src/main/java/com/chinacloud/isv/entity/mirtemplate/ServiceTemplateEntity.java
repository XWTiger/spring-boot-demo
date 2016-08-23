package com.chinacloud.isv.entity.mirtemplate;

public class ServiceTemplateEntity {

	private String name;
    private String description;
    private String type;
    private String id;
    private String service_template_id;
    private int template_status;
    private String create_time;
    private String relevant_farm_name;
    private String creater_name;
    private String env_id;
    private String service_template_schema_map;
    
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getService_template_id() {
		return service_template_id;
	}
	public void setService_template_id(String service_template_id) {
		this.service_template_id = service_template_id;
	}
	public int getTemplate_status() {
		return template_status;
	}
	public void setTemplate_status(int template_status) {
		this.template_status = template_status;
	}
	public String getCreate_time() {
		return create_time;
	}
	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}
	public String getRelevant_farm_name() {
		return relevant_farm_name;
	}
	public void setRelevant_farm_name(String relevant_farm_name) {
		this.relevant_farm_name = relevant_farm_name;
	}
	public String getCreater_name() {
		return creater_name;
	}
	public void setCreater_name(String creater_name) {
		this.creater_name = creater_name;
	}
	public String getEnv_id() {
		return env_id;
	}
	public void setEnv_id(String env_id) {
		this.env_id = env_id;
	}
	public String getService_template_schema_map() {
		return service_template_schema_map;
	}
	public void setService_template_schema_map(String service_template_schema_map) {
		this.service_template_schema_map = service_template_schema_map;
	}
}
