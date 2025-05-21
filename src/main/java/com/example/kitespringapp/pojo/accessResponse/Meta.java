package com.example.kitespringapp.pojo.accessResponse;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Meta{

	@JsonProperty("demat_consent")
	private String dematConsent;

	public void setDematConsent(String dematConsent){
		this.dematConsent = dematConsent;
	}

	public String getDematConsent(){
		return dematConsent;
	}
}