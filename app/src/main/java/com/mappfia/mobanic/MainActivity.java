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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.mappfia.mobanic.MultiSpinner.MakesSpinnerListener;
import static com.mappfia.mobanic.RangeSeekBar.OnRangeSeekBarChangeListener;

public class MainActivity extends ActionBarActivity
        implements MakesSpinnerListener {

    private Toolbar mToolbar;
    private CarsAdapter mCarsAdapter;

    private MultiSpinner mMakeSpinner;
    private MultiSpinner mModelSpinner;
    private MultiSpinner mColorSpinner;

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
                    Set<String> makesList = new HashSet<>();
                    Set<String> modelsList = new HashSet<>();
                    Set<Integer> priceList = new HashSet<>();
                    Set<Integer> yearsList = new HashSet<>();
                    Set<String> colorList = new HashSet<>();

                    mCarsAdapter.clear();
                    for (ParseObject car : cars) {
                        mCarsAdapter.add(car);
                        car.pinInBackground();
                        makesList.add(car.getString("make"));
                        modelsList.add(car.getString("model"));
                        priceList.add(car.getInt("price"));
                        yearsList.add(car.getInt("year"));
                        colorList.add(car.getString("color"));
                    }

                    if (filterKey == null && filterValues == null && minPrice == null && maxPrice == null) {
                        mMakeSpinner = (MultiSpinner) findViewById(R.id.make_spinner);
                        mMakeSpinner.setItems(MainActivity.this, "Make", makesList);

                        mModelSpinner = (MultiSpinner) findViewById(R.id.model_spinner);
                        mModelSpinner.setItems(MainActivity.this, "Model", modelsList);

                        mColorSpinner = (MultiSpinner) findViewById(R.id.color_spinner);
                        mColorSpinner.setItems(MainActivity.this, "Color", colorList);

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
                                populateCarsList(false, minPrice, maxPrice);
                            }
                        });

                        ArrayAdapter<String> adapter = new SpinnerAdapter(MainActivity.this);

                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        for (Integer year : yearsList) {
                            adapter.add(year.toString());
                        }
                        adapter.add("Min. year");

                        Spinner spinner = (Spinner) findViewById(R.id.age_spinner);
                        spinner.setAdapter(adapter);
                        spinner.setSelection(adapter.getCount());
                    }
                }
            }
        });
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
                textView.setText("");
                textView.setHint(getItem(getCount()));
            }

            return v;
        }

        @Override
        public int getCount() {
            return super.getCount() - 1;
        }
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
