package com.mobanic.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.mobanic.R;
import com.mobanic.adapters.CarsAdapter;
import com.mobanic.model.CarMobanic;
import com.mobanic.model.CarParsed;
import com.mobanic.views.PriceSeekBar;
import com.mobanic.views.SpinnerMultiple;
import com.mobanic.views.SpinnerSingle;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class MasterActivity extends AppCompatActivity
        implements SpinnerSingle.ChoiceListener, SpinnerMultiple.ChoiceListener {

    private static final String TAG = MasterActivity.class.getSimpleName();
    private CarsAdapter mCarsAdapter;
    public boolean initialStart = true;
    private boolean mMakesUpdated;
    private boolean mModelsUpdated;
    private boolean mForceNetwork;
    private SharedPreferences mSharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupActionBar(); // adds button to open search


        mCarsAdapter = new CarsAdapter(this);

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
                refreshCarList();
            }
        });

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sContext = this;

        refreshCarList();
    }

    public void refreshCarList() {
        new RefreshCarsTask().execute();
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

    private class RefreshCarsTask extends AsyncTask<Void, Void, List<ParseObject>> {
        @Override
        protected List<ParseObject> doInBackground(Void... voids) {
            try {
                if (initialStart) {
                    if (executeQueryForClass(CarMobanic.class).size() == 0
                            || executeQueryForClass(CarParsed.class).size() == 0) {
                        mForceNetwork = true;
                    }
                }
                List<ParseObject> carList = new ArrayList<>();
                carList.addAll(executeQueryForClass(CarMobanic.class));
                carList.addAll(executeQueryForClass(CarParsed.class));
                return carList;
            } catch (ParseException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<ParseObject> carList) {
            if (carList != null) {
                ParseObject.pinAllInBackground(carList);
                mCarsAdapter.clear();
                for (int i = 0; i < 100; i++) {
                    try {
                        mCarsAdapter.add(carList.get(i));
                    } catch (IndexOutOfBoundsException e) {
                        break;
                    }
                }
                mCarsAdapter.sort(mComparator);
                updateSearchPanel(carList);
                initialStart = false;
                findViewById(R.id.spinner).setVisibility(View.GONE);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public List<ParseObject> executeQueryForClass(Class parseClass) throws ParseException {
        Set<String> makes = mSharedPrefs.getStringSet("Make", null);
        Set<String> models = mSharedPrefs.getStringSet("Model", null);
        Set<String> colors = mSharedPrefs.getStringSet("Colour", null);
        Set<String> transTypes = mSharedPrefs.getStringSet("Transmission", null);
        Set<String> fuelTypes = mSharedPrefs.getStringSet("Fuel Type", null);
        int minPrice = mSharedPrefs.getInt("minPrice", -1);
        int maxPrice = mSharedPrefs.getInt("maxPrice", -1);
        int maxAge = mSharedPrefs.getInt("maxAge", -1);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(parseClass);
        query.setLimit(300);
        if (!(mForceNetwork || (initialStart && isWifi()))) {
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
        return query.find();
    }

    public void updateSearchPanel(List<ParseObject> carList) {
        if (initialStart) {
            ((SpinnerMultiple) findViewById(R.id.make_spinner)).setItems(carList);
        }
        if (initialStart || mMakesUpdated) {
            ((SpinnerMultiple) findViewById(R.id.model_spinner)).setItems(carList);
        }
        if (initialStart || mMakesUpdated || mModelsUpdated) {
            ((PriceSeekBar) findViewById(R.id.price_seekbar)).setItems(carList);
            ((SpinnerSingle) findViewById(R.id.age_spinner)).setItems(carList);
            ((SpinnerMultiple) findViewById(R.id.colour_spinner)).setItems(carList);
            ((SpinnerMultiple) findViewById(R.id.trans_spinner)).setItems(carList);
            ((SpinnerMultiple) findViewById(R.id.fuel_spinner)).setItems(carList);
        }
        mMakesUpdated = false;
        mModelsUpdated = false;
        mForceNetwork = false;
    }

    @Override
    public void onFilterSet(String key, Set<String> values) {
        if (key.equals("Make")) {
            mMakesUpdated = true;
            mSharedPrefs.edit().clear().apply();
        } else if (key.equals("Model")) {
            mModelsUpdated = true;
            Set<String> makes = mSharedPrefs.getStringSet("Make", null);
            mSharedPrefs.edit().clear().apply();
            mSharedPrefs.edit().putStringSet("Make", makes).apply();
        }
        mSharedPrefs.edit().putStringSet(key, values).apply();
        refreshCarList();
    }

    @Override
    public void onAgeSelected(int maxAge) {
        mSharedPrefs.edit().putInt("maxAge", maxAge).apply();
        refreshCarList();
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

    private boolean isWifi() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo wimax = cm.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);
        return (wifi != null && wifi.isConnected())
                || (wimax != null && wimax.isConnected());
    }

    private Comparator<ParseObject> mComparator = new Comparator<ParseObject>() {
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

    private static Context sContext;

    public static Context getContext() {
        return sContext;
    }

    public static class PushReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            MasterActivity a = (MasterActivity) MasterActivity.getContext();
            a.mForceNetwork = true;
            a.refreshCarList();
        }
    }
}