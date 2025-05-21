package com.example.kitespringapp.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GenericData {

	@JsonProperty("order_id")
	private String orderId;
	@JsonProperty("net")
	private List<NetItem> net;

	@JsonProperty("day")
	private List<DayItem> day;

	public List<NetItem> getNet(){
		return net;
	}

	public List<DayItem> getDay(){
		return day;
	}
	public void setOrderId(String orderId){
		this.orderId = orderId;
	}

	public String getOrderId(){
		return orderId;
	}
}