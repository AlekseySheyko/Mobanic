package com.mobanic;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DownloadCarsTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = DownloadCarsTask.class.getSimpleName();
    private static final String BASE_URL = "http://www.kahndesign.com/automobiles/automobiles_available.php?m1=&m2=&n1=All&n2=&classic=N&steering";

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            List<String> titleList = new ArrayList<>();
            List<Integer> priceList = new ArrayList<>();

            Document doc = Jsoup.connect(BASE_URL).get();

            Element container = doc.getElementById("ajax-content-container");
            Elements rows = container.getElementsByClass("row");
            for (Element row : rows) {
                Elements headers = row.getElementsByTag("h4");
                for (Element header : headers) {
                    titleList.add(header.text());
                }
                Elements dataEntries = row.getElementsByClass("thirteencol");
                for (Element entry : dataEntries) {
                    final String priceSuffix = ".00";
                    String entryText = entry.text();
                    Log.d(TAG, entryText);
                    if (entryText.contains(priceSuffix)) {
                        int price = Integer.parseInt(
                                entryText.substring(2, entryText.length() - 3).replace(",", ""));
                        priceList.add(price);
                    } else if (entryText.contains("Under Offer") || entryText.contains("on Application")) {
                        priceList.add(0);
                    }
                }
            }
            for (int i = 0; i < titleList.size(); i++) {
                ParsedCar car = new ParsedCar();
                car.setTitle(titleList.get(i));
                car.setPrice(priceList.get(i));
                car.pinInBackground();
            }
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
