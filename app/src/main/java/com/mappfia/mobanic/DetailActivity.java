package com.mappfia.mobanic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import java.util.List;


public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        String carId = null;
        if (getIntent() != null) {
            carId = getIntent().getStringExtra("car_id");
        } else if (savedInstanceState != null) {
            carId = savedInstanceState.getString("car_id");
        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Car");
        query.fromLocalDatastore();
        query.getInBackground(carId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject car, ParseException e) {
                String make = car.getString("make");
                String model = car.getString("model");

                String title = make + " " + model;
                if (title.length() > 20) {
                    title = model;
                }
                getSupportActionBar().setTitle(title);

                setCoverImage(car);
                setGalleryImages(car);
                fillOutSpecs(car);
                fillOutFeatures(car);
            }
        });

        findViewById(R.id.button_contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DetailActivity.this, ContactActivity.class));
            }
        });
    }

    private void setCoverImage(ParseObject car) {
        String url = car.getParseFile("coverImage").getUrl();

        RatioImageView imageView = (RatioImageView) findViewById(R.id.image);
        Picasso.with(this).load(url).fit().centerCrop().into(imageView);
    }

    private void setGalleryImages(ParseObject car) {
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

        ParseQuery<ParseObject> query = car.getRelation("galleryImage").getQuery();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> images, ParseException e) {
                if (e == null) {
                    flipper.removeAllViews();
                    for (ParseObject image : images) {
                        String url = image.getParseFile("image").getUrl();

                        RatioImageView imageView = (RatioImageView) flipper.inflate(
                                DetailActivity.this,
                                R.layout.gallery_image,
                                null);
                        Picasso.with(DetailActivity.this)
                                .load(url)
                                .fit()
                                .centerCrop()
                                .into(imageView);
                        flipper.addView(imageView);
                    }
                }
            }
        });
    }

    private void fillOutSpecs(ParseObject car) {
        ((TextView) findViewById(R.id.make)).setText(car.getString("make"));
        ((TextView) findViewById(R.id.model)).setText(car.getString("model"));
        ((TextView) findViewById(R.id.year)).setText(car.getInt("year") + "");
        // TODO: Format mileage properly (add space and "km" label)
        ((TextView) findViewById(R.id.mileage)).setText(car.getInt("mileage") + "");
        ((TextView) findViewById(R.id.previousOwners)).setText(car.getInt("previousOwners") + "");
        ((TextView) findViewById(R.id.engine)).setText(car.getString("engine"));
        ((TextView) findViewById(R.id.transmission)).setText(car.getString("transmission"));
        ((TextView) findViewById(R.id.fuelType)).setText(car.getString("fuelType"));
        ((TextView) findViewById(R.id.color)).setText(car.getString("color"));
        ((TextView) findViewById(R.id.location)).setText(car.getString("location"));
    }

    private void fillOutFeatures(ParseObject car) {
        List<String> features = car.getList("features");

        LinearLayout featuresContainer = (LinearLayout) findViewById(R.id.features_container);
        for (String feature : features) {
            TextView textView = (TextView) featuresContainer.inflate(
                    DetailActivity.this,
                    R.layout.feature_list_item,
                    null);
            textView.setText(feature);
            featuresContainer.addView(textView);
        }
    }
}
