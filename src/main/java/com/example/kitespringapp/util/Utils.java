package com.example.kitespringapp.util;

import com.example.kitespringapp.pojo.ExpiryMonth;
import com.example.kitespringapp.pojo.ExpiryYear;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class Utils {
    public static String getNextValidMonthlyExpirySymbol() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = Utils.class.getResourceAsStream("/config/expiryData.json");
        List<ExpiryYear> expiryYears = mapper.readValue(is, new TypeReference<List<ExpiryYear>>() {});

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);

        for (ExpiryYear yearEntry : expiryYears) {
            for (ExpiryMonth month : yearEntry.getMonths()) {
                if (month.getMonthly() == null || month.getMonthly().isEmpty()) continue;

                LocalDate expiryDate = LocalDate.parse(month.getMonthly(), formatter);
                if (expiryDate.isAfter(today)) {
                    String result = String.format("%ty%tb", expiryDate, expiryDate).toUpperCase();
                    return result; // e.g., 25JUN
                }
            }
        }

        throw new RuntimeException("No valid expiry found after today.");
    }

}
