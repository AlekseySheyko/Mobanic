package com.mobanic.activities;

import android.content.Intent;
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

import com.mobanic.CarFromKahn;
import com.mobanic.R;
import com.mobanic.views.RatioImageView;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class DetailActivity extends AppCompatActivity {

    private CarFromKahn mCar;
    private int mCarId;
    private int mCarPosition;

    private Intent mShareIntent;
    private Uri mImageUri;

    private ArrayList<String> mGalleryImageUrls;
    private ArrayList<String> mFeatureList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (getIntent() != null) {
            mCarId = getIntent().getIntExtra("car_id", -1);
            mCarPosition = getIntent().getIntExtra("car_position", -1);
        } else if (savedInstanceState != null) {
            mCarId = savedInstanceState.getInt("car_id");
            mCarPosition = savedInstanceState.getInt("car_position");
        }


        updateCarDetails();

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

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putInt("car_id", mCarId);
        outState.putInt("car_position", mCarPosition);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    private void updateCarDetails() {
        ParseQuery<CarFromKahn> query = ParseQuery.getQuery(CarFromKahn.class);
        query.fromLocalDatastore();
        query.whereEqualTo("id", mCarId);
        query.getFirstInBackground(new GetCallback<CarFromKahn>() {
            @Override
            public void done(CarFromKahn car, ParseException e) {
                if (e != null) {
                    Toast.makeText(DetailActivity.this, e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                } else if (car.isSold()) {
                    Toast.makeText(DetailActivity.this, getString(R.string.sold),
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                mCar = car;

                // TODO Pin downloaded details to retrieve from local database later
                new DownloadSpecsTask().execute();

                String make = car.getMake();
                String model = car.getModel();

                String title = make + " " + model;
                if (title.length() > 20) {
                    title = model;
                }
                getSupportActionBar().setTitle(make);
                
                ((TextView) findViewById(R.id.make)).setText(mCar.getMake());
                ((TextView) findViewById(R.id.model)).setText(mCar.getModel());
                ((TextView) findViewById(R.id.year)).setText(mCar.getYear() + "");
                ((TextView) findViewById(R.id.mileage)).setText(mCar.getMileage());

                setCoverImage();

                String url = mCar.getCoverImageUrl();
                new SetShareIntentTask().execute(title, url);
            }
        });
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

            String imagePath = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "title", null);

            mImageUri = Uri.parse(imagePath);

            mShareIntent = new Intent();
            mShareIntent.setAction(Intent.ACTION_SEND);
            mShareIntent.putExtra(Intent.EXTRA_SUBJECT, title + " - Mobanic");
            mShareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this car I found! Care for your own test drive? - mobanic.com");
            mShareIntent.putExtra("sms_body", "Check out this car I found! Care for your own test drive? - mobanic.com");
            mShareIntent.putExtra(Intent.EXTRA_STREAM, mImageUri);
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
        String url = mCar.getCoverImageUrl();

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

    private class DownloadSpecsTask extends AsyncTask<Void, Void, Boolean> {

        private static final String BASE_URL = "http://www.kahndesign.com/automobiles/automobiles_available_detail.php?i=";

        @Override
        protected Boolean doInBackground(Void... voids) {
            String url = BASE_URL + mCarId;
            mGalleryImageUrls = new ArrayList<>();
            mFeatureList = new ArrayList<>();
            try {
                Document doc = Jsoup.connect(url).timeout(10 * 1000).get();
                Elements images = doc.select("[src*=.jpg]");
                for (Element image : images) {
                    if (image.attr("src").contains("imgMedium")) {
                        String imageUrl = image.attr("src")
                                .replace("../", "https://kahndesign.com/")
                                .replace("imgMedium", "imgLarge");
                        mGalleryImageUrls.add(imageUrl);
                    }
                }

                Elements features = doc.select("#specList");
                for (Element feature : features) {
                    mFeatureList.add(feature.text());
                }
                mFeatureList.remove(mFeatureList.size() - 1);

                Elements specs = doc.select(".fivecol");
                String mileage = specs.get(4).text().substring(2);
                int prevOwners = Integer.parseInt(specs.get(7).text().substring(2));
                int engine;
                try {
                    String engineStr = specs.get(2).text();
                    engine = Integer.parseInt(engineStr.substring(0, engineStr
                            .toLowerCase().indexOf("cc")).substring(2).trim());
                } catch (StringIndexOutOfBoundsException e) {
                    String engineStr = specs.get(2).text().split(": ")[0].substring(2);
                    if (!engineStr.contains(".")) {
                        engine = Integer.parseInt(engineStr.trim());
                    } else {
                        engine = Integer.parseInt(engineStr.split("\\.")[0]) * 1000;
                    }
                }

                mCar.setMileage(mileage);
                mCar.setPrevOwners(prevOwners);
                mCar.setEngine(engine);
                mCar.pinInBackground();

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            findViewById(R.id.spinner).setVisibility(View.GONE);

            if (success) {
                setGalleryImages();
                fillOutSpecs();
                fillOutFeatures();
            } else {
                findViewById(R.id.error).setVisibility(View.VISIBLE);
                findViewById(R.id.make).setVisibility(View.GONE);
            }
        }

    }

    private void setGalleryImages() {
        final ViewFlipper flipper = (ViewFlipper) findViewById(R.id.flipper);
        flipper.startFlipping();
        flipper.setInAnimation(AnimationUtils.loadAnimation(DetailActivity.this,
                android.R.anim.fade_in));
        flipper.setOutAnimation(AnimationUtils.loadAnimation(DetailActivity.this,
                android.R.anim.fade_out));
        flipper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipper.stopFlipping();
                flipper.showNext();
                flipper.startFlipping();
            }
        });

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
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            if (url == mGalleryImageUrls.get(0)) {
                                flipper.startFlipping();
                            }
                        }

                        @Override
                        public void onError() {
                        }
                    });
            flipper.addView(imageView);
        }
        if (mGalleryImageUrls.size() == 1) {
            flipper.setOnClickListener(null);
            flipper.stopFlipping();
        }
    }

    private void fillOutSpecs() {
        ((TextView) findViewById(R.id.previousOwners)).setText(mCar.getPreviousOwners() + "");
        ((TextView) findViewById(R.id.engine)).setText(mCar.getEngine());
        ((TextView) findViewById(R.id.transmission)).setText(mCar.getTransType());
        ((TextView) findViewById(R.id.fuelType)).setText(mCar.getFuelType());
        ((TextView) findViewById(R.id.color)).setText(mCar.getColor());
        TextView locationTextView = (TextView) findViewById(R.id.location);
        locationTextView.setText(mCar.getLocation());
        locationTextView.setPaintFlags(locationTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        locationTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOnMap(mCar.getLocation());
            }
        });
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