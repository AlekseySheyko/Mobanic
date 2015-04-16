package com.mappfia.mobanic;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.melnykov.fab.FloatingActionButton;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;


public class DetailActivity extends ActionBarActivity {

    private ParseObject mCar;
    private String mCarId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (getIntent() != null) {
            if (savedInstanceState == null) {
                mCarId = getIntent().getStringExtra("car_id");
            } else {
                mCarId = savedInstanceState.getString("car_id");
            }

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Car");
            if (!isOnline()) {
                query.fromLocalDatastore();
            }
            query.getInBackground(mCarId, new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject car, ParseException e) {
                    mCar = car;

                    getSupportActionBar().setTitle(mCar.getString("model"));

                    setCoverImage();
                    fillOutSpecifications();
                    setupImageCarousel();
                }
            });
        }

        FloatingActionButton actionButton =
                (FloatingActionButton) findViewById(R.id.fab);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DetailActivity.this, ContactActivity.class));
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putString("car_id", mCarId);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    private void setCoverImage() {
        RatioImageView imageView = (RatioImageView) findViewById(R.id.image);
        Picasso.with(this)
                // TODO: Restore not as URL, but as file, to show image even in offline mode
                .load(mCar.getParseFile("coverImage").getUrl())
                .fit()
                .centerCrop()
                .into(imageView);
    }

    private void fillOutSpecifications() {
        ((TextView) findViewById(R.id.make)).setText(mCar.getString("make"));
        ((TextView) findViewById(R.id.year)).setText(mCar.getInt("year") + "");
        ((TextView) findViewById(R.id.mileage)).setText(mCar.getInt("mileage") + "");
        ((TextView) findViewById(R.id.previousOwners)).setText(mCar.getInt("previousOwners") + "");
        ((TextView) findViewById(R.id.engine)).setText(mCar.getString("engine"));
        ((TextView) findViewById(R.id.transmission)).setText(mCar.getString("transmission"));
        ((TextView) findViewById(R.id.fuelType)).setText(mCar.getString("fuelType"));
        ((TextView) findViewById(R.id.color)).setText(mCar.getString("color"));
        ((TextView) findViewById(R.id.location)).setText(mCar.getString("location"));
    }

    private void setupImageCarousel() {
        RatioImageView imageView1 = (RatioImageView) findViewById(R.id.image1);
        RatioImageView imageView2 = (RatioImageView) findViewById(R.id.image2);
        RatioImageView imageView3 = (RatioImageView) findViewById(R.id.image3);
        RatioImageView imageView4 = (RatioImageView) findViewById(R.id.image4);
        RatioImageView imageView5 = (RatioImageView) findViewById(R.id.image5);

        Picasso.with(this)
                .load("http://www.themotorreport.com.au/content/image/2/0/2014_range_rover_sport_australia_01_1-1020-mc:819x819.jpg")
                .fit()
                .centerCrop()
                .into(imageView1);
        Picasso.with(this)
                .load("http://www.landroverusa.com/Images/L494_14_INT_LOC06_oa_2_293-80676_500x330.jpg?v=1")
                .fit()
                .centerCrop()
                .into(imageView2);
        Picasso.with(this)
                .load("http://www.landroverusa.com/Images/L494_14_INT_DET27_up_oa_2_293-91007_500x330.jpg?v=1")
                .fit()
                .centerCrop()
                .into(imageView3);
        Picasso.with(this)
                .load("http://www.landroverusa.com/Images/L494_14_STU_DET09_oa_2_293-80683_500x330.jpg?v=1")
                .fit()
                .centerCrop()
                .into(imageView4);
        Picasso.with(this)
                .load("http://www.landroverusa.com/Images/L494_14_EXT_STU07_fh_2_04_293-91143_500x330.jpg?v=1")
                .fit()
                .centerCrop()
                .into(imageView5);

        final ViewFlipper flipper = (ViewFlipper) findViewById(R.id.flipper);
        flipper.setInAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.fade_in));
        flipper.setOutAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.fade_out));
        flipper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipper.stopFlipping();
                flipper.showNext();
                flipper.startFlipping();
            }
        });
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
