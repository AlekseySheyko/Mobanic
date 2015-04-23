package com.mappfia.mobanic.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mappfia.mobanic.R;
import com.mappfia.mobanic.utils.CarsAdapter;
import com.mappfia.mobanic.utils.MultiSpinner;
import com.mappfia.mobanic.utils.RangeSeekBar;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.mappfia.mobanic.utils.MultiSpinner.SearchFiltersListener;
import static com.mappfia.mobanic.utils.RangeSeekBar.OnRangeSeekBarChangeListener;

public class MainActivity extends ActionBarActivity
        implements SearchFiltersListener {

    private CarsAdapter mCarsAdapter;

    private MultiSpinner mMakeSpinner;
    private MultiSpinner mModelSpinner;
    private MultiSpinner mColorSpinner;
    private MultiSpinner mTransSpinner;
    private MultiSpinner mLocationSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();


        mCarsAdapter = new CarsAdapter(this);

        ListView carsListView = (ListView) findViewById(R.id.cars_listview);
        carsListView.setAdapter(mCarsAdapter);
        carsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,
                                    int position, long id) {
                String carId = mCarsAdapter.getItem(position).getObjectId();

                Intent intent = new Intent(MainActivity.this,
                        DetailActivity.class);
                intent.putExtra("car_id", carId);
                startActivity(intent);
            }
        });

        updateCarsList(false);
    }

    private void updateCarsList(boolean fromNetwork) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Car");
        if (!fromNetwork) {
            query.fromLocalDatastore();
        }
        /*
        if (filterKey != null && filterValues != null) {
            query.whereContainedIn(filterKey, filterValues);
        }
        if (minPrice != null) {
            query.whereGreaterThanOrEqualTo("price", minPrice * 1000);
        }
        if (maxPrice != null) {
            query.whereLessThanOrEqualTo("price", maxPrice * 1000);
        }
        */
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> cars, ParseException e) {
                if (cars.size() == 0) {
                    if (isOnline()) {
                        updateCarsList(true);
                    } else {
                        Toast.makeText(MainActivity.this,
                                "Connect to a network to load cars list",
                                Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                if (e == null) {
                    Set<String> makesList = new HashSet<>();
                    Set<String> modelsList = new HashSet<>();
                    Set<Integer> priceList = new HashSet<>();
                    Set<String> colorList = new HashSet<>();
                    Set<String> transmissionsList = new HashSet<>();
                    Set<String> locationsList = new HashSet<>();

                    mCarsAdapter.clear();
                    for (ParseObject car : cars) {
                        mCarsAdapter.add(car);
                        car.pinInBackground();
                        makesList.add(car.getString("make"));
                        modelsList.add(car.getString("model"));
                        priceList.add(car.getInt("price"));
                        colorList.add(car.getString("color"));
                        transmissionsList.add(car.getString("transmission"));
                        locationsList.add(car.getString("location"));
                    }

                        mMakeSpinner = (MultiSpinner) findViewById(R.id.make_spinner);
                        mMakeSpinner.setItems(MainActivity.this, "Make", makesList);

                        mModelSpinner = (MultiSpinner) findViewById(R.id.model_spinner);
                        mModelSpinner.setItems(MainActivity.this, "Model", modelsList);

                        mColorSpinner = (MultiSpinner) findViewById(R.id.color_spinner);
                        mColorSpinner.setItems(MainActivity.this, "Color", colorList);

                        mTransSpinner = (MultiSpinner) findViewById(R.id.trans_spinner);
                        mTransSpinner.setItems(MainActivity.this, "Transmission", transmissionsList);

                        mLocationSpinner = (MultiSpinner) findViewById(R.id.location_spinner);
                        mLocationSpinner.setItems(MainActivity.this, "Location", locationsList);

                        Integer minPrice = Collections.min(priceList);
                        minPrice = minPrice / 1000;
                        Integer maxPrice = Collections.max(priceList);
                        maxPrice = maxPrice / 1000 + 1;

                        RangeSeekBar<Integer> rangeSeekBar = (RangeSeekBar<Integer>) findViewById(R.id.price_selector);
                        rangeSeekBar.setRangeValues(minPrice, maxPrice);
                        rangeSeekBar.setSelectedMinValue(minPrice);
                        rangeSeekBar.setSelectedMaxValue(maxPrice + 1);

                        rangeSeekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {
                            @Override
                            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minPrice, Integer maxPrice) {
                                updateCarsList(false);
                            }
                        });

                        ArrayAdapter<String> adapter = new SpinnerAdapter(MainActivity.this);

                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.add("Up to 1 year old");
                        for (int i = 2; i <= 10; i++) {
                            adapter.add("Up to " + i + " years old");
                        }
                        adapter.add("Over 10 years old");
                        adapter.add("Age");

                        Spinner spinner = (Spinner) findViewById(R.id.age_spinner);
                        spinner.setSelection(0);
                        spinner.setAdapter(adapter);
                        spinner.setSelection(adapter.getCount());
                }
            }
        });
    }

    @Override
    public void onFilterSet(String filterKey, List<String> selectedValues) {
        // TODO: Create menu item in action bar to reset filter
    }

    private class SpinnerAdapter extends ArrayAdapter<String> {

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

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
