package com.mobanic;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.text.NumberFormat;
import java.util.Locale;

@ParseClassName("ParsedCar")
public class ParsedCar extends ParseObject {

    private String[] mPossibleMakes = new String[]{
            "Aston Martin", "BMW", "Bentley",
            "Bugatti", "Defender", "Discovery", "Ferrari",
            "Jaguar", "Jeep", "Lamborghini",
            "Range Rover", "Maybach", "Mercedes-Benz",
            "Porsche", "Rolls Royce"
    };

    public ParsedCar() {
    }

    public String getMake() {
        return getString("make");
    }

    public String getModel() {
        return getString("model");
    }

    public void setTitleAndMake(String header) {
        String make = null;
        String model = null;
        for (String possibleMake : mPossibleMakes) {
            if (header.contains(possibleMake)) {
                make = possibleMake;
                model = header.split(" - ")[0].substring(possibleMake.length()).replaceFirst(" ", "");
            }
        }
        try {
            put("make", make);
            put("model", model);
        } catch (Exception e) {
            Log.e("Download", "Not found possible make for " + header);
        }
    }

    public String getFormattedPrice() {
        if (getInt("price") != 0) {
            return formatPrice(getInt("price"));
        } else {
            return "Under offer";
        }
    }

    public void setPrice(String priceStr) {
        if (priceStr.contains(".00")) {
            int price = Integer.parseInt(
                    priceStr.replace(".00", "").replaceAll("\\D+", ""));
            put("price", price);
        } else {
            put("price", 0);
        }
    }

    public String getCoverImage() {
        return getString("coverImage");
    }

    public void setCoverImage(String url) {
        if (url != null) {
            put("coverImage", url.replace("Medium", "Large")
                    .replace("../", "https://www.kahndesign.com/"));
        } else {
            put("coverImage", "https://www.kahndesign.com/images/AwaitingImage.png");
        }
    }

    public int getYear() {
        return getInt("year");
    }

    public void setYear(String yearStr) {
        int year = Integer.parseInt(
                yearStr.split("/")[0]);
        put("year", year);
    }

    public String getColor() {
        return getString("color");
    }

    public void setColor(String color) {
        put("color", color);
    }

    public int getMileage() {
        return getInt("Mileage");
    }

    public void setMileage(String mileageStr) {
        int mileage = Integer.parseInt(
                mileageStr.replaceAll(",", ""));
        put("mileage", mileage);
    }

    public String getFuelType() {
        return getString("fuelType");
    }

    public String getTransType() {
        return getString("transType");
    }

    public void setFuelAndTrans(String fuelAndTrans) {
        String[] params = fuelAndTrans.split(" : ");
        put("fuelType", params[0]);
        put("transType", params[1]);
    }

    public String getLocation() {
        return getString("location");
    }

    public void setLocation(String location) {
        put("location", location);
    }

    public String formatPrice(int price) {
        NumberFormat f = NumberFormat.getCurrencyInstance(Locale.UK);
        f.setMaximumFractionDigits(0);
        return f.format(price);
    }
}
