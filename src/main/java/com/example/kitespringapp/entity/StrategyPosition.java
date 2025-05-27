package com.example.kitespringapp.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "strategy_positions")
public class StrategyPosition {
    @Id
    private String tradingSymbol;
    @Column(nullable = false)
    private String strategyName; // optional: to group positions
    @Column(nullable = false)
    private String positionType; // CE/PE/STOCK etc.

    public StrategyPosition() {

    }
    public StrategyPosition(String tradingSymbol, String strategyName, String positionType) {
        this.tradingSymbol = tradingSymbol;
        this.strategyName = strategyName;
        this.positionType = positionType;
    }

    public String getTradingSymbol() {
        return tradingSymbol;
    }

    public void setTradingSymbol(String tradingSymbol) {
        this.tradingSymbol = tradingSymbol;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public String getPositionType() {
        return positionType;
    }

    public void setPositionType(String positionType) {
        this.positionType = positionType;
    }
}




