package com.mobanic.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.mobanic.R;
import com.mobanic.adapters.CarsAdapter;
import com.mobanic.model.CarParsed;
import com.mobanic.model.CarMobanic;
import com.mobanic.views.PriceSeekBar;
import com.mobanic.views.SpinnerMultiple;
import com.mobanic.views.SpinnerSingle;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;
import java.util.Set;

public class MasterActivity extends AppCompatActivity
        implements SpinnerSingle.ChoiceListener, SpinnerMultiple.ChoiceListener {

    public CarsAdapter mCarsAdapter;
    private SharedPreferences mSharedPrefs;
    private boolean mInitialStart = true;
    private boolean mMakesUpdated = false;
    private boolean mModelsUpdated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupActionBar(); // adds button to open search


        mCarsAdapter = new CarsAdapter(this,
                getQuery(CarMobanic.class),
                getQuery(CarParsed.class));

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        ListView lv = (ListView) findViewById(R.id.cars_listview);
        lv.setAdapter(mCarsAdapter);
        lv.setEmptyView(findViewById(R.id.error));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ParseObject car = mCarsAdapter.getItem(position);
                if (car.getBoolean("isSold")) {
                    Toast.makeText(MasterActivity.this, getString(R.string.sold),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent i = new Intent(MasterActivity.this, DetailActivity.class);
                String carId = car.getInt("id") + "";
                if (carId.equals("0")) {
                    carId = car.getObjectId();
                }
                i.putExtra("car_id", carId);
                i.putExtra("car_position", position + 1);
                startActivity(i);
            }
        });

        PriceSeekBar bar = (PriceSeekBar) findViewById(R.id.price_seekbar);
        bar.setOnPriceChangeListener(new PriceSeekBar.OnPriceChangeListener<Integer>() {
            @Override
            public void onPriceChanged(PriceSeekBar bar, Integer minPrice, Integer maxPrice) {
                mSharedPrefs.edit()
                        .putInt("minPrice", minPrice)
                        .putInt("maxPrice", maxPrice).apply();
                mCarsAdapter.loadCars(getQuery(CarMobanic.class), getQuery(CarParsed.class));
            }
        });

        sContext = this;
    }

    public void updateSearch(List<ParseObject> carList) {
        updateSearchPanel(carList);
        mMakesUpdated = false;
        mModelsUpdated = false;
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

    public ParseQuery<ParseObject> getQuery(Class parseClass) {
        Set<String> makes = mSharedPrefs.getStringSet("Make", null);
        Set<String> models = mSharedPrefs.getStringSet("Model", null);
        Set<String> colors = mSharedPrefs.getStringSet("Colour", null);
        Set<String> transTypes = mSharedPrefs.getStringSet("Transmission", null);
        Set<String> fuelTypes = mSharedPrefs.getStringSet("Fuel Type", null);
        int minPrice = mSharedPrefs.getInt("minPrice", -1);
        int maxPrice = mSharedPrefs.getInt("maxPrice", -1);
        int maxAge = mSharedPrefs.getInt("maxAge", -1);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(parseClass);
        query.orderByDescending("createdAt");
        if (parseClass.equals(CarParsed.class)) {
            query.fromLocalDatastore();
        }

        if (makes != null && makes.size() > 0) {
            query.whereContainedIn("make", makes);
        }
        if (models != null && models.size() > 0) {
            query.whereContainedIn("model", models);
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
        if (colors != null && colors.size() > 0) {
            query.whereContainedIn("color", colors);
        }
        if (fuelTypes != null && fuelTypes.size() > 0) {
            query.whereContainedIn("fuelType", fuelTypes);
        }
        if (transTypes != null && transTypes.size() > 0) {
            query.whereContainedIn("transType", transTypes);
        }
        return query;
    }

    public void updateSearchPanel(List<ParseObject> carList) {
        if (mInitialStart) {
            ((SpinnerMultiple) findViewById(R.id.make_spinner)).setItems(carList);
        }
        if (mInitialStart || mMakesUpdated) {
            ((SpinnerMultiple) findViewById(R.id.model_spinner)).setItems(carList);
        }
        if (mInitialStart || mMakesUpdated || mModelsUpdated) {
            ((PriceSeekBar) findViewById(R.id.price_seekbar)).setItems(carList);
            ((SpinnerSingle) findViewById(R.id.age_spinner)).setItems(carList);
            ((SpinnerMultiple) findViewById(R.id.colour_spinner)).setItems(carList);
            ((SpinnerMultiple) findViewById(R.id.trans_spinner)).setItems(carList);
            ((SpinnerMultiple) findViewById(R.id.fuel_spinner)).setItems(carList);
        }
        mInitialStart = false;
    }

    @Override
    public void onFilterSet(String key, Set<String> values) {
        if (key.equals("Make")) {
            mMakesUpdated = true;
            mSharedPrefs.edit().clear().apply();
        } else if (key.equals("Model")) {
            Set<String> makes = mSharedPrefs.getStringSet("Make", null);
            mSharedPrefs.edit()
                    .clear()
                    .putStringSet("Make", makes)
                    .apply();
            mModelsUpdated = true;
        }
        mSharedPrefs.edit().putStringSet(key, values).apply();
        mCarsAdapter.loadCars(getQuery(CarMobanic.class), getQuery(CarParsed.class));
    }

    @Override
    public void onAgeSelected(int maxAge) {
        mSharedPrefs.edit().putInt("maxAge", maxAge).apply();
        mCarsAdapter.loadCars(getQuery(CarMobanic.class), getQuery(CarParsed.class));
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSharedPrefs.edit().clear().apply();
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private static Context sContext;

    public static Context getContext() {
        return sContext;
    }

    public static class PushReceiver extends BroadcastReceiver {

        private static final String TAG = PushReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Receive update");
            try {
                MasterActivity a = (MasterActivity) MasterActivity.getContext();
//                a.mForcedNetwork = true;
                a.mCarsAdapter.loadCars(a.getQuery(CarMobanic.class), a.getQuery(CarParsed.class));
            } catch (NullPointerException e) {
                Log.w(TAG, "Can't get activity context to update content");
            }
        }
    }
}