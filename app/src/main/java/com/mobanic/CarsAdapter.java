package com.mobanic;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseQueryAdapter;

public class CarsAdapter extends ParseQueryAdapter<ParsedCar> {

    public CarsAdapter(Context context, QueryFactory<ParsedCar> queryFactory) {
        super(context, queryFactory);
    }

    @Override
    public View getItemView(ParsedCar car, View v, ViewGroup parent) {
        if (v == null) {
            v = View.inflate(getContext(), R.layout.list_item_car, null);
        }

//        ((TextView) v.findViewById(R.id.make)).setText(car.getMake());
        ((TextView) v.findViewById(R.id.model)).setText(car.getTitle());
        ((TextView) v.findViewById(R.id.price)).setText(car.getFormattedPrice());
//
//        RatioImageView imageView = (RatioImageView) v.findViewById(R.id.image);
//        Picasso.with(getContext()).load(car.getCoverImage()).fit().centerCrop().into(imageView);
//
//        if (car.isSold()) {
//            v.findViewById(R.id.sold).setVisibility(View.VISIBLE);
//        }
        return v;
    }

    @Override
    public void loadObjects() {
        super.loadObjects();
    }
}
