package com.mobanic;

import com.parse.ParseClassName;
import com.parse.ParseObject;

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

    public int getPrice() {
        return getInt("price");
    }

    public void setPrice(int price) {
        put("price", price);
    }
}
