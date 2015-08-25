package com.android.udacity.google.topicnews.app.google;

import android.net.Uri;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.regex.Pattern;

final class Utility {

    private Utility() { }

    public static Uri buildUri(String base, String genre) {
        Uri baseUri = Uri.parse(base);
        return baseUri.buildUpon()
                .appendQueryParameter("topic", genre)
                .build();
    }

    public static String convertUnsanitize(String s) {
        if (s == null) {
            return null;
        }

        return StringEscapeUtils.unescapeHtml4(s);
    }

    public static String removeHtmlTags(String s) {
        Pattern pattern = Pattern.compile("<.+?>");
        return pattern.matcher(s).replaceAll("");
    }

}
