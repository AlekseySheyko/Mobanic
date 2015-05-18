package com.mobanic.tasks;

import android.os.AsyncTask;

import com.mobanic.activities.MasterActivity;
import com.mobanic.model.CarParsed;
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

public class FetchCarsTask extends AsyncTask<Void, Void, List<CarParsed>> {

    private static final String TAG = FetchCarsTask.class.getSimpleName();
    private static final String BASE_URL = "http://www.kahndesign.com/automobiles/automobiles_available.php";

    @Override
    protected List<CarParsed> doInBackground(Void... voids) {
        List<CarParsed> carList = null;
        try {
            carList = new ArrayList<>();

            Document doc = Jsoup.connect(BASE_URL).timeout(15 * 1000).get();
            Elements cards = doc.select("#ajax-content-container .centre")
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
                String href = card.select("a").first().attr("href");
                int id = Integer.parseInt(
                        href.substring(href.indexOf("?i=") + 3, href.indexOf("&css")));

                CarParsed car = new CarParsed(
                        id, modelAndMake, year, imageId, price, color, mileage, fuelAndTrans, location, isLeftHanded);
                carList.add(car);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return carList;
    }

    @Override
    protected void onPostExecute(final List<CarParsed> carList) {
        super.onPostExecute(carList);
        if (carList != null) {
            ParseObject.pinAllInBackground(carList, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        MasterActivity a = (MasterActivity) MasterActivity.getContext();
                        a.queryCounter = 2;
                        a.refreshCarList();
                    }
                }
            });
        }
    }
}