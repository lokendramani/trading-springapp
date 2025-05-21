package com.example.kitespringapp.pojo.accessResponse;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Data{

	@JsonProperty("user_shortname")
	private String userShortname;

	@JsonProperty("user_name")
	private String userName;

	@JsonProperty("order_types")
	private List<String> orderTypes;

	@JsonProperty("exchanges")
	private List<String> exchanges;

	@JsonProperty("broker")
	private String broker;

	@JsonProperty("products")
	private List<String> products;

	@JsonProperty("access_token")
	private String accessToken;

	@JsonProperty("refresh_token")
	private String refreshToken;

	@JsonProperty("public_token")
	private String publicToken;

	@JsonProperty("user_type")
	private String userType;

	@JsonProperty("avatar_url")
	private Object avatarUrl;

	@JsonProperty("user_id")
	private String userId;

	@JsonProperty("api_key")
	private String apiKey;

	@JsonProperty("enctoken")
	private String enctoken;

	@JsonProperty("login_time")
	private String loginTime;

	@JsonProperty("meta")
	private Meta meta;

	@JsonProperty("email")
	private String email;

	public void setUserShortname(String userShortname){
		this.userShortname = userShortname;
	}

	public String getUserShortname(){
		return userShortname;
	}

	public void setUserName(String userName){
		this.userName = userName;
	}

	public String getUserName(){
		return userName;
	}

	public void setOrderTypes(List<String> orderTypes){
		this.orderTypes = orderTypes;
	}

	public List<String> getOrderTypes(){
		return orderTypes;
	}

	public void setExchanges(List<String> exchanges){
		this.exchanges = exchanges;
	}

	public List<String> getExchanges(){
		return exchanges;
	}

	public void setBroker(String broker){
		this.broker = broker;
	}

	public String getBroker(){
		return broker;
	}

	public void setProducts(List<String> products){
		this.products = products;
	}

	public List<String> getProducts(){
		return products;
	}

	public void setAccessToken(String accessToken){
		this.accessToken = accessToken;
	}

	public String getAccessToken(){
		return accessToken;
	}

	public void setRefreshToken(String refreshToken){
		this.refreshToken = refreshToken;
	}

	public String getRefreshToken(){
		return refreshToken;
	}

	public void setPublicToken(String publicToken){
		this.publicToken = publicToken;
	}

	public String getPublicToken(){
		return publicToken;
	}

	public void setUserType(String userType){
		this.userType = userType;
	}

	public String getUserType(){
		return userType;
	}

	public void setAvatarUrl(Object avatarUrl){
		this.avatarUrl = avatarUrl;
	}

	public Object getAvatarUrl(){
		return avatarUrl;
	}

	public void setUserId(String userId){
		this.userId = userId;
	}

	public String getUserId(){
		return userId;
	}

	public void setApiKey(String apiKey){
		this.apiKey = apiKey;
	}

	public String getApiKey(){
		return apiKey;
	}

	public void setEnctoken(String enctoken){
		this.enctoken = enctoken;
	}

	public String getEnctoken(){
		return enctoken;
	}

	public void setLoginTime(String loginTime){
		this.loginTime = loginTime;
	}

	public String getLoginTime(){
		return loginTime;
	}

	public void setMeta(Meta meta){
		this.meta = meta;
	}

	public Meta getMeta(){
		return meta;
	}

	public void setEmail(String email){
		this.email = email;
	}

	public String getEmail(){
		return email;
	}
}