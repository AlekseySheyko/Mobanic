package com.mobanic;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DownloadCarsTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = DownloadCarsTask.class.getSimpleName();
    private static final String BASE_URL = "http://www.kahndesign.com/automobiles/automobiles_available.php";

    @Override
    protected Void doInBackground(Void... voids) {
        List<String> titleList = new ArrayList<>();
        List<Integer> priceList = new ArrayList<>();
        List<String> imageList = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(BASE_URL).get();
            Elements cards = doc.select("#ajax-content-container .row .stockBack .centre")
                    .not(".midGreyText");
            for (Element card : cards) {
            }


//            for (int i = 0; i < titleList.size(); i++) {
//                ParsedCar car = new ParsedCar();
//                car.setTitle(titleList.get(i));
//                car.setPrice(priceList.get(i));
//                car.setCoverImage(imageList.get(i));
//                car.pinInBackground();
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        MainActivity a = (MainActivity) MainActivity.getContext();
        a.mCarsAdapter.loadObjects();
    }
}