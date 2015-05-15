package com.mobanic;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mobanic.activities.MainActivity;
import com.mobanic.views.RatioImageView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CarsAdapter extends ArrayAdapter<ParseObject> {

    private ParseQuery<ParseObject> mMobanicQuery;
    private ParseQuery<ParseObject> mCahnQuery;
    private List<ParseObject> mCarList;

    public CarsAdapter(Context context, ParseQuery<ParseObject> mobanicQuery, ParseQuery<ParseObject> cahnQuery) {
        super(context, 0, new ArrayList<ParseObject>());
        mMobanicQuery = mobanicQuery;
        mCahnQuery = cahnQuery;

        loadCars();
    }

    public void loadCars() {
        loadCars(null, null);
    }

    public void loadCars(ParseQuery<ParseObject> mobanicQuery, ParseQuery<ParseObject> cahnQuery) {
        clear();
        mCarList = new ArrayList<>();
        if (mobanicQuery == null) {
            mMobanicQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> carList, ParseException e) {
                    if (carList.size() > 0) {
                        mCarList.addAll(carList);
                        Collections.sort(mCarList, new Comparator<ParseObject>() {
                            @Override
                            public int compare(ParseObject parseObject, ParseObject t1) {
                                return parseObject.getString("make").compareTo(t1.getString("make"));
                            }
                        });
                        addAll(mCarList);
                    }
                }
            });
        } else {
            mobanicQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> carList, ParseException e) {
                    mCarList.addAll(carList);
                    Collections.sort(mCarList, new Comparator<ParseObject>() {
                        @Override
                        public int compare(ParseObject parseObject, ParseObject t1) {
                            return parseObject.getString("make").compareTo(t1.getString("make"));
                        }
                    });
                    addAll(mCarList);
                }
            });
        }
        if (cahnQuery == null) {
            mCahnQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> carList, ParseException e) {
                    if (carList.size() > 0) {
                        mCarList.addAll(carList);
                        Collections.sort(mCarList, new Comparator<ParseObject>() {
                            @Override
                            public int compare(ParseObject parseObject, ParseObject t1) {
                                return parseObject.getString("make").compareTo(t1.getString("make"));
                            }
                        });
                        addAll(mCarList);

                        ((MainActivity) getContext()).updateSearch(mCarList);
                    } else {
                        new DownloadCarsTask().execute();
                    }
                }
            });
        } else {
            cahnQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> carList, ParseException e) {
                    mCarList.addAll(carList);
                    Collections.sort(mCarList, new Comparator<ParseObject>() {
                        @Override
                        public int compare(ParseObject parseObject, ParseObject t1) {
                            return parseObject.getString("make").compareTo(t1.getString("make"));
                        }
                    });
                    addAll(mCarList);
                }
            });
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.list_item_car, null);
        }
        ParseObject car = getItem(position);

        ((TextView) convertView.findViewById(R.id.make)).setText(car.getString("make"));
        ((TextView) convertView.findViewById(R.id.model)).setText(car.getString("model"));
        ((TextView) convertView.findViewById(R.id.price)).setText(
                getFormattedPrice(car.getInt("price")));

        RatioImageView imageView = (RatioImageView) convertView.findViewById(R.id.image);
        String coverImageUrl = car.getString("coverImage");
        if (coverImageUrl == null) {
            coverImageUrl = car.getParseFile("coverImage").getUrl();
        }
        Picasso.with(getContext()).load(coverImageUrl).fit().centerCrop().into(imageView);

        if (car.getBoolean("isLeftHanded")) {
            convertView.findViewById(R.id.leftHanded).setVisibility(View.VISIBLE);
        } else {
            convertView.findViewById(R.id.leftHanded).setVisibility(View.GONE);
        }
        if (car.getBoolean("isSold")) {
            convertView.findViewById(R.id.sold).setVisibility(View.VISIBLE);
        } else {
            convertView.findViewById(R.id.sold).setVisibility(View.GONE);
        }
        return convertView;
    }

    public String getFormattedPrice(int price) {
        if (price == -1) {
            return "Under offer";
        } else if (price == 1) {
            return "POA";
        } else {
            return formatPrice(price);
        }
    }

    public String formatPrice(int price) {
        NumberFormat f = NumberFormat.getCurrencyInstance(Locale.UK);
        f.setMaximumFractionDigits(0);
        return f.format(price);
    }

    @Override
    public void addAll(Collection<? extends ParseObject> collection) {
        // TODO Sort all items by make and model
        ((MainActivity) getContext()).findViewById(R.id.spinner).setVisibility(View.GONE);
        super.addAll(collection);
    }
}