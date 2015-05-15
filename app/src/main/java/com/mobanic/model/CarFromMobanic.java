package com.mobanic.model;

import com.parse.ParseClassName;

import java.text.NumberFormat;
import java.util.Locale;

@ParseClassName("Car")
public class CarFromMobanic extends Car {

    public CarFromMobanic() {
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

    public String getValueForKey(String key) {
        return getString(key.toLowerCase().replace("colour", "color").replace("fuel type", "fuelType"));
    }

    public String getAgeCategory() {
        int age = 2015 - getInt("year");
        if (age <= 1) {
            return "Up to 1 year old";
        } else if (age <= 10) {
            return "Up to " + age + " years old";
        } else {
            return "Over 10 years old";
        }
    }

    public String formatPrice(int price) {
        NumberFormat f = NumberFormat.getCurrencyInstance(Locale.UK);
        f.setMaximumFractionDigits(0);
        return f.format(price);
    }
}
