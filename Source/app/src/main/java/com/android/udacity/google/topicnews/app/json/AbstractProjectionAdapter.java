package com.android.udacity.google.topicnews.app.json;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.ArrayAdapter;

import com.android.udacity.google.topicnews.app.GoogleNewsApplication;
import com.android.udacity.google.topicnews.app.google.GoogleNewsTopic;
import com.android.udacity.google.topicnews.app.provider.GoogleNewsContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractProjectionAdapter extends ArrayAdapter<GoogleNewsTopic>
        implements LoaderManager.LoaderCallbacks<Cursor> {

    protected Context mContext = null;

    protected List<GoogleNewsTopic> mRows = null;

    protected Map<String, List<GoogleNewsTopic>> mGenreMap = null;

    public AbstractProjectionAdapter(Context context) {
        super(context, 0);
        mContext = context;
        mRows = new ArrayList<>();
        mGenreMap = new HashMap<>();
    }

    protected void loadNewsTopic() {
        new GoogleNewsLoadTask().execute();
    }

    @Override
    public int getCount() {
        return mRows.size();
    }

    @Override
    public GoogleNewsTopic getItem(int position) {
        return mRows.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    protected abstract String getQuerySortOrder();

    protected abstract String[] getQueryProjection();

    protected abstract String getQuerySelection();

    protected abstract String[] getQuerySelectionArgs();
    
    protected abstract void onQueryResult(Map<String, List<GoogleNewsTopic>> map, Cursor cursor);

    protected List<GoogleNewsTopic> getRows() {
        if (mGenreMap != null) {
            List<GoogleNewsTopic> rows = new ArrayList<>();
            for (String key : mGenreMap.keySet()) {
                rows.add(GoogleNewsTopic.newCategory(key));
                rows.addAll(mGenreMap.get(key));
            }
            return rows;
        }
        return null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        GoogleNewsApplication.debug(getClass().getSimpleName(), "onCreateLoader()");

        return new CursorLoader(getContext(),
                GoogleNewsContract.CONTENT_URI,
                getQueryProjection(),
                getQuerySelection(),
                getQuerySelectionArgs(),
                getQuerySortOrder());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        GoogleNewsApplication.debug(getClass().getSimpleName(), "onLoadFinished()");
        if (cursor.moveToFirst()) {
            onQueryResult(mGenreMap, cursor);
            loadNewsTopic();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        GoogleNewsApplication.debug(getClass().getSimpleName(), "onLoaderReset()");
    }

    private class GoogleNewsLoadTask extends AsyncTask<Void, Void, List<GoogleNewsTopic>> {

        @Override
        protected List<GoogleNewsTopic> doInBackground(Void... params) {
            GoogleNewsApplication.debug(getClass().getSimpleName(), "doInBackground()");
            return getRows();
        }

        @Override
        protected void onPostExecute(List<GoogleNewsTopic> googleNewsTopics) {
            GoogleNewsApplication.debug(getClass().getSimpleName(), "onPostExecute()");
            mRows.clear();
            GoogleNewsApplication.debug(getClass().getSimpleName(), "GoogleNewsTopic is empty ? " + googleNewsTopics.isEmpty());
            if (!googleNewsTopics.isEmpty()) {
                mRows.addAll(googleNewsTopics);
            }
            notifyDataSetChanged();
        }
    }
}
