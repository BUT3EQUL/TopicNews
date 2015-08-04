package com.android.udacity.google.topicnews.app.sync;

import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.Intent;
import android.os.IBinder;

public class GoogleNewsSyncService extends Service {

    private static AbstractThreadedSyncAdapter sSyncAdapter = null;

    private static final Object sSyncLock = new Object();

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (sSyncLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new GoogleNewsSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }

}
