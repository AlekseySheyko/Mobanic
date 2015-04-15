package com.mappfia.mobanic;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // TODO: Retrive car make and model from intent
        getSupportActionBar().setTitle("Land Rover Range Rover Sport");

        RatioImageView imageView = (RatioImageView) findViewById(R.id.image);
        Picasso.with(this)
                .load("http://www.themotorreport.com.au/content/image/2/0/2014_range_rover_sport_australia_01_1-1020-mc:819x819.jpg")
                .fit()
                .centerCrop()
                .into(imageView);

        RatioImageView imageView1 = (RatioImageView) findViewById(R.id.image1);
        RatioImageView imageView2 = (RatioImageView) findViewById(R.id.image2);
        RatioImageView imageView3 = (RatioImageView) findViewById(R.id.image3);
        RatioImageView imageView4 = (RatioImageView) findViewById(R.id.image4);
        RatioImageView imageView5 = (RatioImageView) findViewById(R.id.image5);
        RatioImageView imageView6 = (RatioImageView) findViewById(R.id.image6);
        RatioImageView imageView7 = (RatioImageView) findViewById(R.id.image7);
        RatioImageView imageView8 = (RatioImageView) findViewById(R.id.image8);

        Picasso.with(this)
                .load("http://www.themotorreport.com.au/content/image/2/0/2014_range_rover_sport_australia_01_1-1020-mc:819x819.jpg")
                .fit()
                .centerCrop()
                .into(imageView1);
        Picasso.with(this)
                .load("http://o.aolcdn.com/hss/storage/midas/ff10ff0b8023231885fcbd74f6d32ed8/200047427/lead22-2014-lr-range-rover-sport-review.jpg")
                .fit()
                .centerCrop()
                .into(imageView2);
        Picasso.with(this)
                .load("http://lexani.com/media/images/rendered/2014_Land%20Rover_Range%20Rover%20Sport_740_v1.jpg")
                .fit()
                .centerCrop()
                .into(imageView3);
        Picasso.with(this)
                .load("http://www.themotorreport.com.au/content/image/2/0/2014_range_rover_sport_australia_01_1-1020-mc:819x819.jpg")
                .fit()
                .centerCrop()
                .into(imageView4);
        Picasso.with(this)
                .load("http://o.aolcdn.com/hss/storage/midas/ff10ff0b8023231885fcbd74f6d32ed8/200047427/lead22-2014-lr-range-rover-sport-review.jpg")
                .fit()
                .centerCrop()
                .into(imageView5);
        Picasso.with(this)
                .load("http://lexani.com/media/images/rendered/2014_Land%20Rover_Range%20Rover%20Sport_740_v1.jpg")
                .fit()
                .centerCrop()
                .into(imageView6);
        Picasso.with(this)
                .load("http://www.themotorreport.com.au/content/image/2/0/2014_range_rover_sport_australia_01_1-1020-mc:819x819.jpg")
                .fit()
                .centerCrop()
                .into(imageView7);
        Picasso.with(this)
                .load("http://o.aolcdn.com/hss/storage/midas/ff10ff0b8023231885fcbd74f6d32ed8/200047427/lead22-2014-lr-range-rover-sport-review.jpg")
                .fit()
                .centerCrop()
                .into(imageView8);


        ArrayList<Spec> specs = new ArrayList<>();
        specs.add(new Spec("Make", "Land Rover"));
        specs.add(new Spec("Model", "Range Rover Sport"));
        specs.add(new Spec("Year", "2014"));
        specs.add(new Spec("Mileage", "50"));
        specs.add(new Spec("Previous owners", "1"));
        specs.add(new Spec("Engine", "3000cc"));
        specs.add(new Spec("Transmission", "Automatic"));
        specs.add(new Spec("Fuel Type", "Diesel"));
        specs.add(new Spec("Color", "Paris Grey"));
        specs.add(new Spec("Location", "UK"));
    }

    private class Spec {

        private String mKey;
        private String mValue;

        public Spec(String key, String value) {
            mKey = key;
            mValue = value;
        }

        public String getKey() {
            return mKey;
        }

        public String getValue() {
            return mValue;
        }
    }
}
