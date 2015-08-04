package com.android.udacity.google.topicnews.app.json;

import android.os.Parcelable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public abstract class JSONReader {

    private String mJsonString = null;

    public JSONReader(String uri) {
        try {
            URL url = new URL(uri);
            mJsonString = getJsonString(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    protected String getJsonString() {
        return mJsonString;
    }

    public abstract <T extends Parcelable> List<T> parse();

    private String getJsonString(URL url) {
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();

            InputStream inputStream = httpURLConnection.getInputStream();
            if (inputStream == null) {
                return null;
            }
            return readJsonStream(inputStream);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }

    private String readJsonStream(InputStream inputStream) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
                stringBuffer.append('\n');
            }
            if (stringBuffer.length() == 0) {
                return null;
            }
            return stringBuffer.toString();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}