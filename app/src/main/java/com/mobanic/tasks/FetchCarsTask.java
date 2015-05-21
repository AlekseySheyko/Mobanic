package com.mobanic.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.mobanic.model.CarParsed;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class FetchCarsTask extends AsyncTask<Void, Void, List<CarParsed>> {

    private static final String TAG = FetchCarsTask.class.getSimpleName();
    private static final String BASE_URL = "http://www.kahndesign.com/automobiles/automobiles_available.php";

    @Override
    protected List<CarParsed> doInBackground(Void... voids) {
        List<CarParsed> carList;
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

                if (modelAndMake != null) {
                    CarParsed car = new CarParsed(
                            id, modelAndMake, year, imageId, price, color, mileage, fuelAndTrans, location, isLeftHanded);
                    carList.add(car);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return carList;
    }

    @Override
    protected void onPostExecute(final List<CarParsed> carList) {
        super.onPostExecute(carList);
        if (carList != null) {
            for (final ParseObject car : carList) {
                car.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            new DownloadSpecsTask().execute(car);
                        }
                    }
                });
            }
        }
    }

    private class DownloadSpecsTask extends AsyncTask<ParseObject, Void, Boolean> {
        private static final String BASE_URL = "http://www.kahndesign.com/automobiles/automobiles_available_detail.php?i=";

        @Override
        protected Boolean doInBackground(ParseObject... cars) {
            final ParseObject car = cars[0];

            String url = BASE_URL + car.getInt("id");
            try {
                Document doc = Jsoup.connect(url).timeout(10 * 1000).get();
                Elements images = doc.select("[src*=.jpg]");
                List<String> galleryImageUrls = new ArrayList<>();
                for (Element image : images) {
                    if (image.attr("src").contains("imgMedium")) {
                        String imageUrl = image.attr("src")
                                .replace("../", "https://www.kahndesign.com/")
                                .replace("imgMedium", "imgLarge");
                        galleryImageUrls.add(imageUrl);
                    }
                }
                String imageUrl = car.getString("coverImage");
                if (imageUrl == null) {
                    imageUrl = car.getParseFile("coverImage").getUrl();
                }
                if (galleryImageUrls.size() > 0 && galleryImageUrls.get(0).equals(imageUrl)) {
                    galleryImageUrls.remove(0);
                }

                Elements features = doc.select("#specList");
                List<String> featureList = new ArrayList<>();
                for (Element feature : features) {
                    featureList.add(feature.text());
                }
                if (featureList.size() > 0) {
                    featureList.remove(featureList.size() - 1);
                }

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

                car.put("mileage",
                        Integer.parseInt(mileage.replaceAll(",", "").trim()));
                car.put("previousOwners", prevOwners);
                car.put("engine", engine);
                car.put("galleryImages", galleryImageUrls);
                car.put("features", featureList);
                car.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Log.d(TAG, "Car " + car.getObjectId() + " was saved");
                        } else {
                            Log.d(TAG, "Failed to save " + car.getObjectId() + ": " + e.getMessage());
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }
}