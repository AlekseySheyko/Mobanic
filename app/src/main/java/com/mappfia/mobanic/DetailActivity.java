package com.mappfia.mobanic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.melnykov.fab.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import java.util.List;


public class DetailActivity extends ActionBarActivity {

    private ParseObject mCar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(this);
        String carId = sharedPrefs.getString("car_id", null);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Car");
        query.fromLocalDatastore();
        query.getInBackground(carId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject car, ParseException e) {
                mCar = car;

                String title = mCar.getString("make") + " " +
                        mCar.getString("model");
                if (title.length() > 20) {
                    title = mCar.getString("model");
                }
                getSupportActionBar().setTitle(title);

                setCoverImage();
                setupImageCarousel();
                fillOutSpecifications();
                fillOutFeatures();
            }
        });

        FloatingActionButton actionButton =
                (FloatingActionButton) findViewById(R.id.fab);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DetailActivity.this, ContactActivity.class));
            }
        });
    }

    private void setCoverImage() {
        findViewById(R.id.progressBar).setVisibility(View.GONE);

        RatioImageView imageView = (RatioImageView) findViewById(R.id.image);
        imageView.setVisibility(View.VISIBLE);
        Picasso.with(this)
                .load(mCar.getParseFile("coverImage").getUrl())
                .fit()
                .centerCrop()
                .into(imageView);
    }

    private void setupImageCarousel() {
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

        ParseQuery<ParseObject> query = mCar.getRelation("galleryImage").getQuery();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> images, ParseException e) {
                if (e == null && images != null) {
                    findViewById(R.id.gallery_placeholder).setVisibility(View.GONE);
                    for (ParseObject image : images) {
                        RatioImageView galleryImageView = (RatioImageView)
                                flipper.inflate(DetailActivity.this, R.layout.gallery_image, null);
                        flipper.addView(galleryImageView);
                        Picasso.with(DetailActivity.this)
                                .load(image.getParseFile("image").getUrl())
                                .fit()
                                .centerCrop()
                                .into(galleryImageView);
                    }
                }
            }
        });
    }

    private void fillOutSpecifications() {
        ((TextView) findViewById(R.id.make)).setText(mCar.getString("make"));
        ((TextView) findViewById(R.id.model)).setText(mCar.getString("model"));
        ((TextView) findViewById(R.id.year)).setText(mCar.getInt("year") + "");
        ((TextView) findViewById(R.id.mileage)).setText(mCar.getInt("mileage") + "");
        ((TextView) findViewById(R.id.previousOwners)).setText(mCar.getInt("previousOwners") + "");
        ((TextView) findViewById(R.id.engine)).setText(mCar.getString("engine"));
        ((TextView) findViewById(R.id.transmission)).setText(mCar.getString("transmission"));
        ((TextView) findViewById(R.id.fuelType)).setText(mCar.getString("fuelType"));
        ((TextView) findViewById(R.id.color)).setText(mCar.getString("color"));
        ((TextView) findViewById(R.id.location)).setText(mCar.getString("location"));
    }

    private void fillOutFeatures() {
        List<String> features = mCar.getList("features");

        LinearLayout featuresContainer = (LinearLayout) findViewById(R.id.features_container);
        for (String feature : features) {
            TextView featureTextView = (TextView)
                    featuresContainer.inflate(DetailActivity.this, R.layout.feature_list_item, null);
            featuresContainer.addView(featureTextView);
            featureTextView.setText(feature);
        }
    }
}
