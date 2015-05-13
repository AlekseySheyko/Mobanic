package com.mobanic;

import android.os.AsyncTask;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DownloadCarsTask extends AsyncTask<Void, Void, List<ParsedCar>> {

    private static final String TAG = DownloadCarsTask.class.getSimpleName();
    private static final String BASE_URL = "http://www.kahndesign.com/automobiles/automobiles_available.php";

    @Override
    protected List<ParsedCar> doInBackground(Void... voids) {
        List<ParsedCar> carList = null;
        try {
            carList = new ArrayList<>();

            Document doc = Jsoup.connect(BASE_URL).get();
            Elements cards = doc.select("#ajax-content-container .row .stockBack .centre")
                    .not(".midGreyText");
            for (Element card : cards) {
                String title = card.getElementsByTag("h4").first().text();

                Elements specs = card.getElementsByClass("thirteencol");
                String year = specs.get(0).text();
                String color = specs.get(1).text().trim();
                String mileage = specs.get(2).text();
                String fuelAndTrans = specs.get(3).text();
                String location = specs.get(5).text();
                String price = specs.get(6).text();

                Element coverImage = card.select("[src*=.jpg]").first();
                String imageUrl = null;
                if (coverImage != null) {
                    imageUrl = coverImage.attr("src");
                }


                ParsedCar car = new ParsedCar();
                car.setTitle(title);
                car.setYear(year);
                car.setCoverImage(imageUrl);
                car.setPrice(price);
                car.setColor(color);
                car.setMileage(mileage);
                car.setFuelAndTrans(fuelAndTrans);
                car.setLocation(location);
                carList.add(car);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return carList;
    }

    @Override
    protected void onPostExecute(List<ParsedCar> carList) {
        super.onPostExecute(carList);
        ParseObject.pinAllInBackground(carList, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    MainActivity a = (MainActivity) MainActivity.getContext();
                    a.mCarsAdapter.loadObjects();
                }
            }
        });
    }
}