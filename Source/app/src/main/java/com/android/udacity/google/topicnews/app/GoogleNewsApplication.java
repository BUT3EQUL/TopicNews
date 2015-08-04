package com.android.udacity.google.topicnews.app;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class GoogleNewsApplication extends Application {

    private static Context sContext = null;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
    }

    public static Context getsContext() {
        return sContext;
    }

    public static void debug(String tag, String msg, Throwable e) {
        boolean enableDebug = getsContext().getResources().getBoolean(R.bool.debug);
        if (enableDebug) {
            Log.d(tag, msg, e);
        }
    }

    public static void debug(String tag, String msg) {
        boolean enableDebug = getsContext().getResources().getBoolean(R.bool.debug);
        if (enableDebug) {
            Log.d(tag, msg);
        }
    }

    public static void trace(String tag, String msg, Throwable e) {
        boolean enableTrace = getsContext().getResources().getBoolean(R.bool.trace);
        if (enableTrace) {
            Log.v(tag, msg, e);
        }
    }

    public static void trace(String tag, String msg) {
        boolean enableTrace = getsContext().getResources().getBoolean(R.bool.trace);
        if (enableTrace) {
            Log.v(tag, msg);
        }
    }
}
