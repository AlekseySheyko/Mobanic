package com.mobanic.activities;

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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.mobanic.CarFromKahn;
import com.mobanic.CarsAdapter;
import com.mobanic.DownloadCarsTask;
import com.mobanic.R;
import com.mobanic.views.MultiSpinner;
import com.mobanic.views.PriceSeekBar;
import com.mobanic.views.SingleSpinner;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.util.List;
import java.util.Set;

import static com.mobanic.views.MultiSpinner.MultipleFiltersListener;
import static com.mobanic.views.SingleSpinner.AgeFilterListener;

public class MainActivity extends AppCompatActivity implements MultipleFiltersListener,
        AgeFilterListener {

    public CarsAdapter mCarsAdapter;
    private SharedPreferences mSharedPrefs;
    private boolean mInitialStart = true;
    private boolean mMakesUpdated;
    private boolean mModelsUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Add button to open search
        setupActionBar();


        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mCarsAdapter = new CarsAdapter(this, getQueryFactory());
        mCarsAdapter.setAutoload(false);
        mCarsAdapter.setPaginationEnabled(false);
        mCarsAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<CarFromKahn>() {
            @Override
            public void onLoading() {
                findViewById(R.id.spinner).setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoaded(List<CarFromKahn> carList, Exception e) {
                if (carList.size() == 0) {
                    new DownloadCarsTask().execute();
                    return;
                }
                findViewById(R.id.spinner).setVisibility(View.GONE);
                if (e == null) {
                    updateSearchPanel(carList);
                }
                mMakesUpdated = false;
                mModelsUpdated = false;
            }
        });

        ListView lv = (ListView) findViewById(R.id.cars_listview);
        lv.setAdapter(mCarsAdapter);
        lv.setEmptyView(findViewById(R.id.error));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // TODO: Pass all pinned details to the next page
                CarFromKahn car = mCarsAdapter.getItem(position);

                Intent i = new Intent(MainActivity.this, DetailActivity.class);
                i.putExtra("car_id", car.getId());
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
                mCarsAdapter.loadObjects();
            }
        });

        mCarsAdapter.loadObjects();
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

        sContext = this;
    }

    public ParseQueryAdapter.QueryFactory<CarFromKahn> getQueryFactory() {
        return new ParseQueryAdapter.QueryFactory<CarFromKahn>() {
            public ParseQuery<CarFromKahn> create() {
                Set<String> makes = mSharedPrefs.getStringSet("Make", null);
                Set<String> models = mSharedPrefs.getStringSet("Model", null);
                Set<String> colors = mSharedPrefs.getStringSet("Colour", null);
                Set<String> transTypes = mSharedPrefs.getStringSet("Transmission", null);
                Set<String> fuelTypes = mSharedPrefs.getStringSet("Fuel Type", null);
                int minPrice = mSharedPrefs.getInt("minPrice", -1);
                int maxPrice = mSharedPrefs.getInt("maxPrice", -1);
                int maxAge = mSharedPrefs.getInt("maxAge", -1);

                ParseQuery<CarFromKahn> query = ParseQuery.getQuery(CarFromKahn.class);
                query.orderByDescending("createdAt");
                query.fromLocalDatastore();

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
        };
    }

    public void updateSearchPanel(List<CarFromKahn> carList) {
        if (mInitialStart) {
            ((MultiSpinner) findViewById(R.id.make_spinner)).setItems(carList);
        }
        if (mInitialStart || mMakesUpdated) {
            ((MultiSpinner) findViewById(R.id.model_spinner)).setItems(carList);
        }
        if (mInitialStart || mMakesUpdated || mModelsUpdated) {
            ((PriceSeekBar) findViewById(R.id.price_seekbar)).setItems(carList);
            ((SingleSpinner) findViewById(R.id.age_spinner)).setItems(carList);
            ((MultiSpinner) findViewById(R.id.colour_spinner)).setItems(carList);
            ((MultiSpinner) findViewById(R.id.trans_spinner)).setItems(carList);
            ((MultiSpinner) findViewById(R.id.fuel_spinner)).setItems(carList);
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
        mCarsAdapter.loadObjects();
    }

    @Override
    public void onAgeSelected(int maxAge) {
        mSharedPrefs.edit().putInt("maxAge", maxAge).apply();
        mCarsAdapter.loadObjects();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSharedPrefs.edit().clear().apply();
    }

    // TODO Add menu button to manually refresh car listing

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private static Context sContext;

    public static Context getContext() {
        return sContext;
    }
}