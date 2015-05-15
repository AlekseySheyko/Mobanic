package com.mobanic.model;

import com.parse.ParseObject;

public class Car extends ParseObject {

    public Car() {
    }

    public String getMake() {
        return getString("make");
    }

    public String getModel() {
        return getString("model");
    }
}
