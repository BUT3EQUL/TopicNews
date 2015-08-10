package com.android.udacity.google.topicnews.app.tablet;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.udacity.google.topicnews.app.GoogleNewsApplication;
import com.android.udacity.google.topicnews.app.google.GoogleNewsTopic;
import com.android.udacity.google.topicnews.app.provider.GoogleNewsContract;

import java.util.List;

public class GenreListFragment extends ListFragment {

    private static final String EXTRA_SELECTION_GENRE = "selection_genre";

    private static final int GENRE_LOADER = 3002;

    private GoogleNewsGenreListAdapter mAdapter = null;

    protected int mSelectedPosition = -1;

    protected String mSelectedGenre = null;

    protected ListView mListView = null;

    private GoogleNewsContentObserver mContentObserver = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null
            && savedInstanceState.containsKey(EXTRA_SELECTION_GENRE)) {
            mSelectedGenre = savedInstanceState.getString(EXTRA_SELECTION_GENRE);
        }

        View view = super.onCreateView(inflater, container, savedInstanceState);
        mListView = (ListView) view.findViewById(android.R.id.list);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.clearChoices();

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
        if (mSelectedGenre != null) {
            outState.putString(EXTRA_SELECTION_GENRE, mSelectedGenre);
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

        mListView.setItemChecked(position, true);

        mSelectedPosition = position;

        List<String> rows = mAdapter.getGenreList();
        mSelectedGenre = rows.get(position);

        GoogleNewsTopic topic = (GoogleNewsTopic) l.getItemAtPosition(position);
        if (topic != null) {
            Activity activity = getActivity();
            if (activity instanceof Callback) {
                GoogleNewsApplication.debug(getClass().getSimpleName(), topic.title + " selected");
                ((Callback) activity).onItemClick(topic.title);
            }
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
        Handler getHandler();
        void onItemClick(String genre);
    }

}