package com.mappfia.mobanic;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ViewFlipper;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // TODO: Retrive car make and model from intent
        getSupportActionBar().setTitle("Range Rover Sport");

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
