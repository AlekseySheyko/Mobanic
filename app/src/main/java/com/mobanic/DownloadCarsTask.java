package com.mobanic;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class DownloadCarsTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = DownloadCarsTask.class.getSimpleName();
    private static final String BASE_URL = "http://www.kahndesign.com/automobiles/automobiles_available.php?m1=&m2=&n1=All&n2=&loc=&gh=&layout=2&view=ALL&classic=N&steering";

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            Document doc = Jsoup.connect(BASE_URL).get();

            Element container = doc.getElementById("ajax-content-container");
            Elements rows = container.getElementsByClass("row");
            for (Element row : rows) {
                Elements cards = row.getElementsByClass("foureightcol");
                for (Element card : cards) {
                    Elements entries = card.children();
                    String detailsUrl = entries.attr("href");
                    String price = entries.select(".redText").text();
                    Log.d(TAG, "Price: " + price);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void parseRows() {

    }
}
