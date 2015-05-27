package com.mobanic.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.mobanic.R;
import com.mobanic.model.CarMobanic;
import com.mobanic.model.CarParsed;
import com.mobanic.views.RatioImageView;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class DetailActivity extends AppCompatActivity {

    private ParseObject mCar;
    private String mCarId;
    private int mCarPosition;

    private Intent mShareIntent;
    private Uri mImageUri;

    private List<String> mGalleryImageUrls;
    private List<String> mFeatureList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getConfiguration().orientation == 2) { // landscape
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_detail);

        if (getIntent() != null) {
            mCarId = getIntent().getStringExtra("car_id");
            mCarPosition = getIntent().getIntExtra("car_position", -1);
        } else if (savedInstanceState != null) {
            mCarId = savedInstanceState.getString("car_id");
            mCarPosition = savedInstanceState.getInt("car_position");
        }


        updateCarDetails();

        if (findViewById(R.id.fab_contact) != null) {
            findViewById(R.id.fab_contact).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(DetailActivity.this, ContactActivity.class);
                    i.putExtra("car_id", mCarId);
                    i.putExtra("car_position", mCarPosition);
                    startActivity(i);
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putString("car_id", mCarId);
        outState.putInt("car_position", mCarPosition);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    private void updateCarDetails() {
        ParseQuery query;
        if (mCarId.length() == 10) {
            query = ParseQuery.getQuery(CarMobanic.class);
        } else {
            query = ParseQuery.getQuery(CarParsed.class);
        }
        query.fromLocalDatastore();
        if (mCarId.length() == 10) {
            query.whereEqualTo("objectId", mCarId);
        } else {
            query.whereEqualTo("id", Integer.parseInt(mCarId));
        }
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject car, ParseException e) {
                if (e != null) {
                    Toast.makeText(DetailActivity.this, e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                } else if (car.getBoolean("isSold")) {
                    Toast.makeText(DetailActivity.this, getString(R.string.sold),
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                mCar = car;

                String make = car.getString("make");
                String model = car.getString("model");

                String title = make + " " + model;
                if (title.length() > 20) {
                    title = model;
                }
                getSupportActionBar().setTitle(make);

                populateGalleryList();

                if (findViewById(R.id.make) == null) return;

                ((TextView) findViewById(R.id.make)).setText(mCar.getString("make"));
                ((TextView) findViewById(R.id.model)).setText(mCar.getString("model"));
                ((TextView) findViewById(R.id.year)).setText(mCar.getInt("year") + "");
                String mileage = NumberFormat.getNumberInstance(Locale.UK).format(mCar.getInt("mileage"));
                ((TextView) findViewById(R.id.mileage)).setText(mileage);
                ((TextView) findViewById(R.id.fuelType)).setText(mCar.getString("fuelType"));
                ((TextView) findViewById(R.id.color)).setText(mCar.getString("color"));
                TextView locationTextView = (TextView) findViewById(R.id.location);
                locationTextView.setText(mCar.getString("location"));
                locationTextView.setPaintFlags(locationTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                locationTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showOnMap(mCar.getString("location"));
                    }
                });

                setCoverImage();

                String url = car.getString("coverImage");
                if (url == null && car.getParseFile("coverImage") != null) {
                    url = car.getParseFile("coverImage").getUrl();
                }
                new SetShareIntentTask().execute(title, url);
            }
        });
    }

    private void populateGalleryList() {
        mFeatureList = mCar.getList("features");
        String engineStr = NumberFormat.getNumberInstance(Locale.UK)
                .format(mCar.getInt("engine")) + "\u2009" + "cc";
        if (engineStr.length() > 4) {
            mGalleryImageUrls = mCar.getList("galleryImages");
            setGalleryImages();
            fillOutSpecs();
            fillOutFeatures();
        } else {
            mGalleryImageUrls = new ArrayList<>();
            if (mCarId.length() == 10) {
                ParseQuery<ParseObject> query = mCar.getRelation("galleryImage").getQuery();
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> images, ParseException e) {
                        if (e == null && images.size() > 0) {
                            for (ParseObject image : images) {
                                String url = image.getParseFile("image").getUrl();
                                mGalleryImageUrls.add(url);
                            }
                            setGalleryImages();
                        } else {
                            findViewById(R.id.gallery_header).setVisibility(View.GONE);
                            findViewById(R.id.gallery_placeholder).setVisibility(View.GONE);
                            findViewById(R.id.spinner).setVisibility(View.GONE);
                        }
                    }
                });
                fillOutSpecs();
                fillOutFeatures();
            }
            mFeatureList = new ArrayList<>();
        }

        if (mCarId.length() < 10) {
            setGalleryImages();
            fillOutSpecs();
            fillOutFeatures();
        }
    }

    private class SetShareIntentTask extends AsyncTask<String, Void, Intent> {
        @Override
        protected Intent doInBackground(String... strings) {

            String title = strings[0];
            String urlStr = strings[1];

            Bitmap bitmap = null;
            try {
                URL url = new URL(urlStr);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                Log.e("DetailActivity", "Failed to attach image to share intent");
            }

            if (getContentResolver() != null && bitmap != null) {
                String imagePath = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "title", null);
                if (imagePath != null) {
                    mImageUri = Uri.parse(imagePath);
                }
            }

            mShareIntent = new Intent();
            mShareIntent.setAction(Intent.ACTION_SEND);
            mShareIntent.putExtra(Intent.EXTRA_SUBJECT, title + " - Mobanic");
            mShareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this car I found! Care for your own test drive? - https://goo.gl/P2BUBs");
            mShareIntent.putExtra("sms_body", "Check out this car I found! Care for your own test drive? - https://goo.gl/P2BUBs");
            if (mImageUri != null) {
                mShareIntent.putExtra(Intent.EXTRA_STREAM, mImageUri);
            }
            mShareIntent.setType("text/plain");
            mShareIntent.setType("image/*");

            return null;
        }

        @Override
        protected void onPostExecute(Intent intent) {
            super.onPostExecute(intent);

            invalidateOptionsMenu();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mImageUri != null) {
            getContentResolver().delete(mImageUri, null, null);
        }
    }

    private void setCoverImage() {
        String url = mCar.getString("coverImage");
        if (url == null) {
            url = mCar.getParseFile("coverImage").getUrl();
        }

        RatioImageView imageView = (RatioImageView) findViewById(R.id.image);
        Picasso.with(this).load(url).fit().centerCrop().into(imageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);

        MenuItem item = menu.findItem(R.id.menu_item_share);
        ShareActionProvider shareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if (shareActionProvider != null & mShareIntent != null) {
            shareActionProvider.setShareIntent(mShareIntent);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return (true);
        }
        return super.onOptionsItemSelected(item);
    }

    private void setGalleryImages() {
        final ViewFlipper flipper = (ViewFlipper) findViewById(R.id.flipper);
        flipper.startFlipping();
        flipper.setInAnimation(AnimationUtils.loadAnimation(DetailActivity.this,
                android.R.anim.fade_in));
        flipper.setOutAnimation(AnimationUtils.loadAnimation(DetailActivity.this,
                android.R.anim.fade_out));
        if (getResources().getConfiguration().orientation == 2) { // landscape
            flipper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    flipper.showNext();
                }
            });
        } else {
            flipper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            });
        }

        flipper.removeAllViews();
        for (final String url : mGalleryImageUrls) {
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
        if (mGalleryImageUrls.size() == 1) {
            flipper.setOnClickListener(null);
            flipper.stopFlipping();
        }
    }

    public void closePreview(View view) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onBackPressed() {
        if (getResources().getConfiguration().orientation == 2) { // landscape
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            super.onBackPressed();
        }
    }

    private void fillOutSpecs() {
        if (findViewById(R.id.mileage) == null) return;

        String mileage = NumberFormat.getNumberInstance(Locale.UK).format(mCar.getInt("mileage"));
        ((TextView) findViewById(R.id.mileage)).setText(mileage);
        ((TextView) findViewById(R.id.previousOwners)).setText(mCar.getInt("previousOwners") + "");
        int engine = mCar.getInt("engine");
        if (engine == 0) {
            engine = Integer.parseInt(mCar.getString("engine").replace("cc", ""));
        }
        ((TextView) findViewById(R.id.engine)).setText(NumberFormat.getNumberInstance(Locale.UK).format(engine) + "\u2009" + "cc");
        String transType = mCar.getString("transType");
        if (transType == null) {
            transType = mCar.getString("transmission");
        }
        ((TextView) findViewById(R.id.transmission)).setText(transType);
    }

    public void showOnMap(String location) {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + location);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(this, "Install Google Maps to see the map", Toast.LENGTH_SHORT).show();
        }
    }

    private void fillOutFeatures() {
        if (findViewById(R.id.features_container) == null) return;

        LinearLayout featuresContainer = (LinearLayout) findViewById(R.id.features_container);
        if (mFeatureList != null) {
            for (String feature : mFeatureList) {
                TextView textView = (TextView) View.inflate(
                        DetailActivity.this,
                        R.layout.list_item_feature,
                        null);
                textView.setText(feature);
                featuresContainer.addView(textView);
            }
        } else {
            findViewById(R.id.features_header).setVisibility(View.GONE);
            findViewById(R.id.features_container).setVisibility(View.GONE);
        }
    }
}