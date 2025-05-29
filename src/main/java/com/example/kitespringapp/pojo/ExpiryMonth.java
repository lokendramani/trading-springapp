package com.example.kitespringapp.pojo;

import java.util.List;

public class ExpiryMonth {
    private String month;
    private String monthly;
    private List<String> weekly;

    public String getMonthly() { return monthly; }

    public String getMonth() {
        return month;
    }

    public List<String> getWeekly() {
        return weekly;
    }
}
