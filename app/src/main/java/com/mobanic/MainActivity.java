package com.mobanic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.util.Set;

import static com.mobanic.views.MultiSpinner.SearchFiltersListener;

public class MainActivity extends AppCompatActivity implements SearchFiltersListener {

    private CarsAdapter mCarsAdapter;
    private SharedPreferences mSharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Add button to open search
        setupActionBar();

        mCarsAdapter = new CarsAdapter(this, getQueryFactory());

        ListView lv = (ListView) findViewById(R.id.cars_listview);
        lv.setAdapter(mCarsAdapter);
        lv.setEmptyView(findViewById(R.id.empty));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Car car = mCarsAdapter.getItem(position);

                Intent i = new Intent(MainActivity.this, DetailActivity.class);
                i.putExtra("car_id", car.getObjectId());
                i.putExtra("car_position", position + 1);
                startActivity(i);
            }
        });

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
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

    public ParseQueryAdapter.QueryFactory<Car> getQueryFactory() {
        return new ParseQueryAdapter.QueryFactory<Car>() {
            public ParseQuery<Car> create() {
                ParseQuery<Car> query = ParseQuery.getQuery(Car.class);
                query.orderByDescending("createdAt");
                try {
                    Set<String> makes = mSharedPrefs.getStringSet("Make", null);
                    Set<String> models = mSharedPrefs.getStringSet("Model", null);
                    Set<String> colors = mSharedPrefs.getStringSet("Colour", null);
                    Set<String> transTypes = mSharedPrefs.getStringSet("Transmission", null);
                    Set<String> fuelTypes = mSharedPrefs.getStringSet("Fuel Type", null);

                    if (makes != null && makes.size() > 0) {
                        query.whereContainedIn("make", makes);
                    }
                    if (models != null && models.size() > 0) {
                        query.whereContainedIn("model", models);
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
                    /*
                    if (minPrice != -1) {
                        query.whereGreaterThanOrEqualTo("price", minPrice * 1000);
                    }
                    if (maxPrice != -1) {
                        query.whereLessThanOrEqualTo("price", maxPrice * 1000);
                    }
                    if (maxAge > 0 && maxAge < 11) {
                        query.whereGreaterThanOrEqualTo("year", (2015 - maxAge));
                    }
                    */
                } catch (NullPointerException e) {
                    // filters not set, just continue
                }
                return query;
            }
        };
    }

    @Override
    public void onFilterSet(String key, Set<String> values) {
        mSharedPrefs.edit().putStringSet(key, values).apply();
        mCarsAdapter.loadObjects();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSharedPrefs.edit().clear().apply();
    }
}