package com.android.udacity.google.topicnews.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;

import com.android.udacity.google.topicnews.app.GoogleNewsApplication;
import com.android.udacity.google.topicnews.app.R;
import com.android.udacity.google.topicnews.app.google.GoogleNewsAjaxReader;
import com.android.udacity.google.topicnews.app.google.GoogleNewsTopic;
import com.android.udacity.google.topicnews.app.provider.GoogleNewsContract;

import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class GoogleNewsSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final int SYNC_INTERNAL_HOURS = 3;

    private static final int SYNC_INTERNAL = SYNC_INTERNAL_HOURS * 60 * 60;

    private static final int SYNC_FLEXTIME = SYNC_INTERNAL / 3;

    private static final long DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1);

    private static final long QUARTER_OF_DAY = DAY_IN_MILLIS / 4;

    private static final int NOTIFICATION_ID = 5284;

    private interface Columns {
        int GENRE = 0;
        int TITLE = 1;
        int MAX_INDEXES = 2;
    }

    private static final String[] QUERY_PROJECTION = new String[Columns.MAX_INDEXES];
    static {
        QUERY_PROJECTION[Columns.GENRE] = GoogleNewsContract.COLUMN_GENRE;
        QUERY_PROJECTION[Columns.TITLE] = GoogleNewsContract.COLUMN_TITLE;
    }

    public GoogleNewsSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        loadGoogleNewsAllTopic(getContext());
        notifyFreshNews();
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    private static Account getSyncAccount(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account newAccount = new Account(context.getString(R.string.app_name),
                context.getString(R.string.sync_account_type));
        if (accountManager.getPassword(newAccount) == null) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            configurePeriodicSync(context, SYNC_INTERNAL, SYNC_FLEXTIME);
            ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
            syncImmediately(context);
        }
        return newAccount;
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int syncFlexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        Bundle extras = new Bundle();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest syncRequest = new SyncRequest.Builder()
                    .syncPeriodic(syncInterval, syncFlexTime)
                    .setSyncAdapter(account, authority)
                    .setExtras(extras)
                    .build();
            ContentResolver.requestSync(syncRequest);
        } else {
            ContentResolver.addPeriodicSync(account, authority, extras, syncInterval);
        }
    }

    private void loadGoogleNewsAllTopic(Context context) {
        getContext().getContentResolver().delete(GoogleNewsContract.CONTENT_URI, null, null);
        insertGoogleNews(context.getString(R.string.pref_key_entertainment), GoogleNewsAjaxReader.Genres.ENTERTAINMENT);
        insertGoogleNews(context.getString(R.string.pref_key_nation), GoogleNewsAjaxReader.Genres.NATION);
        insertGoogleNews(context.getString(R.string.pref_key_pickup), GoogleNewsAjaxReader.Genres.PICKUP);
        insertGoogleNews(context.getString(R.string.pref_key_politics), GoogleNewsAjaxReader.Genres.POLITICS);
        insertGoogleNews(context.getString(R.string.pref_key_society), GoogleNewsAjaxReader.Genres.SOCIETY);
        insertGoogleNews(context.getString(R.string.pref_key_sports), GoogleNewsAjaxReader.Genres.SPORTS);
        insertGoogleNews(context.getString(R.string.pref_key_technology), GoogleNewsAjaxReader.Genres.TECHNOLOGY);
        insertGoogleNews(context.getString(R.string.pref_key_world), GoogleNewsAjaxReader.Genres.WORLD);
    }

    private void insertGoogleNews(String genre, String queryKey) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (!preferences.getBoolean(genre, true)) {
            return;
        }

        GoogleNewsAjaxReader ajaxReader = new GoogleNewsAjaxReader(queryKey);
        List<GoogleNewsTopic> topicList = ajaxReader.parse();

        Vector<ContentValues> contentValues = new Vector<>();
        for (int i = 0; i < topicList.size(); i++) {
            GoogleNewsTopic topic = topicList.get(i);

            ContentValues values = new ContentValues();
            values.put(GoogleNewsContract.COLUMN_GENRE, genre);
            values.put(GoogleNewsContract.COLUMN_TITLE, topic.title);
            values.put(GoogleNewsContract.COLUMN_CONTENT, topic.content);
            values.put(GoogleNewsContract.COLUMN_URL, topic.url.toString());
            values.put(GoogleNewsContract.COLUMN_DATE, topic.publishedDate);
            values.put(GoogleNewsContract.COLUMN_PUBLISHER, topic.publisher);
            if (topic.originImage != null) {
                values.put(GoogleNewsContract.COLUMN_IMAGE_URL, topic.originImage.toString());
            }
            if (topic.thumbnail != null) {
                values.put(GoogleNewsContract.COLUMN_THUMBNAIL_URL, topic.thumbnail.toString());
            }

            contentValues.add(values);
        }

        int inserted = 0;
        if (contentValues.size() > 0) {
            ContentValues[] contentValuesArray = new ContentValues[contentValues.size()];
            contentValues.toArray(contentValuesArray);
            inserted = getContext().getContentResolver().bulkInsert(GoogleNewsContract.CONTENT_URI, contentValuesArray);
        }
        GoogleNewsApplication.trace(getContext().getString(R.string.app_name), String.valueOf(inserted) + " news inserted");

        Time dayTime = new Time();
        dayTime.setToNow();
        int julianCurrentTime = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
        long julianYesterday = dayTime.setJulianDay(julianCurrentTime - 1);
        int deleted = getContext().getContentResolver().delete(
                GoogleNewsContract.CONTENT_URI,
                GoogleNewsContract.COLUMN_DATE + " <= ?",
                new String[] { Long.toString(julianYesterday) });
        GoogleNewsApplication.trace(getContext().getString(R.string.app_name), String.valueOf(deleted) + " news deleted");
    }

    private void notifyFreshNews() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        String lastNotificationKey = getContext().getString(R.string.pref_last_notification);
        long lastSyncTime = preferences.getLong(lastNotificationKey, System.currentTimeMillis());
        if (System.currentTimeMillis() - lastSyncTime < QUARTER_OF_DAY) {
            Cursor cursor = getContext().getContentResolver().query(GoogleNewsContract.CONTENT_URI,
                    QUERY_PROJECTION,
                    null,
                    null,
                    null);
            if (cursor.moveToFirst()) {
                int count = cursor.getCount();
                Random random = new Random(lastSyncTime);
                int index = random.nextInt(count);
                cursor.moveToPosition(index);

                String genre = cursor.getString(Columns.GENRE);
                String title = cursor.getString(Columns.TITLE);

                Notification notification = new NotificationCompat.Builder(getContext())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(genre)
                        .setContentText(title)
                        .build();

                NotificationManager nm = (NotificationManager) getContext()
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                nm.notify(NOTIFICATION_ID, notification);

                preferences.edit()
                        .putLong(lastNotificationKey, System.currentTimeMillis())
                        .commit();
            }
        }
    }
}
