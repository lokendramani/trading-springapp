package com.example.kitespringapp.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PositionsResponse{

	@JsonProperty("data")
	private GenericData data;

	@JsonProperty("status")
	private String status;

	public GenericData getData(){
		return data;
	}

	public String getStatus(){
		return status;
	}
}