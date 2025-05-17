package com.example.kitespringapp.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DayItem{

	@JsonProperty("day_sell_value")
	private int daySellValue;

	@JsonProperty("sell_price")
	private double sellPrice;

	@JsonProperty("buy_value")
	private int buyValue;

	@JsonProperty("close_price")
	private double closePrice;

	@JsonProperty("overnight_value")
	private int overnightValue;

	@JsonProperty("tradingsymbol")
	private String tradingsymbol;

	@JsonProperty("day_buy_value")
	private int dayBuyValue;

	@JsonProperty("value")
	private int value;

	@JsonProperty("sell_m2m")
	private int sellM2m;

	@JsonProperty("product")
	private String product;

	@JsonProperty("sell_value")
	private int sellValue;

	@JsonProperty("quantity")
	private int quantity;

	@JsonProperty("realised")
	private int realised;

	@JsonProperty("multiplier")
	private int multiplier;

	@JsonProperty("instrument_token")
	private int instrumentToken;

	@JsonProperty("average_price")
	private double averagePrice;

	@JsonProperty("day_sell_price")
	private double daySellPrice;

	@JsonProperty("m2m")
	private double m2m;

	@JsonProperty("day_buy_price")
	private int dayBuyPrice;

	@JsonProperty("day_sell_quantity")
	private int daySellQuantity;

	@JsonProperty("sell_quantity")
	private int sellQuantity;

	@JsonProperty("overnight_price")
	private int overnightPrice;

	@JsonProperty("pnl")
	private double pnl;

	@JsonProperty("buy_quantity")
	private int buyQuantity;

	@JsonProperty("buy_m2m")
	private int buyM2m;

	@JsonProperty("overnight_quantity")
	private int overnightQuantity;

	@JsonProperty("exchange")
	private String exchange;

	@JsonProperty("buy_price")
	private int buyPrice;

	@JsonProperty("day_buy_quantity")
	private int dayBuyQuantity;

	@JsonProperty("unrealised")
	private double unrealised;

	@JsonProperty("last_price")
	private double lastPrice;

	public int getDaySellValue(){
		return daySellValue;
	}

	public double getSellPrice(){
		return sellPrice;
	}

	public int getBuyValue(){
		return buyValue;
	}

	public double getClosePrice(){
		return closePrice;
	}

	public int getOvernightValue(){
		return overnightValue;
	}

	public String getTradingsymbol(){
		return tradingsymbol;
	}

	public int getDayBuyValue(){
		return dayBuyValue;
	}

	public int getValue(){
		return value;
	}

	public int getSellM2m(){
		return sellM2m;
	}

	public String getProduct(){
		return product;
	}

	public int getSellValue(){
		return sellValue;
	}

	public int getQuantity(){
		return quantity;
	}

	public int getRealised(){
		return realised;
	}

	public int getMultiplier(){
		return multiplier;
	}

	public int getInstrumentToken(){
		return instrumentToken;
	}

	public double getAveragePrice(){
		return averagePrice;
	}

	public double getDaySellPrice(){
		return daySellPrice;
	}

	public double getM2m(){
		return m2m;
	}

	public int getDayBuyPrice(){
		return dayBuyPrice;
	}

	public int getDaySellQuantity(){
		return daySellQuantity;
	}

	public int getSellQuantity(){
		return sellQuantity;
	}

	public int getOvernightPrice(){
		return overnightPrice;
	}

	public double getPnl(){
		return pnl;
	}

	public int getBuyQuantity(){
		return buyQuantity;
	}

	public int getBuyM2m(){
		return buyM2m;
	}

	public int getOvernightQuantity(){
		return overnightQuantity;
	}

	public String getExchange(){
		return exchange;
	}

	public int getBuyPrice(){
		return buyPrice;
	}

	public int getDayBuyQuantity(){
		return dayBuyQuantity;
	}

	public double getUnrealised(){
		return unrealised;
	}

	public double getLastPrice(){
		return lastPrice;
	}
}