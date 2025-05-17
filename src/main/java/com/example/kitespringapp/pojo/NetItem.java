package com.example.kitespringapp.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NetItem{

	@JsonProperty("day_sell_value")
	private double daySellValue;

	@JsonProperty("sell_price")
	private double sellPrice;

	@JsonProperty("buy_value")
	private double buyValue;

	@JsonProperty("close_price")
	private double closePrice;

	@JsonProperty("overnight_value")
	private double overnightValue;

	@JsonProperty("tradingsymbol")
	private String tradingsymbol;

	@JsonProperty("day_buy_value")
	private double dayBuyValue;

	@JsonProperty("value")
	private double value;

	@JsonProperty("sell_m2m")
	private double sellM2m;

	@JsonProperty("product")
	private String product;

	@JsonProperty("sell_value")
	private double sellValue;

	@JsonProperty("quantity")
	private int quantity;

	@JsonProperty("realised")
	private double realised;

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
	private double dayBuyPrice;

	@JsonProperty("day_sell_quantity")
	private int daySellQuantity;

	@JsonProperty("sell_quantity")
	private int sellQuantity;

	@JsonProperty("overnight_price")
	private double overnightPrice;

	@JsonProperty("pnl")
	private double pnl;

	@JsonProperty("buy_quantity")
	private int buyQuantity;

	@JsonProperty("buy_m2m")
	private double buyM2m;

	@JsonProperty("overnight_quantity")
	private double overnightQuantity;

	@JsonProperty("exchange")
	private String exchange;

	@JsonProperty("buy_price")
	private double buyPrice;

	@JsonProperty("day_buy_quantity")
	private int dayBuyQuantity;

	@JsonProperty("unrealised")
	private double unrealised;

	@JsonProperty("last_price")
	private double lastPrice;

	public double getDaySellValue(){
		return daySellValue;
	}

	public double getSellPrice(){
		return sellPrice;
	}

	public double getBuyValue(){
		return buyValue;
	}

	public double getClosePrice(){
		return closePrice;
	}

	public double getOvernightValue(){
		return overnightValue;
	}

	public String getTradingsymbol(){
		return tradingsymbol;
	}

	public double getDayBuyValue(){
		return dayBuyValue;
	}

	public double getValue(){
		return value;
	}

	public double getSellM2m(){
		return sellM2m;
	}

	public String getProduct(){
		return product;
	}

	public double getSellValue(){
		return sellValue;
	}

	public int getQuantity(){
		return quantity;
	}

	public double getRealised(){
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

	public double getDayBuyPrice(){
		return dayBuyPrice;
	}

	public int getDaySellQuantity(){
		return daySellQuantity;
	}

	public int getSellQuantity(){
		return sellQuantity;
	}

	public double getOvernightPrice(){
		return overnightPrice;
	}

	public double getPnl(){
		return pnl;
	}

	public int getBuyQuantity(){
		return buyQuantity;
	}

	public double getBuyM2m(){
		return buyM2m;
	}

	public double getOvernightQuantity(){
		return overnightQuantity;
	}

	public String getExchange(){
		return exchange;
	}

	public double getBuyPrice(){
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

	@Override
	public String toString() {
		return "NetItem{" +
				"pnl=" + pnl +
				", tradingsymbol='" + tradingsymbol + '\'' +
				'}';
	}
}