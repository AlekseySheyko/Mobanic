package com.mappfia.mobanic.activities;

import android.content.BroadcastReceiver;
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
import android.util.Log;
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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.mappfia.mobanic.utils.MultiSpinner.SearchFiltersListener;
import static com.mappfia.mobanic.utils.RangeSeekBar.OnRangeSeekBarChangeListener;

public class MainActivity extends ActionBarActivity
        implements SearchFiltersListener {

    private String LOG_TAG = MainActivity.class.getSimpleName();
    private boolean FROM_LOCAL_STORAGE = false;
    private boolean FROM_NETWORK = true;

    private CarsAdapter mCarsAdapter;

    public static Context mContext;
    private SharedPreferences mSharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

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
        carsListView.setEmptyView(findViewById(R.id.spinner));
        carsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,
                                    int position, long id) {
                ParseObject car = mCarsAdapter.getItem(position);
                String carId = car.getObjectId();

                if (!car.getBoolean("isSold")) {
                    Intent intent = new Intent(MainActivity.this,
                            DetailActivity.class);
                    intent.putExtra("car_id", carId);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "This car has been sold!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        updateCarsList(FROM_LOCAL_STORAGE);


        ArrayAdapter<String> adapter = new SpinnerAdapter(MainActivity.this);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add("Up to 1 year old");
        for (int i = 2; i <= 10; i++) {
            adapter.add("Up to " + i + " years old");
        }
        adapter.add("Over 10 years old");
        adapter.add("Age");

        Spinner spinner = (Spinner) findViewById(R.id.age_spinner);
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getCount());
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {
                mSharedPrefs.edit().putInt("maxAge", position + 1).apply();
                updateCarsList(FROM_LOCAL_STORAGE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    public void updateCarsList(boolean fromNetwork) {

        final Set<String> makes = mSharedPrefs.getStringSet("Make", null);
        final Set<String> models = mSharedPrefs.getStringSet("Model", null);
        final Set<String> colors = mSharedPrefs.getStringSet("Color", null);
        final Set<String> transTypes = mSharedPrefs.getStringSet("Transmission", null);
        final Set<String> fuelTypes = mSharedPrefs.getStringSet("Fuel Type", null);
        final int minPrice = mSharedPrefs.getInt("minPrice", -1);
        final int maxPrice = mSharedPrefs.getInt("maxPrice", -1);
        final int maxAge = mSharedPrefs.getInt("maxAge", -1);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Car");
        query.orderByDescending("createdAt");
        if (!fromNetwork && !mSharedPrefs.getBoolean("update", false)) {
            query.fromLocalDatastore();
        } else {
            mSharedPrefs.edit().putBoolean("update", false).apply();
        }
        if (makes != null && makes.size() > 0) {
            query.whereContainedIn("make", makes);
        }
        if (models != null && models.size() > 0) {
            if (!mSharedPrefs.getBoolean("forceUpdate", false)) {
                query.whereContainedIn("model", models);
            }
        }
        if (colors != null && colors.size() > 0) {
            query.whereContainedIn("color", colors);
        }
        if (transTypes != null && transTypes.size() > 0) {
            query.whereContainedIn("transmission", transTypes);
        }
        if (fuelTypes != null && fuelTypes.size() > 0) {
            query.whereContainedIn("fuelType", fuelTypes);
        }
        if (minPrice != -1) {
            query.whereGreaterThanOrEqualTo("price", minPrice * 1000);
        }
        if (maxPrice != -1) {
            query.whereLessThanOrEqualTo("price", maxPrice * 1000);
        }
        if (maxAge != -1) {
            query.whereGreaterThanOrEqualTo("year", (2015 - maxAge));
        }
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> cars, ParseException e) {
                mCarsAdapter.clear();
                if (e != null) return;


                if (cars.size() == 0 && filtersNotSet()) {
                    if (isOnline()) {
                        updateCarsList(FROM_NETWORK);
                    } else {
                        findViewById(R.id.spinner).setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this,
                                "Connect to a network to load cars list",
                                Toast.LENGTH_LONG).show();
                    }
                    return;
                } else if (cars.size() == 0 && !filtersNotSet()) {
                    Toast.makeText(MainActivity.this, "No items match your search", Toast.LENGTH_SHORT).show();
                    findViewById(R.id.spinner).setVisibility(View.GONE);
                    findViewById(R.id.search_empty).setVisibility(View.VISIBLE);
                    return;
                }

                for (ParseObject car : cars) {
                    mCarsAdapter.add(car);
                    car.pinInBackground();
                }

                if (filtersNotSet()) {
                    updateSearchPanel(cars, false);
                } else if (models != null && models.size() > 0) {
                    mSharedPrefs.edit().putBoolean("doNotSetModels", true).apply();
                    updateSearchPanel(cars, true);
                } else if (makes != null && makes.size() > 0) {
                    updateSearchPanel(cars, true);
                }
            }

            private boolean filtersNotSet() {
                return ((makes == null || makes.size() == 0) &&
                        (models == null || models.size() == 0) &&
                        (colors == null || colors.size() == 0) &&
                        (transTypes == null || transTypes.size() == 0) &&
                        (fuelTypes == null || fuelTypes.size() == 0) &&
                        (minPrice == -1 || maxPrice == -1) && maxAge == -1);
            }
        });
    }

    private void updateSearchPanel(List<ParseObject> cars, final boolean filtersSet) {

        Set<String> makesList = new TreeSet<>();
        Set<String> modelsList = new TreeSet<>();
        Set<Integer> priceList = new TreeSet<>();
        Set<String> colorList = new TreeSet<>();
        Set<String> transTypesList = new TreeSet<>();
        Set<String> fuelTypesList = new TreeSet<>();

        for (ParseObject car : cars) {
            makesList.add(car.getString("make"));
            modelsList.add(car.getString("model"));
            priceList.add(car.getInt("price"));
            colorList.add(car.getString("color"));
            transTypesList.add(car.getString("transmission"));
            fuelTypesList.add(car.getString("fuelType"));
        }

        if (!filtersSet) {
            MultiSpinner makeSpinner = (MultiSpinner) findViewById(R.id.make_spinner);
            makeSpinner.setItems("Make", makesList);
            makeSpinner.setSelection(makesList.size() + 1);
        }

        MultiSpinner modelSpinner = (MultiSpinner) findViewById(R.id.model_spinner);
        if (!mSharedPrefs.getBoolean("doNotSetModels", false)) {
            modelSpinner.setItems("Model", modelsList);
            if (!filtersSet) {
                modelSpinner.setSelection(modelsList.size() + 1);
            }
            if (filtersSet) {
                modelSpinner.refresh();
                modelSpinner.setSelection(modelsList.size() + 1);
            }
        } else {
            mSharedPrefs.edit().putBoolean("doNotSetModels", false).apply();
        }

        if (mSharedPrefs.getBoolean("forceUpdate", false)) {
            modelSpinner.setItems("Model", modelsList);
            modelSpinner.refresh();
            modelSpinner.setSelection(modelsList.size() + 1);
            mSharedPrefs.edit().putBoolean("forceUpdate", false).apply();
        }

        MultiSpinner colorSpinner = (MultiSpinner) findViewById(R.id.color_spinner);
        colorSpinner.setItems("Color", colorList);
        colorSpinner.setSelection(makesList.size() + 1);

        MultiSpinner transSpinner = (MultiSpinner) findViewById(R.id.trans_spinner);
        transSpinner.setItems("Transmission", transTypesList);
        transSpinner.setSelection(makesList.size() + 1);

        MultiSpinner fuelTypeSpinner = (MultiSpinner) findViewById(R.id.fuel_type_spinner);
        fuelTypeSpinner.setItems("Fuel Type", fuelTypesList);
        fuelTypeSpinner.setSelection(makesList.size() + 1);

        Integer minPrice = Collections.min(priceList) / 1000;
        Integer maxPrice = Collections.max(priceList) / 1000 + 1;

        RangeSeekBar<Integer> priceSeekBar = (RangeSeekBar<Integer>) findViewById(R.id.price_selector);
        if (!filtersSet) {
            priceSeekBar.setRangeValues(minPrice, maxPrice);
        }
        if (minPrice == -1) {
            priceSeekBar.setSelectedMinValue(minPrice);
        }
        if (maxPrice == -1) {
            priceSeekBar.setSelectedMaxValue(maxPrice + 1);
        }

        priceSeekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minPrice, Integer maxPrice) {
                mSharedPrefs.edit()
                        .putInt("minPrice", minPrice)
                        .putInt("maxPrice", maxPrice)
                        .apply();
                updateCarsList(FROM_LOCAL_STORAGE);
            }
        });
    }

    @Override
    public void onFilterSet(String filterKey, Set<String> selectedValues) {

        if (filterKey.equals("Model")) {
            mSharedPrefs.edit().putBoolean("doNotSetModels", true).apply();
        }
        if (filterKey.equals("Make")) {
            mSharedPrefs.edit().putBoolean("forceUpdate", true).apply();
        }

        mSharedPrefs.edit()
                .putStringSet(filterKey, selectedValues)
                .apply();
        updateCarsList(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSharedPrefs.edit()
                .putStringSet("Make", null)
                .putStringSet("Model", null)
                .putStringSet("Color", null)
                .putStringSet("Transmission", null)
                .putStringSet("Fuel Type", null)
                .putInt("minPrice", -1)
                .putInt("maxPrice", -1)
                .putInt("maxAge", -1)
                .putBoolean("doNotSetModels", false)
                .putBoolean("forceUpdate", false)
                .apply();
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static Context getContext() {
        return mContext;
    }

    public static class PushReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                ((MainActivity) MainActivity.getContext()).updateCarsList(true);
            } catch (Exception e) {
                SharedPreferences sharedPrefs =
                        PreferenceManager.getDefaultSharedPreferences(context);
                sharedPrefs.edit().putBoolean("update", true).apply();
                Log.d("MainActivity", "Can't get activity context to update content. " +
                        "Just skip, will be updated in a moment.");
            }
        }
    }
}
