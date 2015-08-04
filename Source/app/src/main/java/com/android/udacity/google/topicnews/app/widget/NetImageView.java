package com.android.udacity.google.topicnews.app.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.android.udacity.google.topicnews.app.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * https://android.googlesource.com/platform/frameworks/base/+/lollipop-mr1-release/core/java/android/widget/ImageView.java
 * http://developer.android.com/reference/android/content/res/TypedArray.html
 */
public class NetImageView extends ImageView {

    private static final Map<URL, Bitmap> sLoadedBitmaps = new HashMap<>();

    private static final Object sMapLock = new Object();

    private static final float VISIBLE = 1.0f;

    private static final float INVISIBLE = 0.0f;

    private static final long ANIMATION_DURATION = 500L;

    private URL mUrl = null;

    private Bitmap mBitmap = null;

    private LoaderCallback mLoaderCallback = null;

    private Set<URL> mUrlSet = null;

    public NetImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mLoaderCallback = new LoaderCallbackImpl();
        mUrlSet = new HashSet<>();

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NetImageView, defStyleAttr, 0);
        try {
            String urlString = a.getString(R.styleable.NetImageView_url);
            if (urlString != null) {
                try {
                    setImageURL(new URL(urlString));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            if (a != null) {
                a.recycle();
            }
        }
    }

    public NetImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NetImageView(Context context) {
        super(context);
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        if (mBitmap != null && !mBitmap.equals(bitmap)) {
            Animation animation = new AlphaAnimation(VISIBLE, INVISIBLE);
            animation.setDuration(ANIMATION_DURATION);
            setAnimation(animation);
        }

        super.setImageBitmap(bitmap);

        Animation animation = new AlphaAnimation(INVISIBLE, VISIBLE);
        animation.setDuration(ANIMATION_DURATION);
        setAnimation(animation);
    }

    public void setImageLoadedBitmap(Bitmap bitmap) {
        NetImageView.putBitmap(mUrl, bitmap);
        setImageBitmap(bitmap);
    }

    public void setImageURL(URL url) {
        if (url == null || url.equals(mUrl)) {
            return;
        }

        mUrl = url;

        mUrlSet.add(mUrl);

        Bitmap bitmap = sLoadedBitmaps.get(mUrl);
        if (bitmap == null) {
            LoadImageTask task = new LoadImageTask(mUrl);
            task.setLoaderCallback(mLoaderCallback);
            task.execute();
        } else {
            setImageBitmap(bitmap);
        }
    }

    public URL getImageURL() {
        return mUrl;
    }

    private static boolean putBitmap(URL url, Bitmap bitmap) {
        synchronized (sMapLock) {
            if (sLoadedBitmaps.containsKey(url)) {
                return false;
            }
            sLoadedBitmaps.put(url, bitmap);
            return true;
        }
    }

    private static boolean removeBitmap(URL url) {
        synchronized (sMapLock) {
            if (!sLoadedBitmaps.containsKey(url)) {
                return false;
            }
            sLoadedBitmaps.remove(url);
            return true;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (mUrlSet != null && !mUrlSet.isEmpty()) {
            for (URL url : mUrlSet) {
                NetImageView.removeBitmap(url);
            }
            mUrlSet = null;
        }
        super.finalize();
    }

    private class LoaderCallbackImpl implements LoaderCallback {

        @Override
        public void onLoadFinished(Bitmap bitmap) {
            setImageLoadedBitmap(bitmap);
        }

        @Override
        public void onLoadCanceled(Throwable throwable) {
            throwable.printStackTrace();
        }

    }

    private class LoadImageTask extends AsyncTask<Bitmap, Void, Bitmap> {

        private LoaderCallback mCallback = null;

        private URL mUrl = null;

        private Throwable mThrowable = null;

        public LoadImageTask(URL url) {
            mUrl = url;
        }

        @Override
        protected Bitmap doInBackground(Bitmap... params) {
            Bitmap bitmap = null;
            try {
                InputStream inputStream = mUrl.openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);

            } catch (IOException e) {
                mThrowable = e;
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (mCallback != null) {
                if (bitmap != null) {
                    mCallback.onLoadFinished(bitmap);
                } else {
                    mCallback.onLoadCanceled(mThrowable);
                }
            }
        }

        public void setLoaderCallback(LoaderCallback callback) {
            mCallback = callback;
        }

    }

    private interface LoaderCallback {
        void onLoadFinished(Bitmap bitmap);
        void onLoadCanceled(Throwable throwable);
    }
}
