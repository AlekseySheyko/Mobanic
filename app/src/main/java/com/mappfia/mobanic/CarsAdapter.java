package com.mappfia.mobanic;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class CarsAdapter extends ArrayAdapter<Car> {

    public CarsAdapter(Context context) {
        super(context, 0, new ArrayList<Car>());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
        View rootView = inflater.inflate(R.layout.grid_item_car, parent, false);



        return rootView;
    }
}
