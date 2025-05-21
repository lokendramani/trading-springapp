package com.example.kitespringapp.pojo.accessResponse;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccessTokenResponse{

	@JsonProperty("data")
	private Data data;

	@JsonProperty("status")
	private String status;

	public void setData(Data data){
		this.data = data;
	}

	public Data getData(){
		return data;
	}

	public void setStatus(String status){
		this.status = status;
	}

	public String getStatus(){
		return status;
	}
}