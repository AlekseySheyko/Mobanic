package com.mobanic.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mobanic.R;
import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;

import java.text.NumberFormat;
import java.util.Locale;

public class CarsQueryAdapter extends ParseQueryAdapter<ParseObject> {

    public CarsQueryAdapter(Context context, QueryFactory<ParseObject> queryFactory) {
        super(context, queryFactory);
        setImageKey("coverImage");
    }

    @Override
    public View getItemView(ParseObject car, View v, ViewGroup parent) {
        if (v == null) {
            v = View.inflate(getContext(), R.layout.list_item_car, null);
        }
        TextView makeTextView = (TextView) v.findViewById(R.id.make);
        makeTextView.setText(car.getString("make"));

        TextView modelTextView = (TextView) v.findViewById(R.id.model);
        modelTextView.setText(car.getString("model"));

        TextView priceTextView = (TextView) v.findViewById(R.id.price);
        priceTextView.setText(formatPrice(car.getInt("price")));

        super.getItemView(car, v, parent);

        return v;
    }

    public String formatPrice(int price) {
        return "\u00A3" + NumberFormat.getNumberInstance(Locale.US).format(price);
    }

}
