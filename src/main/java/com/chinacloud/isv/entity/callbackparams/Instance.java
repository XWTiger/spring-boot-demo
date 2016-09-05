package com.chinacloud.isv.entity.callbackparams;

import java.util.HashMap;

public class Instance {

	private HashMap<String, Object> metadata;
	private String type = "Mir";

	public HashMap<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(HashMap<String, Object> metadata) {
		this.metadata = metadata;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
