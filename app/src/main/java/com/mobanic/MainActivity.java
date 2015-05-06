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
import com.mobanic.utils.CarsAdapter;
import com.mobanic.utils.MultiSpinner;
import com.mobanic.utils.RangeSeekBar;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import static com.mobanic.utils.MultiSpinner.SearchFiltersListener;

public class MainActivity extends AppCompatActivity implements SearchFiltersListener {

    private ParseQueryAdapter<ParseObject> mCarsAdapter;
    private SharedPreferences mSharedPrefs;
    private boolean mFiltersNotSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sContext = this;

        setContentView(R.layout.activity_main);
        setupNavigationDrawer();

        updateCarsAdapter();

        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

    private void setupNavigationDrawer() {
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

        setupAgeSpinner();
    }

    private void setupAgeSpinner() {
        ArrayAdapter<String> adapter = new AgeSpinnerAdapter(
                this, getResources().getStringArray(R.array.ages));

        Spinner spinner = (Spinner) findViewById(R.id.age_spinner);
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getCount());
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {
                if (position <= 10) {
                    mSharedPrefs.edit().putInt("maxAge", position + 1).apply();
                }
                updateCarsAdapter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do not support reset
            }
        });
    }

    public void updateCarsAdapter() {
        mCarsAdapter = new CarsAdapter(this, getQueryFactory());
        mCarsAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<ParseObject>() {
            @Override
            public void onLoaded(List<ParseObject> cars, Exception e) {
                if (e == null) {
                    updateSearchPanel();
                } else {
                    findViewById(R.id.empty).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLoading() {
            }
        });
        populateList();
    }

    private void populateList() {
        ListView lv = (ListView) findViewById(R.id.cars_listview);
        lv.setAdapter(mCarsAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ParseObject car = mCarsAdapter.getItem(position);

                if (!car.getBoolean("isSold")) {
                    Intent i = new Intent(MainActivity.this, DetailActivity.class);
                    i.putExtra("car_id", car.getObjectId());
                    i.putExtra("car_position", position + 1);
                    startActivity(i);
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.sold), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
    }

    private ParseQueryAdapter.QueryFactory<ParseObject> getQueryFactory() {
        return new ParseQueryAdapter.QueryFactory<ParseObject>() {
            public ParseQuery<ParseObject> create() {
                return getQuery();
            }
        };
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
        if (!mSharedPrefs.getBoolean("forceUpdate", false)) {
            query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        } else {
            query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY);
            mSharedPrefs.edit().putBoolean("forceUpdate", false).apply();
        }

        if (makes.size() > 0) {
            query.whereContainedIn("make", makes);
        }
        if (models.size() > 0) {
            query.whereContainedIn("model", models);
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
            query.whereGreaterThanOrEqualTo("price", minPrice);
        }
        if (maxPrice != -1) {
            query.whereLessThanOrEqualTo("price", maxPrice);
        }
        if (maxAge != -1) {
            query.whereGreaterThanOrEqualTo("year", (2015 - maxAge));
        }

        mFiltersNotSet = (makes.size() == 0 && models.size() == 0 && colors.size() == 0
                && transTypes.size() == 0 && fuelTypes.size() == 0 && minPrice == -1
                && maxPrice == -1 && maxAge == -1);

        return query;
    }

    @SuppressWarnings("unchecked")
    private void updateSearchPanel() {

        Set<String> makes = new TreeSet<>();
        Set<String> models = new TreeSet<>();
        Set<Integer> prices = new TreeSet<>();
        Set<String> colors = new TreeSet<>();
        Set<String> transTypes = new TreeSet<>();
        Set<String> fuelTypes = new TreeSet<>();

        for (int i = 0; i < mCarsAdapter.getCount(); i++) {
            ParseObject car = mCarsAdapter.getItem(i);

            makes.add(car.getString("make"));
            models.add(car.getString("model"));
            prices.add(car.getInt("price"));
            colors.add(car.getString("color"));
            transTypes.add(car.getString("transmission"));
            fuelTypes.add(car.getString("fuelType"));
        }

        if (mFiltersNotSet) {
            MultiSpinner makeSpinner = (MultiSpinner) findViewById(R.id.make_spinner);
            makeSpinner.setItems(makes);
        }

        MultiSpinner modelSpinner = (MultiSpinner) findViewById(R.id.model_spinner);
        modelSpinner.setItems(models);

        MultiSpinner colorSpinner = (MultiSpinner) findViewById(R.id.color_spinner);
        colorSpinner.setItems(colors);

        MultiSpinner transSpinner = (MultiSpinner) findViewById(R.id.trans_spinner);
        transSpinner.setItems(transTypes);

        MultiSpinner fuelTypeSpinner = (MultiSpinner) findViewById(R.id.fuel_type_spinner);
        fuelTypeSpinner.setItems(fuelTypes);

        Integer minPrice;
        Integer maxPrice;
        try {
            minPrice = Collections.min(prices);
            maxPrice = Collections.max(prices);
        } catch (NoSuchElementException e) {
            minPrice = 0;
            maxPrice = 99999;
        }

        RangeSeekBar<Integer> priceSeekBar =
                (RangeSeekBar<Integer>) findViewById(R.id.price_selector);
        priceSeekBar.setRangeValues(minPrice / 1000, maxPrice / 1000 + 1);
        priceSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar
                .OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minPrice,
                                                    Integer maxPrice) {
                mSharedPrefs.edit()
                        .putInt("minPrice", minPrice)
                        .putInt("maxPrice", maxPrice)
                        .apply();
                updateCarsAdapter();
            }
        });
    }

    @Override
    public void onFilterSet(String filterKey, Set<String> selectedValues) {
        mSharedPrefs.edit().putStringSet(filterKey, selectedValues).apply();
        updateCarsAdapter();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSharedPrefs.edit().clear().apply();
    }


    private static Context sContext;

    public static Context getContext() {
        return sContext;
    }

    public static class PushReceiver extends BroadcastReceiver {
        private static final String TAG = PushReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                SharedPreferences sharedPrefs =
                        PreferenceManager.getDefaultSharedPreferences(getContext());
                sharedPrefs.edit().putBoolean("forceUpdate", true).apply();
                ((MainActivity) MainActivity.getContext()).updateCarsAdapter();
            } catch (NullPointerException e) {
                Log.w(TAG, "Can't get activity context to update content");
            }
        }
    }
}