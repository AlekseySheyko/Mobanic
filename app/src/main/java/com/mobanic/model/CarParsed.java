package com.mobanic.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@ParseClassName("CarParsed")
public class CarParsed extends ParseObject {

    private String[] mPossibleMakes = new String[]{
            "Aston Martin", "BMW", "Bentley",
            "Bugatti", "Defender", "Discovery", "Ferrari",
            "Jaguar", "Jeep", "Lamborghini",
            "Range Rover", "Maybach", "Mercedes-Benz",
            "Porsche", "Rolls Royce"
    };

    public CarParsed() {
    }

    public CarParsed(int id, String header, String year, String imageId, String price, String color,
                     String mileage, String fuelAndTrans, String location, boolean isLeftHanded) {
        put("id", id);
        setTitleAndMake(header);
        setYear(year);
        setCoverImage(imageId);
        setPrice(price);
        setColor(color);
        setMileage(mileage);
        setFuelAndTrans(fuelAndTrans);
        setLocation(location);
        setLeftHanded(isLeftHanded);
    }

    public int getId() {
        return getInt("id");
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
                String[] parts = header.split(" - ");
                String fragmentToRemove = parts[parts.length - 1];
                model = header.substring(make.length()).replaceFirst(" ", "").replace(fragmentToRemove, "").replace("- ", "");
                if (model.contains("(")) {
                    fragmentToRemove = model.substring(model.indexOf("(") - 1, model.indexOf(")") + 1);
                    model = model.replace(fragmentToRemove, "").trim();
                }
                model = model.replace("(LHD)", "").replace("2+2", "")
                        .replace(" (Turbo Wide Body)", "").replace("Fiorano Handling Pack", "")
                        .replace("Koenig Specials", "").replace("Auto", "").replace("Sport", "")
                        .replace("5.0 2dr", "").replace("TWR", "").replace("4.0", "")
                        .replace("2.8 Diesel", "").replace("2DR", "").replace("4DR", "")
                        .replace("7 Series 730 Ld M", "730Ld").replace("16.4", "")
                        .replace("LHD", "").replace("LE", "").replace("3.6 Petrol", "")
                        .replace(" Double Cab", "").replace(" 5 Door", "").replace(" Tiptronic", "")
                        .replace("Vanquish", "Vanquish V12").replace("Mulliner", "")
                        .replace("Vanquish V12 S", "Vanquish S").replace("Diesel PDK", "")
                        .replace(" Saloon", "").replace("3.0", "").replace("2 Cabrio", "Cabrio")
                        .replace("Roadster", "").replace("5.5 V12", "").replace("Diesel HSE", "")
                        .replace("SDV6", "Range Rover Sport SDV6").replace("2.2 SD4 5DR", "")
                        .replace("biography", "").replace("SDV6", "").replace("TDV6", "")
                        .replace("4.4", "").replace("SDV8  ", "").replace("SDV8 ", "")
                        .replace("2.2 TDCI SW 90", "Range Rover Defender").replace("6.2 430 BHP", "Range Rover Defender")
                        .replace("2.2 TDCI 90", "Range Rover Defender").replace("2.2 TDCI XS 110", "Range Rover Defender")
                        .replace("2.2 TDCI XS 90", "Range Rover Defender").replace("2.2 TDCi SW 90", "Range Rover Defender")
                        .replace("2.4 TDCI XS 110", "Range Rover Defender").replace("Sport  SE Tech", "Range Rover Discovery Sport")
                        .replace("2.2 SD4", "Range Rover Discovery").replace("Evoque", "Range Rover Evoque")
                        .replace("Range Rover Range Rover", "Range Rover").replace("Vogue", "Range Rover Vogue")
                        .replace("LWB", "Range Rover Vogue").replace("Discovery Sport", "Sport")
                        .replace("Defender Pick Up", "Defender")
                        .trim();
            }
        }
        put("make", make.replace("Range Rover", "Land Rover").replace("Discovery", "Land Rover").replace("Defender", "Land Rover"));
        put("model", model);
    }

    public String getFormattedPrice() {
        if (getPrice() == -1) {
            return "Under offer";
        } else if (getInt("price") == 1) {
            return "POA";
        } else {
            return formatPrice(getInt("price"));
        }
    }

    public int getPrice() {
        return getInt("price");
    }

    public void setPrice(String priceStr) {
        if (priceStr.contains(".00")) {
            int price = Integer.parseInt(
                    priceStr.replace(".00", "").replaceAll("\\D+", ""));
            put("price", price);
        } else if (priceStr.contains("on Application")) {
            put("price", -1);
        } else {
            put("price", -1);
        }
    }

    public String getCoverImageUrl() {
        return getString("coverImage");
    }

    public void setCoverImage(String imageId) {
        if (!imageId.isEmpty()) {
            put("coverImage", "https://www.kahndesign.com/imgLarge/" + imageId + ".jpg");
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

    public String getMileage() {
        return NumberFormat.getNumberInstance(Locale.UK).format(getInt("mileage"));
    }

    public void setMileage(String mileageStr) {
        int mileage = Integer.parseInt(
                mileageStr.replaceAll(",", ""));
        put("mileage", mileage);
    }

    public int getPreviousOwners() {
        return getInt("previousOwners");
    }

    public void setPrevOwners(int previousOwners) {
        put("previousOwners", previousOwners);
    }

    public String getEngine() {
        return NumberFormat.getNumberInstance(Locale.UK).format(getInt("engine")) + "\u2009" + "cc";
    }

    public void setEngine(int engine) {
        put("engine", engine);
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
        if (location.length() > 2) {
            location = location.substring(1);
        }
        put("location", location);
    }

    public void setLeftHanded(boolean isLeftHanded) {
        put("isLeftHanded", isLeftHanded);
    }

    public boolean isLeftHanded() {
        return getBoolean("isLeftHanded");
    }

    public String getValueForKey(String key) {
        return getString(key.toLowerCase().replace("colour", "color").replace("fuel type", "fuelType").replace("transmission", "transType"));
    }

    public String getAgeCategory() {
        int age = 2015 - getYear();
        if (age <= 1) {
            return "Up to 1 year old";
        } else if (age <= 10) {
            return "Up to " + age + " years old";
        } else {
            return "Over 10 years old";
        }
    }

    public boolean isSold() {
        return false;
    }

    public String formatPrice(int price) {
        NumberFormat f = NumberFormat.getCurrencyInstance(Locale.UK);
        f.setMaximumFractionDigits(0);
        return f.format(price);
    }

    public List<String> getGalleryImages() {
        return getList("galleryImages");
    }

    public void setGalleryImages(List<String> galleryImages) {
        put("galleryImages", galleryImages);
    }

    public List<String> getFeatures() {
        return getList("features");
    }

    public void setFeatures(List<String> features) {
        put("features", features);
    }
}
