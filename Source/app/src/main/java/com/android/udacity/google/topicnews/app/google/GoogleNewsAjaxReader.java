package com.android.udacity.google.topicnews.app.google;

import com.android.udacity.google.topicnews.app.GoogleNewsApplication;
import com.android.udacity.google.topicnews.app.json.JSONReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GoogleNewsAjaxReader extends JSONReader {

    private static final String AJAX_GOOGLE_NEWS = "http://ajax.googleapis.com/ajax/services/search/news?v=1.0";

    public interface Genres {
        String ENTERTAINMENT = "e";
        String NATION = "n";
        String POLITICS ="p";
        String SPORTS = "s";
        String TECHNOLOGY = "t";
        String WORLD = "w";
        String SOCIETY = "y";
        String PICKUP = "ir";
    }

    public GoogleNewsAjaxReader(String genre) {
        super(Utility.buildUri(AJAX_GOOGLE_NEWS, genre).toString());
    }

    @Override
    public List<GoogleNewsTopic> parse() {
        List<GoogleNewsTopic> result = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(getJsonString());
            JSONObject responseData = root.getJSONObject("responseData");
            JSONArray results = responseData.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                JSONObject element = results.getJSONObject(i);
                String title = Utility.removeHtmlTags(Utility.convertUnsanitize(element.getString("title")));
                String content = Utility.removeHtmlTags(Utility.convertUnsanitize(element.getString("content")));
                String url = element.getString("url");
                String publisher = element.getString("publisher");
                String publishedDate = element.getString("publishedDate");

                String thumbnail = null;
                String imageUrl = null;
                if (element.has("image")) {
                    JSONObject image = element.getJSONObject("image");
                    thumbnail = image.getString("tbUrl");
                    imageUrl = image.getString("url");
                    GoogleNewsApplication.debug("GoogleNewsAjaxReader", "" + i + " : " + imageUrl);
                } else {
                    GoogleNewsApplication.debug("GoogleNewsAjaxReader", "" + i + " : image none");
                }

                result.add(new GoogleNewsTopic(title, content, url, publisher, publishedDate, imageUrl, thumbnail));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
