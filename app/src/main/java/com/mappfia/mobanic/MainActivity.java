package com.mappfia.mobanic;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import static com.mappfia.mobanic.MultiSpinner.MultiSpinnerListener;

public class MainActivity extends ActionBarActivity
        implements MultiSpinnerListener {

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private Toolbar mToolbar;
    private CarsAdapter mCarsAdapter;
    private MultiSpinner mMakeSpinner;

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
                PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit()
                        .putString("car_id", carId).apply();

                Intent intent = new Intent(MainActivity.this,
                        DetailActivity.class);
                startActivity(intent);

                // TODO: If selected car is sold, show similar models in listing
            }
        });

        populateCarsList(false);
    }

    private void populateCarsList(boolean fromNetwork) {
        final ListView carsListView = (ListView) findViewById(R.id.listview_cars);
        final FrameLayout progressBar = (FrameLayout) findViewById(R.id.progressBar);

        carsListView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Car");
        if (!fromNetwork) {
            query.fromLocalDatastore();
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
                    List<String> makeStrings = new ArrayList<>();

                    mCarsAdapter.clear();
                    for (ParseObject car : cars) {
                        mCarsAdapter.add(car);
                        car.pinInBackground();
                        makeStrings.add(car.getString("make"));
                    }

                    mMakeSpinner = (MultiSpinner) findViewById(R.id.make_spinner);
                    mMakeSpinner.setItems(MainActivity.this, makeStrings);
                }
            }
        });
    }

    @Override
    public void onItemsSelected(boolean[] selected) {

    }

    private void setupActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        String[] navItems = getResources().getStringArray(R.array.categories);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new NavigationDrawerAdapter(this, navItems));
        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar, R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerSlide(View drawerView, float slideOffset) {
                float moveFactor = (mDrawerList.getWidth() * slideOffset);
                LinearLayout container = (LinearLayout) findViewById(R.id.container);
                container.setTranslationX(moveFactor);
            }
        };
        drawerToggle.syncState();
        mDrawerLayout.setDrawerListener(drawerToggle);
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

    private void selectItem(int position) {
        Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();

        mDrawerLayout.closeDrawer(Gravity.START);
    }

    private class NavigationDrawerAdapter extends ArrayAdapter<String> {
        public NavigationDrawerAdapter(Context context, String[] navItems) {
            super(context, 0, navItems);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String itemName = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.nav_drawer_item, parent, false);
            }
            TextView navLabel = (TextView) convertView.findViewById(R.id.text);
            navLabel.setText(itemName);

            return convertView;
        }
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

}
