package com.mappfia.mobanic;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CarsAdapter extends ArrayAdapter<Car> {

    public CarsAdapter(Context context) {
        super(context, 0, new ArrayList<Car>());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
        View rootView = inflater.inflate(R.layout.list_item_car, parent, false);

        Car car = getItem(position);

        TextView makeTextView = (TextView) rootView.findViewById(R.id.make);
        makeTextView.setText(car.getMake());

        TextView modelTextView = (TextView) rootView.findViewById(R.id.model);
        modelTextView.setText(car.getModel());

        TextView priceTextView = (TextView) rootView.findViewById(R.id.price);
        priceTextView.setText(car.getPrice());

        RatioImageView imageView = (RatioImageView) rootView.findViewById(R.id.image);
        Picasso.with(getContext()).load(car.getImageUrl()).fit().centerCrop().into(imageView);

        return rootView;
    }
}
