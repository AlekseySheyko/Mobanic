package com.mappfia.mobanic.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.mappfia.mobanic.R;
import com.mappfia.mobanic.utils.RatioImageView;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import java.util.List;


public class DetailActivity extends ActionBarActivity {

    private ParseObject mCar;
    private String mCarId;

    public static Context mContext;
    private SharedPreferences mSharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mContext = this;

        if (getIntent() != null) {
            mCarId = getIntent().getStringExtra("car_id");
        } else if (savedInstanceState != null) {
            mCarId = savedInstanceState.getString("car_id");
        }

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        updateCarsList(false);

        findViewById(R.id.button_contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DetailActivity.this, ContactActivity.class));
            }
        });
    }

    private void updateCarsList(boolean fromNetwork) {
        if (mCarId == null) return;

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Car");
        if (!fromNetwork && !mSharedPrefs.getBoolean("update", false)) {
            query.fromLocalDatastore();
        } else {
            mSharedPrefs.edit().putBoolean("update", false).apply();
        }
        query.getInBackground(mCarId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject car, ParseException e) {
                mCar = car;

                boolean isSold = car.getBoolean("isSold");
                if (isSold) {
                    Toast.makeText(DetailActivity.this,
                            "Sorry, this car was sold recently", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(DetailActivity.this, MainActivity.class));
                }

                String make = car.getString("make");
                String model = car.getString("model");

                String title = make + " " + model;
                if (title.length() > 20) {
                    title = model;
                }
                getSupportActionBar().setTitle(title);

                setCoverImage();
                setGalleryImages();
                fillOutSpecs();
                fillOutFeatures();
            }
        });
}

    private void setCoverImage() {
        String url = mCar.getParseFile("coverImage").getUrl();

        RatioImageView imageView = (RatioImageView) findViewById(R.id.image);
        Picasso.with(this).load(url).fit().centerCrop().into(imageView);
    }

    private void setGalleryImages() {
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
                if (e == null && images.size() > 0) {
                    flipper.removeAllViews();
                    for (ParseObject image : images) {
                        String url = image.getParseFile("image").getUrl();

                        RatioImageView imageView = (RatioImageView) View.inflate(
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
                } else {
                    findViewById(R.id.gallery_header).setVisibility(View.GONE);
                    findViewById(R.id.flipper).setVisibility(View.GONE);
                }
            }
        });
    }

    private void fillOutSpecs() {
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
        if (features != null) {
            for (String feature : features) {
                TextView textView = (TextView) View.inflate(
                        DetailActivity.this,
                        R.layout.feature_list_item,
                        null);
                textView.setText(feature);
                featuresContainer.addView(textView);
            }
        } else {
            findViewById(R.id.features_header).setVisibility(View.GONE);
            findViewById(R.id.features_container).setVisibility(View.GONE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("car_id", mCar.getObjectId());
        super.onSaveInstanceState(outState);
    }

    public static Context getContext() {
        return mContext;
    }

    public static class PushReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                ((DetailActivity) DetailActivity.getContext()).updateCarsList(true);
                SharedPreferences sharedPrefs =
                        PreferenceManager.getDefaultSharedPreferences(context);
                sharedPrefs.edit().putBoolean("update", true).apply();
            } catch (Exception e) {
                Log.d("MainActivity", "Can't get activity context to update content. " +
                        "Just skip, will be updated in a moment.");
            }
        }
    }
}
