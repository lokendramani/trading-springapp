package com.example.kitespringapp.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KiteException{

	@JsonProperty("error_type")
	private String errorType;

	@JsonProperty("message")
	private String message;

	@JsonProperty("status")
	private String status;

	public void setErrorType(String errorType){
		this.errorType = errorType;
	}

	public String getErrorType(){
		return errorType;
	}

	public void setMessage(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
	}

	public void setStatus(String status){
		this.status = status;
	}

	public String getStatus(){
		return status;
	}
}