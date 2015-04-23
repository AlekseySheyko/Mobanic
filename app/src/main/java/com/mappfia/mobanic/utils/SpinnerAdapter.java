package com.mappfia.mobanic.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SpinnerAdapter extends ArrayAdapter<String> {

    public SpinnerAdapter(Context context) {
        super(context, android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = super.getView(position, convertView, parent);
        if (position == getCount()) {
            TextView textView = (TextView) v.findViewById(android.R.id.text1);
            textView.setText(getItem(getCount()));
        } else {
            TextView textView = (TextView) v.findViewById(android.R.id.text1);
            textView.setText(getItem(position));
        }

        return v;
    }

    @Override
    public int getCount() {
        return super.getCount() - 1;
    }
}