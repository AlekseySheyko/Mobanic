package com.mobanic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.mobanic.utils.AgeSpinnerAdapter;
import com.mobanic.utils.CarsQueryAdapter;
import com.mobanic.utils.MultiSpinner;
import com.mobanic.utils.RangeSeekBar;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.mobanic.utils.MultiSpinner.SearchFiltersListener;

public class MainActivity extends AppCompatActivity implements SearchFiltersListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ParseQueryAdapter<ParseObject> mCarsAdapter;

    private SharedPreferences mSharedPrefs;
    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupNavDrawer();
        setupAgeSpinner();

        updateCars();

        ListView listView = (ListView) findViewById(R.id.cars_listview);
        listView.setAdapter(mCarsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ParseObject car = mCarsAdapter.getItem(position);

                if (!car.getBoolean("isSold")) {
                    mSharedPrefs.edit()
                            .putString("car_id", car.getObjectId())
                            .putInt("car_position", position + 1)
                            .apply();
                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.sold), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });


        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        mContext = this;
    }

    private void setupNavDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();
    }

    public void updateCars() {
        mCarsAdapter = new CarsQueryAdapter(this, getQueryFactory());
    }

    public ParseQuery<ParseObject> getQuery() {
        Set<String> makes = mSharedPrefs.getStringSet("Make", new HashSet<String>());
        Set<String> models = mSharedPrefs.getStringSet("Model", new HashSet<String>());
        Set<String> colors = mSharedPrefs.getStringSet("Color", new HashSet<String>());
        Set<String> transTypes = mSharedPrefs.getStringSet("Transmission", new HashSet<String>());
        Set<String> fuelTypes = mSharedPrefs.getStringSet("Fuel Type", new HashSet<String>());
        int minPrice = mSharedPrefs.getInt("minPrice", -1);
        int maxPrice = mSharedPrefs.getInt("maxPrice", -1);
        int maxAge = mSharedPrefs.getInt("maxAge", -1);

        ParseQuery<ParseObject> query = new ParseQuery<>("Car");
        query.orderByDescending("createdAt");
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);

        if (makes.size() > 0) {
            query.whereContainedIn("make", makes);
        }
        if (models.size() > 0) {
            if (!mSharedPrefs.getBoolean("forceUpdate", false)) {
                query.whereContainedIn("model", models);
            }
        }
        if (colors.size() > 0) {
            query.whereContainedIn("color", colors);
        }
        if (transTypes.size() > 0) {
            query.whereContainedIn("transmission", transTypes);
        }
        if (fuelTypes.size() > 0) {
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

        return query;
    }

    private void updateSearchPanel(List<ParseObject> cars) {

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

        // TODO: Remove this
        boolean filtersSet = false;

        if (!filtersSet) {
            MultiSpinner makeSpinner = (MultiSpinner) findViewById(R.id.make_spinner);
            makeSpinner.setItems(makesList);
            makeSpinner.setSelection(makesList.size() + 1);
        }

        MultiSpinner modelSpinner = (MultiSpinner) findViewById(R.id.model_spinner);
        if (!mSharedPrefs.getBoolean("doNotSetModels", false)) {
            modelSpinner.setItems(modelsList);
            modelSpinner.setSelection(modelsList.size() + 1);
        } else {
            mSharedPrefs.edit().putBoolean("doNotSetModels", false).apply();
        }

        if (mSharedPrefs.getBoolean("forceUpdate", false)) {
            modelSpinner.setItems(modelsList);
            modelSpinner.setSelection(modelsList.size() + 1);
            mSharedPrefs.edit().putBoolean("forceUpdate", false).apply();
        }

        MultiSpinner colorSpinner = (MultiSpinner) findViewById(R.id.color_spinner);
        colorSpinner.setItems(colorList);
        colorSpinner.setSelection(makesList.size() + 1);

        MultiSpinner transSpinner = (MultiSpinner) findViewById(R.id.trans_spinner);
        transSpinner.setItems(transTypesList);
        transSpinner.setSelection(makesList.size() + 1);

        MultiSpinner fuelTypeSpinner = (MultiSpinner) findViewById(R.id.fuel_type_spinner);
        fuelTypeSpinner.setItems(fuelTypesList);
        fuelTypeSpinner.setSelection(makesList.size() + 1);

        Integer minPrice = Collections.min(priceList) / 1000;
        Integer maxPrice = Collections.max(priceList) / 1000 + 1;

        RangeSeekBar<Integer> priceSeekBar =
                (RangeSeekBar<Integer>) findViewById(R.id.price_selector);
        if (!filtersSet) {
            priceSeekBar.setRangeValues(minPrice, maxPrice);
        }
        if (minPrice == -1) {
            priceSeekBar.setSelectedMinValue(minPrice);
        }
        if (maxPrice == -1) {
            priceSeekBar.setSelectedMaxValue(maxPrice + 1);
        }

        priceSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minPrice, Integer maxPrice) {
                mSharedPrefs.edit()
                        .putInt("minPrice", minPrice)
                        .putInt("maxPrice", maxPrice)
                        .apply();
                updateCars();
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
        mSharedPrefs.edit().putStringSet(filterKey, selectedValues).apply();
        updateCars();
    }

    private ParseQueryAdapter.QueryFactory<ParseObject> getQueryFactory() {
        return new ParseQueryAdapter.QueryFactory<ParseObject>() {
            public ParseQuery<ParseObject> create() {
                return getQuery();
            }
        };
    }

    private void setupAgeSpinner() {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        ArrayAdapter<String> adapter = new AgeSpinnerAdapter(
                this, getResources().getStringArray(R.array.car_ages));

        Spinner s = (Spinner) findViewById(R.id.age_spinner);
        s.setAdapter(adapter);
        s.setSelection(adapter.getCount());
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (position != 10) {
                    mSharedPrefs.edit().putInt("maxAge", position + 1).apply();
                }
                updateCars();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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

    public static Context getContext() {
        return mContext;
    }

    public static class PushReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                ((MainActivity) MainActivity.getContext()).updateCars();
            } catch (Exception e) {
                Log.w("PushReceiver//Main", "Can't get activity context to update content");
            }
        }
    }
}
