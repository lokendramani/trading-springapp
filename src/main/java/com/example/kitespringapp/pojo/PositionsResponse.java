package com.example.kitespringapp.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PositionsResponse{

	@JsonProperty("data")
	private Data data;

	@JsonProperty("status")
	private String status;

	public Data getData(){
		return data;
	}

	public String getStatus(){
		return status;
	}
}