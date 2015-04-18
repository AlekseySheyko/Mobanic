package com.mappfia.mobanic;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mappfia.mobanic.MultiSpinner.MakesSpinnerListener;
import static com.mappfia.mobanic.RangeSeekBar.OnRangeSeekBarChangeListener;

public class MainActivity extends ActionBarActivity
        implements MakesSpinnerListener {

    private Toolbar mToolbar;
    private CarsAdapter mCarsAdapter;

    private MultiSpinner mMakeSpinner;
    private MultiSpinner mModelSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupActionBar();


        mCarsAdapter = new CarsAdapter(this);

        final ListView carsListView = (ListView) findViewById(R.id.listview_cars);
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

                // TODO: If selected car is sold, show similar models in listing
            }
        });

        populateCarsList(false);
    }

    private void populateCarsList(boolean fromNetwork) {
        populateCarsList(fromNetwork, null, null, null, null);
    }

    private void populateCarsList(boolean fromNetwork, String filterKey, List<String> filterValues) {
        populateCarsList(fromNetwork, filterKey, filterValues, null, null);
    }

    private void populateCarsList(boolean fromNetwork, Integer minPrice, Integer maxPrice) {
        populateCarsList(fromNetwork, null, null, minPrice, maxPrice);
    }

    private void populateCarsList(boolean fromNetwork, final String filterKey, final List<String> filterValues, final Integer minPrice, final Integer maxPrice) {
        final ListView carsListView = (ListView) findViewById(R.id.listview_cars);
        final FrameLayout progressBar = (FrameLayout) findViewById(R.id.progressBar);

        carsListView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Car");
        if (!fromNetwork) {
            query.fromLocalDatastore();
        }
        if (filterKey != null && filterValues != null) {
            query.whereContainedIn(filterKey, filterValues);
        }
        if (minPrice != null) {
            query.whereGreaterThanOrEqualTo("price", minPrice * 1000);
        }
        if (maxPrice != null) {
            query.whereLessThanOrEqualTo("price", maxPrice * 1000);
        }
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> cars, ParseException e) {
                if (cars.size() == 0) {
                    if (isOnline()) {
                        populateCarsList(true);
                    } else {
                        Toast.makeText(MainActivity.this,
                                "Connect to a network to load cars list", Toast.LENGTH_LONG).show();
                    }
                    return;
                }

                progressBar.setVisibility(View.GONE);
                carsListView.setVisibility(View.VISIBLE);

                if (e == null) {
                    List<String> makeItemsList = new ArrayList<>();
                    List<String> modelItemsList = new ArrayList<>();
                    List<Integer> priceList = new ArrayList<>();

                    mCarsAdapter.clear();
                    for (ParseObject car : cars) {
                        mCarsAdapter.add(car);
                        car.pinInBackground();
                        makeItemsList.add(car.getString("make"));
                        modelItemsList.add(car.getString("model"));
                        priceList.add(car.getInt("price"));
                    }

                    if (filterKey == null && filterValues == null && minPrice == null && maxPrice == null) {
                        mMakeSpinner = (MultiSpinner) findViewById(R.id.make_spinner);
                        mMakeSpinner.setItems(MainActivity.this, "Make", makeItemsList);

                        mModelSpinner = (MultiSpinner) findViewById(R.id.model_spinner);
                        mModelSpinner.setItems(MainActivity.this, "Model", modelItemsList);

                        Integer minPrice = Collections.min(priceList);
                        minPrice = minPrice / 1000 - 1;
                        Integer maxPrice = Collections.max(priceList);
                        maxPrice = maxPrice / 1000 + 1;

                        RangeSeekBar<Integer> rangeSeekBar = (RangeSeekBar<Integer>) findViewById(R.id.price_selector);
                        rangeSeekBar.setRangeValues(minPrice, maxPrice);
                        rangeSeekBar.setSelectedMinValue(minPrice);
                        rangeSeekBar.setSelectedMaxValue(maxPrice + 1);

                        rangeSeekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {
                            @Override
                            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minPrice, Integer maxPrice) {
                                populateCarsList(false, minPrice, maxPrice);
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void onFilterSet(String filterKey, List<String> selectedValues) {
        populateCarsList(false, filterKey.toLowerCase(), selectedValues);
        // TODO: Create menu item in action bar to reset filter
    }

    private void setupActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                mToolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();
        // TODO: Remove
        drawerLayout.openDrawer(Gravity.START);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mToolbar.inflateMenu(R.menu.menu_main);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // TODO: Replace the refresh button with GCM live updates from server
        if (id == R.id.action_refresh) {
            populateCarsList(true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

}
