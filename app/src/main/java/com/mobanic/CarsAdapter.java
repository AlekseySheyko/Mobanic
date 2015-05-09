package com.mobanic;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mobanic.views.RatioImageView;
import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.Locale;

public class CarsAdapter extends ParseQueryAdapter<ParseObject> {

    public CarsAdapter(Context context, QueryFactory<ParseObject> queryFactory) {
        super(context, queryFactory);
    }

    @Override
    public View getItemView(ParseObject car, View v, ViewGroup parent) {
        v = View.inflate(getContext(), R.layout.list_item_car, null);

        ((TextView) v.findViewById(R.id.make)).setText(car.getString("make"));
        ((TextView) v.findViewById(R.id.model)).setText(car.getString("model"));
        ((TextView) v.findViewById(R.id.price)).setText(formatPrice(car.getInt("price")));

        RatioImageView imageView = (RatioImageView) v.findViewById(R.id.image);
        Picasso.with(getContext()).load(car.getParseFile("coverImage").getUrl())
                .fit().centerCrop().into(imageView);

        if (car.getBoolean("isSold")) {
            v.findViewById(R.id.sold_label).setVisibility(View.VISIBLE);
        }
        super.getItemView(car, v, parent);

        return v;
    }

    public String formatPrice(int price) {
        NumberFormat f = NumberFormat.getCurrencyInstance(Locale.US);
        f.setMaximumFractionDigits(0);
        return f.format(price);
    }
}
