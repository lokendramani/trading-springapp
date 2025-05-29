package com.example.kitespringapp.pojo;

import java.util.List;

public class ExpiryYear {
    private String year;
    private List<ExpiryMonth> months;

    public void setMonths(List<ExpiryMonth> months) {
        this.months = months;
    }

    public String getYear() { return year; }
    public List<ExpiryMonth> getMonths() { return months; }

    public void setYear(String year) {
        this.year = year;
    }
}
