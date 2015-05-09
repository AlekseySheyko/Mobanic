package com.mobanic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.mobanic.views.PriceSeekBar;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.util.List;
import java.util.Set;

import static com.mobanic.views.MultiSpinner.SearchFiltersListener;

public class MainActivity extends AppCompatActivity implements SearchFiltersListener {

    private ParseQueryAdapter<ParseObject> mCarsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupActionBar();

        updateCarsAdapter();

        ListView lv = (ListView) findViewById(R.id.cars_listview);
        lv.setAdapter(mCarsAdapter);
        lv.setEmptyView(findViewById(R.id.empty));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ParseObject car = mCarsAdapter.getItem(position);

                Intent i = new Intent(MainActivity.this, DetailActivity.class);
                i.putExtra("car_id", car.getObjectId());
                i.putExtra("car_position", position + 1);
                startActivity(i);
            }
        });
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

    private void setupPriceSeekBar() {
        priceSeekBar.setOnRangeSeekBarChangeListener(
                new PriceSeekBar.OnRangeSeekBarChangeListener<Integer>() {
                    @Override
                    public void onRangeSeekBarValuesChanged(PriceSeekBar<?> bar, Integer minPrice,
                                                            Integer maxPrice) {
                        updateCarsAdapter();
                    }
                });
    }

    public void updateCarsAdapter(boolean fromNetwork) {
        mCarsAdapter = new CarsAdapter(this, getQueryFactory(fromNetwork));
        mCarsAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<ParseObject>() {
            @Override
            public void onLoaded(final List<ParseObject> cars, Exception e) {
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
    }

    public ParseQuery<ParseObject> getQuery(boolean fromNetwork) {
        ParseQuery<ParseObject> query = new ParseQuery<>("Car");
        query.orderByDescending("createdAt");
        if (!fromNetwork) {
            query.fromLocalDatastore();
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
            query.whereGreaterThanOrEqualTo("price", minPrice * 1000);
        }
        if (maxPrice != -1) {
            query.whereLessThanOrEqualTo("price", maxPrice * 1000);
        }
        if (maxAge > 0 && maxAge < 11) {
            query.whereGreaterThanOrEqualTo("year", (2015 - maxAge));
        }

        mFirstLaunch = (makes.size() == 0 && models.size() == 0 && colors.size() == 0
                && transTypes.size() == 0 && fuelTypes.size() == 0 && minPrice == -1
                && maxPrice == -1 && maxAge == -1);

        return query;
    }

    private void updateSearchPanel() {
        makeSpinner.setItems(makes);
        modelSpinner.setItems(models);
        colorSpinner.setItems(colors);
        transSpinner.setItems(transTypes);
        fuelTypeSpinner.setItems(fuelTypes);
    }

    @Override
    public void onFilterSet(String filterKey, Set<String> selectedValues) {
        updateCarsAdapter();
    }


    private BroadcastReceiver mPushReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateCarsAdapter(true);
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.parse.push.intent.RECEIVE");
        filter.addAction("com.parse.push.intent.DELETE");
        filter.addAction("com.parse.push.intent.OPEN");

        registerReceiver(mPushReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mPushReceiver);
    }
}