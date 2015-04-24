package com.mappfia.mobanic.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.mappfia.mobanic.R;
import com.mappfia.mobanic.utils.CarsAdapter;
import com.mappfia.mobanic.utils.MultiSpinner;
import com.mappfia.mobanic.utils.RangeSeekBar;
import com.mappfia.mobanic.utils.SpinnerAdapter;
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

    private SharedPreferences mSharedPrefs;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                mToolbar,
                R.string.drawer_open,
                R.string.drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();


        mCarsAdapter = new CarsAdapter(this);

        ListView carsListView = (ListView) findViewById(R.id.cars_listview);
        carsListView.setAdapter(mCarsAdapter);
        carsListView.setEmptyView(findViewById(R.id.spinner));
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

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        updateCarsList(false);
    }

    private boolean mFiltersNotSet;

    private void updateCarsList(boolean fromNetwork) {

        final Set<String> makes = mSharedPrefs.getStringSet("Make", null);
        final Set<String> models = mSharedPrefs.getStringSet("Model", null);
        final Set<String> colors = mSharedPrefs.getStringSet("Color", null);
        final Set<String> transmissions = mSharedPrefs.getStringSet("Transmission", null);
        final int minPrice = mSharedPrefs.getInt("minPrice", -1);
        final int maxPrice = mSharedPrefs.getInt("maxPrice", -1);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Car");
        if (!fromNetwork) {
            query.fromLocalDatastore();
        }
        if (makes != null && makes.size() > 0) {
            query.whereContainedIn("make", makes);
        }
        if (models != null && models.size() > 0) {
            query.whereContainedIn("model", models);
        }
        if (colors != null && colors.size() > 0) {
            query.whereContainedIn("color", colors);
        }
        if (transmissions != null && transmissions.size() > 0) {
            query.whereContainedIn("transmission", transmissions);
        }
        if (minPrice != -1) {
            query.whereGreaterThanOrEqualTo("price", minPrice * 1000);
        }
        if (maxPrice != -1) {
            query.whereLessThanOrEqualTo("price", maxPrice * 1000);
        }
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> cars, ParseException e) {
                mFiltersNotSet = filtersNotSet();

                mCarsAdapter.clear();

                if (cars.size() == 0 && filtersNotSet()) {
                    if (isOnline()) {
                        updateCarsList(true);
                    } else {
                        Toast.makeText(MainActivity.this,
                                "Connect to a network to load cars list",
                                Toast.LENGTH_SHORT).show();
                    }
                    return;
                } else if (cars.size() == 0 && !filtersNotSet()) {
                    Toast.makeText(MainActivity.this, "No items match your search", Toast.LENGTH_SHORT).show();
                    findViewById(R.id.spinner).setVisibility(View.GONE);
                    findViewById(R.id.search_empty).setVisibility(View.VISIBLE);
                    return;
                }

                if (e == null) {
                    for (ParseObject car : cars) {
                        mCarsAdapter.add(car);
                        car.pinInBackground();
                    }

                    if (filtersNotSet()) {
                        populateSearchPanel(cars);
                    }
                }
            }

            private boolean filtersNotSet() {
                return ((makes == null || makes.size() == 0) &&
                        (models == null || models.size() == 0) &&
                        (colors == null || colors.size() == 0) &&
                        (transmissions == null || transmissions.size() == 0) &&
                        (minPrice == -1 || maxPrice == -1));
            }
        });
    }

    private void populateSearchPanel(List<ParseObject> cars) {

        Set<String> makesList = new HashSet<>();
        Set<String> modelsList = new HashSet<>();
        Set<Integer> priceList = new HashSet<>();
        Set<String> colorList = new HashSet<>();
        Set<String> transmissionsList = new HashSet<>();
        Set<String> locationsList = new HashSet<>();

        for (ParseObject car : cars) {
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
        if (mFiltersNotSet) {
            rangeSeekBar.setRangeValues(minPrice, maxPrice);
        }
        if (minPrice == -1) {
            rangeSeekBar.setSelectedMinValue(minPrice);
        }
        if (maxPrice == -1) {
            rangeSeekBar.setSelectedMaxValue(maxPrice + 1);
        }

        rangeSeekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minPrice, Integer maxPrice) {
                mSharedPrefs.edit()
                        .putInt("minPrice", minPrice)
                        .putInt("maxPrice", maxPrice)
                        .apply();
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

    @Override
    public void onFilterSet(String filterKey, Set<String> selectedValues) {
        mSharedPrefs.edit()
                .putStringSet(filterKey, selectedValues)
                .apply();
        updateCarsList(false);
    }

    @Override
    protected void onPause() {
        // TODO: Add feature list uploading to JS web app
        // TODO: Configure JS GCM updates
        // TODO: Fix multiple image uploading
        super.onPause();
        mSharedPrefs.edit()
                .putStringSet("Make", null)
                .putStringSet("Model", null)
                .putStringSet("Color", null)
                .putStringSet("Transmission", null)
                .putInt("minPrice", -1)
                .putInt("maxPrice", -1)
                .apply();
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
