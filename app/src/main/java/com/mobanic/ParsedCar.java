package com.mobanic;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.text.NumberFormat;
import java.util.Locale;

@ParseClassName("ParsedCar")
public class ParsedCar extends ParseObject {

    public ParsedCar() {
    }

    public String getTitle() {
        return getString("title");
    }

    public void setTitle(String title) {
        put("title", title);
    }

    public String getFormattedPrice() {
        if (getInt("price") != 0) {
            return formatPrice(getInt("price"));
        } else {
            return "Under offer";
        }
    }

    public void setPrice(int price) {
        put("price", price);
    }

    public String formatPrice(int price) {
        NumberFormat f = NumberFormat.getCurrencyInstance(Locale.UK);
        f.setMaximumFractionDigits(0);
        return f.format(price);
    }
}
