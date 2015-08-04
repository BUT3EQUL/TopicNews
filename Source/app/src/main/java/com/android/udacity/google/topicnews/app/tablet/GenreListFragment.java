package com.android.udacity.google.topicnews.app.tablet;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.android.udacity.google.topicnews.app.GoogleNewsApplication;
import com.android.udacity.google.topicnews.app.R;
import com.android.udacity.google.topicnews.app.google.GoogleNewsTopic;
import com.android.udacity.google.topicnews.app.json.AbstractProjectionAdapter;
import com.android.udacity.google.topicnews.app.provider.GoogleNewsContract;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GenreListFragment extends ListFragment {

    private static final String EXTRA_LAST_POSITION = "last_position";

    private static final int GENRE_LOADER = 3002;

    private GoogleNewsGenreListAdapter mAdapter = null;

    private int mSelectedPosition = -1;

    private ListView mListView = null;

    private GoogleNewsContentObserver mContentObserver = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null
                && savedInstanceState.containsKey(EXTRA_LAST_POSITION)) {
            mSelectedPosition = savedInstanceState.getInt(EXTRA_LAST_POSITION);
        }

        View view = super.onCreateView(inflater, container, savedInstanceState);
        mListView = (ListView) view.findViewById(android.R.id.list);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        mAdapter = new GoogleNewsGenreListAdapter(this);
        setListAdapter(mAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mContentObserver = new GoogleNewsContentObserver();
        mContentObserver.register(getActivity().getContentResolver());

        getLoaderManager().initLoader(GENRE_LOADER, null, mAdapter);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        getLoaderManager().restartLoader(GENRE_LOADER, null, mAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mSelectedPosition != ListView.INVALID_POSITION) {
            outState.putInt(EXTRA_LAST_POSITION, mSelectedPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContentObserver.unregister(getActivity().getContentResolver());
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (mSelectedPosition == position) {
            return;
        }

//        mListView.setItemChecked(mSelectedPosition, false);
        mListView.setItemChecked(position, true);

        mSelectedPosition = position;

        GoogleNewsTopic topic = (GoogleNewsTopic) l.getItemAtPosition(position);
        if (topic != null) {
            Activity activity = getActivity();
            if (activity instanceof Callback) {
                GoogleNewsApplication.debug(getClass().getSimpleName(), topic.title + " selected");
                ((Callback) activity).onItemClick(topic.title);
            }
        }
    }



    private static class GoogleNewsGenreListAdapter extends AbstractProjectionAdapter {

        private static final int PADDING = 10;

        private interface Columns {
            int ID = 0;
            int GENRE = 1;
            int MAX_INDEXES = 2;
        }

        private static final String[] QUERY_PROJECTION = new String[Columns.MAX_INDEXES];
        static {
            QUERY_PROJECTION[Columns.ID] = GoogleNewsContract._ID;
            QUERY_PROJECTION[Columns.GENRE] = GoogleNewsContract.COLUMN_GENRE;
        }

        private GenreListFragment mParent = null;

        private List<String> mGenreList = null;

        public GoogleNewsGenreListAdapter(GenreListFragment parent) {
            super(parent.getActivity());
            mParent = parent;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final ViewHolder viewHolder;

            if (view == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(mContext);
                view = layoutInflater.inflate(R.layout.list_item_genre, null, false);
                viewHolder = new ViewHolder(view);
                viewHolder.titleView.setPadding(PADDING, 0, PADDING, 0);
                viewHolder.titleView.setCompoundDrawablePadding(PADDING);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            GoogleNewsTopic topic = getItem(position);
            viewHolder.titleView.setText(topic.title.substring(0, 1).toUpperCase() + topic.title.substring(1));
            return view;
        }

        @Override
        protected String getQuerySortOrder() {
            return QUERY_PROJECTION[Columns.GENRE] + " ASC";
        }

        @Override
        protected String[] getQueryProjection() {
            return QUERY_PROJECTION;
        }

        @Override
        protected String getQuerySelection() {
            return null;
        }

        @Override
        protected String[] getQuerySelectionArgs() {
            return null;
        }

        @Override
        protected List<GoogleNewsTopic> getRows() {
            List<GoogleNewsTopic> rows = new ArrayList<>();
            for (int i = 0; i < mGenreList.size(); i++) {
                rows.add(GoogleNewsTopic.newCategory(mGenreList.get(i)));
            }
            return rows;
        }

        @Override
        protected void onQueryResult(Map<String, List<GoogleNewsTopic>> map, Cursor cursor) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            if (mGenreList == null) {
                mGenreList = new ArrayList<>();
            }
            mGenreList.clear();
            do {
                String genre = cursor.getString(Columns.GENRE);
                if (!preferences.getBoolean(genre, true)) {
                    continue;
                }
                if (mGenreList.contains(genre)) {
                    continue;
                }
                mGenreList.add(genre);

            } while (cursor.moveToNext());
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            super.onLoadFinished(loader, cursor);
            if (mParent.mSelectedPosition != ListView.INVALID_POSITION) {
                mParent.getListView().setSelection(mParent.mSelectedPosition);
                mParent.getListView().smoothScrollToPosition(mParent.mSelectedPosition);
            }
        }
    }

    private static class ViewHolder {
        final TextView titleView;

        ViewHolder(View view) {
            titleView = (TextView) view.findViewById(R.id.title);
        }
    }

    private class GoogleNewsContentObserver extends ContentObserver {

        public GoogleNewsContentObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            getLoaderManager().restartLoader(GENRE_LOADER, null, mAdapter);
        }

        public void register(ContentResolver resolver) {
            resolver.registerContentObserver(GoogleNewsContract.CONTENT_URI, true, this);
        }

        public void unregister(ContentResolver resolver) {
            resolver.unregisterContentObserver(this);
        }

    }

    public interface Callback {
        void onItemClick(String genre);
    }
}