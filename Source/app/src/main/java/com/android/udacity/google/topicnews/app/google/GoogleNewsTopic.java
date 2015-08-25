package com.android.udacity.google.topicnews.app.google;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class GoogleNewsTopic implements Parcelable {

    private static final String TOPIC_DATE_PARSE_FORMAT = "EEE, d MMM yyyy HH:mm:ss Z";

    private static final String TOPIC_DATE_OUTPUT = "yyyy/M/d hh:mm";

    public static final Parcelable.Creator<GoogleNewsTopic> CREATOR = new Parcelable.Creator<GoogleNewsTopic>() {

        @Override
        public GoogleNewsTopic createFromParcel(Parcel source) {
            return new GoogleNewsTopic(source);
        }

        @Override
        public GoogleNewsTopic[] newArray(int size) {
            return new GoogleNewsTopic[size];
        }

    };

    public String title;
    public String content;
    public URL url;
    public String publisher;
    public long publishedDate;
    public URL originImage;
    public URL thumbnail;

    public static GoogleNewsTopic newTopicNews(String title, String content, String url, String publisher, long publishedDate, String imageUrl, String thumbnail) {
        return new GoogleNewsTopic(title, content, url, publisher, publishedDate, imageUrl, thumbnail);
    }

    public static GoogleNewsTopic newCategory(String title) {
        return new GoogleNewsTopic(title, null, null, null, -1, null, null);
    }

    public GoogleNewsTopic(String title, String content, String url, String publisher, String publishedDate, String imageUrl, String thumbnail) {
        this.title = title;
        this.content = content;
        this.url = convertURL(url);
        this.publisher = publisher;
        this.publishedDate = parsePublishedDate(publishedDate);
        this.originImage = convertURL(imageUrl);
        this.thumbnail = convertURL(thumbnail);
    }

    public GoogleNewsTopic(String title, String content, String url, String publisher, long publishDate, String imageUrl, String thumbnail) {
        this.title = title;
        this.content = content;
        this.url = convertURL(url);
        this.publisher = publisher;
        this.publishedDate = publishDate;
        this.originImage = convertURL(imageUrl);
        this.thumbnail = convertURL(thumbnail);
    }

    private GoogleNewsTopic(Parcel parcel) {
        title = parcel.readString();
        content = parcel.readString();
        url = convertURL(parcel.readString());
        publisher = parcel.readString();
        publishedDate = parcel.readLong();

        boolean hasOriginImage = parcel.readInt() == 1;
        boolean hasThumbnail = parcel.readInt() == 1;
        if (hasOriginImage) {
            originImage = convertURL(parcel.readString());
        }
        if (hasThumbnail) {
            thumbnail = convertURL(parcel.readString());
        }
    }

    public boolean isNewsTopic() {
        return url != null;
    }

    public String getPublishedDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TOPIC_DATE_OUTPUT, Locale.getDefault());
        return simpleDateFormat.format(publishedDate);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(content);
        dest.writeString(url.toString());
        dest.writeString(publisher);
        dest.writeLong(publishedDate);

        boolean hasOriginImage = originImage != null;
        dest.writeInt(hasOriginImage ? 1 : 0);

        boolean hasThumbnail = thumbnail != null;
        dest.writeInt(hasThumbnail ? 1 : 0);

        if (hasOriginImage) {
            dest.writeString(originImage.toString());
        }

        if (hasThumbnail) {
            dest.writeString(thumbnail.toString());
        }

        dest.setDataPosition(0);
    }

    private URL convertURL(String s) {
        if (s != null) {
            try {
                return new URL(URLDecoder.decode(s));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private long parsePublishedDate(String s) {
        if (s != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TOPIC_DATE_PARSE_FORMAT, Locale.getDefault());
            try {
                return simpleDateFormat.parse(s).getTime();
            } catch (ParseException e) {
                return System.currentTimeMillis();
            }
        }
        return -1;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public boolean equals(Object o) {
        if (o instanceof GoogleNewsTopic) {
            GoogleNewsTopic other = (GoogleNewsTopic) o;
            if (!Objects.equals(title, other.title)) {
                return false;
            } else if (!Objects.equals(content, other.content)) {
                return false;
            } else if (!Objects.equals(url, other.url)) {
                return false;
            } else if (!Objects.equals(publisher, other.publisher)) {
                return false;
            } else if (!Objects.equals(publishedDate, other.publishedDate)) {
                return false;
            } else if (!Objects.equals(originImage, other.originImage)) {
                return false;
            } else if (!Objects.equals(thumbnail, other.thumbnail)) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
}