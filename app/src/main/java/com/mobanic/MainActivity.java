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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobanic.utils.MultiSpinner;
import com.mobanic.utils.RangeSeekBar;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import static com.mobanic.utils.MultiSpinner.SearchFiltersListener;

public class MainActivity extends AppCompatActivity implements SearchFiltersListener {

    private SharedPreferences mSharedPrefs;
    private static Context mContext;
    private ParseQueryAdapter<ParseObject> mCarsAdapter;
    private boolean mFiltersNotSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setupActionBar();

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        updateCarsAdapter();

        ListView listView = (ListView) findViewById(R.id.cars_listview);
        listView.setAdapter(mCarsAdapter);

        TextView emptyText = (TextView) findViewById(android.R.id.empty);
        listView.setEmptyView(emptyText);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ParseObject car = mCarsAdapter.getItem(position);
                String carId = car.getObjectId();
                boolean carIsSold = car.getBoolean("carIsSold");

                if (!carIsSold) {
                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                    mSharedPrefs.edit()
                            .putString("car_id", carId)
                            .putInt("car_position", position + 1)
                            .apply();
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "This car has been sold!", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        mContext = this;


//        ArrayAdapter<String> adapter = new SpinnerAdapter(MainActivity.this);
//
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        adapter.add("Up to 1 year old");
//        for (int i = 2; i <= 10; i++) {
//            adapter.add("Up to " + i + " years old");
//        }
//        adapter.add("Over 10 years old");
//        adapter.add("Age");
//
//        Spinner ageSpinner = (Spinner) findViewById(R.id.age_spinner);
//        ageSpinner.setAdapter(adapter);
//        ageSpinner.setSelection(adapter.getCount());
//        ageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {
//                if ((position + 1) != 11) {
//                    mSharedPrefs.edit().putInt("maxAge", position + 1).apply();
//                }
//                updateCarsList(UPDATE_LOCALLY);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//            }
//        });

        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

    private void setupActionBar() {
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

    private void updateCarsAdapter() {
        mCarsAdapter = new ParseQueryAdapter<ParseObject>(this, getQueryFactory()) {
            @Override
            public View getItemView(ParseObject car, View v, ViewGroup parent) {
                if (v == null) {
                    v = View.inflate(getContext(), R.layout.list_item_car, null);
                }
                TextView makeTextView = (TextView) v.findViewById(R.id.make);
                makeTextView.setText(car.getString("make"));

                TextView modelTextView = (TextView) v.findViewById(R.id.model);
                modelTextView.setText(car.getString("model"));

                TextView priceTextView = (TextView) v.findViewById(R.id.price);
                priceTextView.setText(formatPrice(car.getInt("price")));

                super.getItemView(car, v, parent);

                return v;
            }
        };
        mCarsAdapter.setImageKey("coverImage");
        mCarsAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<ParseObject>() {
            public void onLoading() {
            }

            @Override
            public void onLoaded(List<ParseObject> cars, Exception e) {
                if (e != null) return;

                if (mFiltersNotSet) {
                    MultiSpinner makeSpinner = (MultiSpinner) findViewById(R.id.make_spinner);
                    makeSpinner.setItems("Make", new HashSet<String>());

                    MultiSpinner modelSpinner = (MultiSpinner) findViewById(R.id.model_spinner);
                    modelSpinner.setItems("Model", new HashSet<String>());

                    MultiSpinner colorSpinner = (MultiSpinner) findViewById(R.id.color_spinner);
                    colorSpinner.setItems("Color", new HashSet<String>());

                    MultiSpinner transSpinner = (MultiSpinner) findViewById(R.id.trans_spinner);
                    transSpinner.setItems("Transmission", new HashSet<String>());

                    MultiSpinner fuelTypeSpinner = (MultiSpinner) findViewById(R.id.fuel_type_spinner);
                    fuelTypeSpinner.setItems("Fuel Type", new HashSet<String>());
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
        final Set<String> makes = mSharedPrefs.getStringSet("Make", null);
        final Set<String> models = mSharedPrefs.getStringSet("Model", null);
        final Set<String> colors = mSharedPrefs.getStringSet("Color", null);
        final Set<String> transTypes = mSharedPrefs.getStringSet("Transmission", null);
        final Set<String> fuelTypes = mSharedPrefs.getStringSet("Fuel Type", null);
        final int minPrice = mSharedPrefs.getInt("minPrice", -1);
        final int maxPrice = mSharedPrefs.getInt("maxPrice", -1);
        final int maxAge = mSharedPrefs.getInt("maxAge", -1);

        ParseQuery<ParseObject> query = new ParseQuery<>("Car");
        query.orderByDescending("createdAt");

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

        mFiltersNotSet = ((makes == null || makes.size() == 0) &&
                (models == null || models.size() == 0) &&
                (colors == null || colors.size() == 0) &&
                (transTypes == null || transTypes.size() == 0) &&
                (fuelTypes == null || fuelTypes.size() == 0) &&
                (minPrice == -1 || maxPrice == -1) && maxAge == -1);

        return query;
    }

    public String formatPrice(int price) {
        return "\u00A3" + NumberFormat.getNumberInstance(Locale.US).format(price);
    }


//                if (filtersNotSet()) {
//                    updateSearchPanel(cars, false);
//                } else if (models != null && models.size() > 0) {
//                    mSharedPrefs.edit().putBoolean("doNotSetModels", true).apply();
//                    updateSearchPanel(cars, true);
//                } else if (makes != null && makes.size() > 0) {
//                    updateSearchPanel(cars, true);
//                }
//            }
//

//        });
//    }

    private void updateSearchPanel(List<ParseObject> cars, final boolean filtersSet) {

        Set<String> makesList = new TreeSet<>();
        Set<String> modelsList = new TreeSet<>();
        Set<Integer> priceList = new TreeSet<>();
        Set<String> colorList = new TreeSet<>();
        Set<String> transTypesList = new TreeSet<>();
        Set<String> fuelTypesList = new TreeSet<>();

        for (ParseObject car : cars) {
            if (car.getString("make") != null) {
                makesList.add(car.getString("make"));
            }
            if (car.getString("model") != null) {
                modelsList.add(car.getString("model"));
            }
            if (car.getInt("price") != 0) {
                priceList.add(car.getInt("price"));
            }
            if (car.getString("color") != null) {
                colorList.add(car.getString("color"));
            }
            if (car.getString("transmission") != null) {
                transTypesList.add(car.getString("transmission"));
            }
            if (car.getString("fuelType") != null) {
                fuelTypesList.add(car.getString("fuelType"));
            }
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

        priceSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minPrice, Integer maxPrice) {
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

        if (filterKey.equals("Model")) {
            mSharedPrefs.edit().putBoolean("doNotSetModels", true).apply();
        }
        if (filterKey.equals("Make")) {
            mSharedPrefs.edit().putBoolean("forceUpdate", true).apply();
        }
        mSharedPrefs.edit().putStringSet(filterKey, selectedValues).apply();
        updateCarsAdapter();
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
                ((MainActivity) MainActivity.getContext()).updateCarsAdapter();
            } catch (Exception e) {
                Log.w("MainActivity", "Can't get activity context to update content");
            }
        }
    }
}
