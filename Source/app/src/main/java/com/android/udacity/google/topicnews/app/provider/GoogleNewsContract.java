package com.android.udacity.google.topicnews.app.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

public class GoogleNewsContract implements BaseColumns {

    public static final String AUTHORITY = "com.android.udacity.google.topicnews.app";

    public static final String PATH_DEFAULT = "default";

    public static final String PATH_DISTINCT = "distinct";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY).buildUpon()
            .appendPath(PATH_DEFAULT).build();

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/" + AUTHORITY +
            "/" + PATH_DEFAULT;

    public static final String TABLE_NAME = "news";

    public static final String COLUMN_DATE = "date";

    public static final String COLUMN_GENRE = "genre";

    public static final String COLUMN_TITLE = "title";

    public static final String COLUMN_CONTENT = "content";

    public static final String COLUMN_IMAGE_URL = "image";

    public static final String COLUMN_THUMBNAIL_URL = "thumbnail";

    public static final String COLUMN_PUBLISHER = "publisher";

    public static final String COLUMN_URL = "url";

    public static long normalizeDate(long startDate) {
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    public static Uri buildGoogleNewsUri(long id) {
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }

}
