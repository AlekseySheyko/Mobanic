package com.mobanic.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mobanic.R;
import com.mobanic.activities.MasterActivity;
import com.mobanic.views.RatioImageView;
import com.mobanic.tasks.FetchCarsTask;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CarsAdapter extends ArrayAdapter<ParseObject> {

    public CarsAdapter(Context context, ParseQuery<ParseObject> mobanicQuery, ParseQuery<ParseObject> cahnQuery) {
        super(context, 0, new ArrayList<ParseObject>());

        loadCars(mobanicQuery, cahnQuery);
    }

    public void loadCars(ParseQuery<ParseObject> mobanicQuery, ParseQuery<ParseObject> cahnQuery) {
        clear();
        mobanicQuery.findInBackground(mCallback);
        cahnQuery.findInBackground(mCallback);
    }

    private final FindCallback<ParseObject> mCallback = new FindCallback<ParseObject>() {
        @Override
        public void done(List<ParseObject> carList, ParseException e) {
            if (carList.size() == 0) {
                new FetchCarsTask().execute();
            }

            addAll(carList);
            sort(mComparator);

            // TODO: Pass full array, not only the latest carList
            ((MasterActivity) getContext()).updateSearch(carList);
        }
    };

    private final Comparator<ParseObject> mComparator = new Comparator<ParseObject>() {
        @Override
        public int compare(ParseObject parseObject, ParseObject t1) {
            String make1 = parseObject.getString("make");
            String make2 = t1.getString("make");
            String model1 = parseObject.getString("model");
            String model2 = t1.getString("model");
            if (make1.compareTo(make2) != 0) {
                return make1.compareTo(make2);
            } else {
                return model1.compareTo(model2);
            }
        }
    };

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
        ((MasterActivity) getContext()).findViewById(R.id.spinner).setVisibility(View.GONE);
        super.addAll(collection);
    }
}