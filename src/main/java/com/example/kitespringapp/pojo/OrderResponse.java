package com.example.kitespringapp.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderResponse{

	@JsonProperty("data")
	private GenericData data;

	@JsonProperty("status")
	private String status;

	public void setData(GenericData data){
		this.data = data;
	}

	public GenericData getData(){
		return data;
	}

	public void setStatus(String status){
		this.status = status;
	}

	public String getStatus(){
		return status;
	}
}