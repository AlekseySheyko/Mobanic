package com.mobanic.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mobanic.R;
import com.mobanic.views.RatioImageView;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class CarsAdapter extends ArrayAdapter<ParseObject> {

    private List<ParseObject> mCarList;

    public CarsAdapter(Context context) {
        super(context, R.layout.list_item_car);
        mCarList = new ArrayList<>();
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        ParseObject car = getItem(position);

        if (v == null) {
            v = View.inflate(getContext(), R.layout.list_item_car, null);
        }
        ((TextView) v.findViewById(R.id.make)).setText(car.getString("make"));
        ((TextView) v.findViewById(R.id.model)).setText(car.getString("model"));
        ((TextView) v.findViewById(R.id.price)).setText(getFormattedPrice(car.getInt("price")));

        RatioImageView imageView = (RatioImageView) v.findViewById(R.id.image);
        if (car.getClassName().equals("Car")) { // instance of CarMobanic
            String url = car.getParseFile("coverImage").getUrl();
            Picasso.with(getContext()).load(url).fit().centerCrop().into(imageView);
        } else {
            String url = car.getString("coverImageUrl");
            Picasso.with(getContext()).load(url).fit().centerCrop().into(imageView);
        }

        if (car.getBoolean("isLeftHanded")) {
            v.findViewById(R.id.leftHanded).setVisibility(View.VISIBLE);
        } else {
            v.findViewById(R.id.leftHanded).setVisibility(View.GONE);
        }
        if (car.getBoolean("isSold")) {
            v.findViewById(R.id.sold).setVisibility(View.VISIBLE);
        } else {
            v.findViewById(R.id.sold).setVisibility(View.GONE);
        }
        return v;
    }

    @Override
    public void addAll(Collection<? extends ParseObject> collection) {
        if (collection != null) {
            mCarList.addAll(collection);
            super.addAll(collection);
        }
    }

    @Override
    public void clear() {
        mCarList.clear();
        super.clear();
    }

    public List<ParseObject> getItems() {
        return mCarList;
    }

    public String getFormattedPrice(int price) {
        if (price == -1) {
            return "Under offer";
        } else if (price == 1) {
            return "POA";
        } else {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.UK);
            formatter.setMaximumFractionDigits(0);
            return formatter.format(price); // adds pound sign
        }
    }
}