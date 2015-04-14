package com.mappfia.mobanic;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class Car {

    private String mMake;
    private String mModel;
    private float mPrice;
    private String mImageUrl;

    public Car(String make, String model, float price, String imageUrl) {
        mMake = make;
        mModel = model;
        mPrice = price;
        mImageUrl = imageUrl;
    }

    public String getMake() {
        return mMake;
    }

    public String getModel() {
        return mModel;
    }

    public String getPrice() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        DecimalFormat format = new DecimalFormat("#,###", symbols);

        return "\u00A3" + format.format(mPrice);
    }

    public String getImageUrl() {
        return mImageUrl;
    }
}
