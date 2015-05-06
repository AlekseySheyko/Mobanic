package com.mobanic.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AgeSpinnerAdapter extends ArrayAdapter<String> {

    private static final String TAG = AgeSpinnerAdapter.class.getSimpleName();

    public AgeSpinnerAdapter(Context context, String[] values) {
        super(context, android.R.layout.simple_spinner_dropdown_item, values);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = super.getView(position, convertView, parent);
        if (position == getCount()) {
            ((TextView)v.findViewById(android.R.id.text1)).setText(getItem(getCount()));
        }

        return v;
    }

    @Override
    public int getCount() {
        return super.getCount() - 1;
    }
}