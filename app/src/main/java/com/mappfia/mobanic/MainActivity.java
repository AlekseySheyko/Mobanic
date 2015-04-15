package com.mappfia.mobanic;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        setupNavigationDrawer();


        CarsAdapter adapter = new CarsAdapter(this);

        adapter.add(new Car("Land Rover", "Range Rover Sport", 82875, "http://www.themotorreport.com.au/content/image/2/0/2014_range_rover_sport_australia_01_1-1020-mc:819x819.jpg"));
        adapter.add(new Car("Land Rover", "Range Rover Sport", 89475, "http://o.aolcdn.com/hss/storage/midas/ff10ff0b8023231885fcbd74f6d32ed8/200047427/lead22-2014-lr-range-rover-sport-review.jpg"));
        adapter.add(new Car("Land Rover", "Range Rover Sport", 99875, "http://lexani.com/media/images/rendered/2014_Land%20Rover_Range%20Rover%20Sport_740_v1.jpg"));

        ListView carListView = (ListView) findViewById(R.id.listview_cars);
        carListView.setAdapter(adapter);
        carListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // TODO: Pass car id in intent extra to retrieve the details for the current car from database
                startActivity(new Intent(MainActivity.this, DetailActivity.class));
            }
        });
    }

    private void setupNavigationDrawer() {
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

    private void selectItem(int position) {
        /*
        String[] categories = getResources().getStringArray(R.array.categories);
        String category = categories[position];

        Intent intent = new Intent(this, CategoryActivity.class);
        intent.putExtra("category", category);
        startActivity(intent);
        */
        Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();

        mDrawerLayout.closeDrawer(Gravity.START);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mToolbar.inflateMenu(R.menu.menu_main);
        return true;
    }

    private class NavigationDrawerAdapter extends ArrayAdapter<String> {
        public NavigationDrawerAdapter(Context context, String[] navItems) {
            super(context, 0, navItems);
        }

        @Override public View getView(int position, View convertView, ViewGroup parent) {
            String itemName = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.nav_drawer_item, parent, false);
            }
            TextView navLabel = (TextView) convertView.findViewById(R.id.text);
            navLabel.setText(itemName);

            return convertView;
        }
    }

}
