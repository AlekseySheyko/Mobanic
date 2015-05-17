package com.mobanic.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

// TODO Remove if not make use of
@ParseClassName("Car")
public class Car extends ParseObject {

    public Car() {
    }

    // Two ways to get cars:
    // - from cloud
    // - parse from site
}
