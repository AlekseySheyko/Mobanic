package com.mappfia.mobanic;

public class Car {

    private int mId;
    private String mMake;
    private String mModel;
    private float mPrice;
    private String mImageUrl;

    public Car() {
    }

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

    public float getPrice() {
        return mPrice;
    }

    public String getImageUrl() {
        return mImageUrl;
    }
}
