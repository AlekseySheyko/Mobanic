package com.mobanic;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.text.NumberFormat;
import java.util.Locale;

@ParseClassName("Car")
public class Car extends ParseObject {

    public Car() {
    }

    public String getMake() {
        return getString("make");
    }

    public String getModel() {
        return getString("model");
    }

    public int getPrice() {
        return getInt("price");
    }

    public String getFormattedPrice() {
        return formatPrice(getInt("price"));
    }

    public String getCoverImage() {
        return getParseFile("coverImage").getUrl();
    }

    public boolean isSold() {
        return getBoolean("isSold");
    }

    public String formatPrice(int price) {
        NumberFormat f = NumberFormat.getCurrencyInstance(Locale.US);
        f.setMaximumFractionDigits(0);
        return f.format(price);
    }
}
