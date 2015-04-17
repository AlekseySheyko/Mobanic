package com.mappfia.mobanic;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

public class CarsAdapter extends ArrayAdapter<ParseObject> {

    public CarsAdapter(Context context) {
        super(context, 0, new ArrayList<ParseObject>());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
        View rootView = inflater.inflate(R.layout.list_item_car, parent, false);

        ParseObject car = getItem(position);

        TextView makeTextView = (TextView) rootView.findViewById(R.id.make);
        makeTextView.setText(car.getString("make"));

        TextView modelTextView = (TextView) rootView.findViewById(R.id.model);
        modelTextView.setText(car.getString("model"));

        TextView priceTextView = (TextView) rootView.findViewById(R.id.price);
        priceTextView.setText(formatPrice(car.getInt("price")));

        RatioImageView imageView = (RatioImageView) rootView.findViewById(R.id.image);
        Picasso.with(getContext()).load(car.getParseFile("coverImage").getUrl()).fit().centerCrop().into(imageView);

        if (car.getBoolean("isSold")) {
            rootView.findViewById(R.id.sold_mark).setVisibility(View.VISIBLE);
        }

        return rootView;
    }

    public String formatPrice(int price) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        DecimalFormat format = new DecimalFormat("#,###", symbols);

        return "\u00A3" + format.format(price);
    }
}
