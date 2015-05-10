package com.mobanic;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mobanic.views.RatioImageView;
import com.parse.ParseQueryAdapter;
import com.squareup.picasso.Picasso;

public class CarsAdapter extends ParseQueryAdapter<Car> {

    public CarsAdapter(Context context, QueryFactory<Car> queryFactory) {
        super(context, queryFactory);
    }

    @Override
    public View getItemView(Car car, View v, ViewGroup parent) {
        v = View.inflate(getContext(), R.layout.list_item_car, null);

        ((TextView) v.findViewById(R.id.make)).setText(car.getMake());
        ((TextView) v.findViewById(R.id.model)).setText(car.getModel());
        ((TextView) v.findViewById(R.id.price)).setText(car.getFormattedPrice());

        RatioImageView imageView = (RatioImageView) v.findViewById(R.id.image);
        Picasso.with(getContext()).load(car.getCoverImage()).fit().centerCrop().into(imageView);

        if (car.isSold()) {
            v.findViewById(R.id.sold).setVisibility(View.VISIBLE);
        }
        return v;
    }

    @Override
    public void loadObjects() {
        super.loadObjects();
    }
}
