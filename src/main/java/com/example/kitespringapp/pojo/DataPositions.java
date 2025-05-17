package com.example.kitespringapp.pojo;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DataPositions {

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
}