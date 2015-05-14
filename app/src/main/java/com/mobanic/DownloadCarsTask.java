package com.mobanic;

import android.os.AsyncTask;

import com.mobanic.activities.MainActivity;
import com.parse.DeleteCallback;
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

public class DownloadCarsTask extends AsyncTask<Void, Void, List<CarFromKahn>> {

    private static final String TAG = DownloadCarsTask.class.getSimpleName();
    private static final String BASE_URL = "http://www.kahndesign.com/automobiles/automobiles_available.php";

    @Override
    protected List<CarFromKahn> doInBackground(Void... voids) {
        List<CarFromKahn> carList = null;
        try {
            carList = new ArrayList<>();

            Document doc = Jsoup.connect(BASE_URL).get();
            Elements cards = doc.select("#ajax-content-container .row .stockBack .centre")
                    .not(".midGreyText");
            for (Element card : cards) {
                String modelAndMake = card.getElementsByTag("h4").first().text();

                Elements specs = card.getElementsByClass("thirteencol");
                String year = specs.get(0).text();
                String color = specs.get(1).text().trim();
                String mileage = specs.get(2).text();
                String fuelAndTrans = specs.get(3).text();
                String location = specs.get(5).text();
                String price = specs.get(6).text();

                String linkContents = card.select("a").html();
                boolean isLeftHanded = false;
                if (linkContents.contains("LHDBack")) {
                    isLeftHanded = true;
                }
                String imageId;
                try {
                    imageId = linkContents.substring(
                            linkContents.indexOf(".jpg") - 5,
                            linkContents.indexOf(".jpg"));
                } catch (StringIndexOutOfBoundsException e) {
                    imageId = "";
                }

                CarFromKahn car = new CarFromKahn(
                        modelAndMake, year, imageId, price, color, mileage, fuelAndTrans, location, isLeftHanded);
                carList.add(car);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return carList;
    }

    @Override
    protected void onPostExecute(final List<CarFromKahn> carList) {
        super.onPostExecute(carList);
        ParseObject.unpinAllInBackground(carList, new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
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
        });
    }
}