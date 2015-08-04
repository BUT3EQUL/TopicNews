package com.android.udacity.google.topicnews.app.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class GoogleNewsAuthenticatorService extends Service {

    private GoogleNewsAuthenticator mAuthenticator = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mAuthenticator = new GoogleNewsAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }

}
