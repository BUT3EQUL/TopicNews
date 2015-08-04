package com.android.udacity.google.topicnews.app.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.android.udacity.google.topicnews.app.GoogleNewsApplication;

public class GoogleNewsProvider extends ContentProvider {

    private static final String DATABASE_NAME = "topic_news.db";

    private static final int DATABASE_VERSION = 1;

    private static final int NORMAL = 101;

    private static final int DISTINCT_MODE = 102;

    private SQLiteOpenHelper mOpenHelper = null;

    private UriMatcher mUriMatcher = null;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        mUriMatcher = buildUriMatcher();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(GoogleNewsContract.TABLE_NAME);
        boolean useDistinct = false;
        switch (mUriMatcher.match(uri)) {
            case DISTINCT_MODE:
                useDistinct = true;
            case NORMAL:
                queryBuilder.setTables(GoogleNewsContract.TABLE_NAME);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
        queryBuilder.setDistinct(useDistinct);

        SQLiteDatabase database = mOpenHelper.getReadableDatabase();
        return queryBuilder.query(database,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    @Override
    public String getType(Uri uri) {
        return GoogleNewsContract.CONTENT_TYPE;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id = mOpenHelper.getWritableDatabase().insert(GoogleNewsContract.TABLE_NAME,
                null,
                values);
        if (id > 0) {
            return GoogleNewsContract.buildGoogleNewsUri(id);
        }
        throw new SQLiteException("Failed to insert row into " + uri);
    }

    @Override
    public int bulkInsert(Uri uri, @NonNull ContentValues[] values) {
        SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        int insertedRow = 0;
        database.beginTransaction();
        try {
            for (ContentValues contentValues : values) {
                normalizeDate(contentValues);
                long id = database.insert(GoogleNewsContract.TABLE_NAME, null, contentValues);
                if (id > 0) {
                    insertedRow++;
                }
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return insertedRow;
    }

    private void normalizeDate(ContentValues values) {
        if (values.containsKey(GoogleNewsContract.COLUMN_DATE)) {
            long dateValue = values.getAsLong(GoogleNewsContract.COLUMN_DATE);
            values.put(GoogleNewsContract.COLUMN_DATE, GoogleNewsContract.normalizeDate(dateValue));
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int deletedRow = mOpenHelper.getWritableDatabase().delete(GoogleNewsContract.TABLE_NAME,
                selection,
                selectionArgs);
        if (deletedRow != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return deletedRow;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int updatedRow = mOpenHelper.getWritableDatabase().update(GoogleNewsContract.TABLE_NAME,
                values,
                selection,
                selectionArgs);
        if (updatedRow != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updatedRow;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            final String SQL_CREATE_TABLE = "CREATE TABLE " + GoogleNewsContract.TABLE_NAME + "( " +
                    GoogleNewsContract._ID + " INTEGER PRIMARY KEY, " +
                    GoogleNewsContract.COLUMN_DATE + " INTEGER NOT NULL, " +
                    GoogleNewsContract.COLUMN_GENRE + " TEXT NOT NULL, " +
                    GoogleNewsContract.COLUMN_TITLE + " TEXT NOT NULL, " +
                    GoogleNewsContract.COLUMN_CONTENT + " TEXT NOT NULL, " +
                    GoogleNewsContract.COLUMN_PUBLISHER + " TEXT NOT NULL, " +
                    GoogleNewsContract.COLUMN_IMAGE_URL + " TEXT, " +
                    GoogleNewsContract.COLUMN_THUMBNAIL_URL + " TEXT, " +
                    GoogleNewsContract.COLUMN_URL + " TEXT NOT NULL" +
                    " );";
            GoogleNewsApplication.debug("GoogleNewsProvider", SQL_CREATE_TABLE);
            db.execSQL(SQL_CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            final String SQL_UPDATE_TABLE = "DROP TABLE IF EXISTS " + GoogleNewsContract.TABLE_NAME;
            db.execSQL(SQL_UPDATE_TABLE);
            onCreate(db);
        }
    }

    static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(GoogleNewsContract.AUTHORITY, GoogleNewsContract.PATH_DEFAULT, NORMAL);
        matcher.addURI(GoogleNewsContract.AUTHORITY, GoogleNewsContract.PATH_DISTINCT, DISTINCT_MODE);
        return matcher;
    }

}
