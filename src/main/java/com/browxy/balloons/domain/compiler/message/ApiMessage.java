package com.browxy.balloons.domain.compiler.message;

public class ApiMessage {
	private String payload;
	private String type;

	public ApiMessage(String payload, String type) {
		this.payload = payload;
		this.type = type;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
