package com.android.udacity.google.topicnews.app;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.udacity.google.topicnews.app.google.GoogleNewsListAdapter;
import com.android.udacity.google.topicnews.app.google.GoogleNewsTopic;
import com.android.udacity.google.topicnews.app.provider.GoogleNewsContract;


public class TopicFragment extends ListFragment {

    private static final int TOPIC_LOADER = 3001;

    private GoogleNewsListAdapter mAdapter = null;

    private GoogleNewsContentObserver mContentObserver = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_listview, container, false);

        mAdapter = new GoogleNewsListAdapter(getActivity());
        setListAdapter(mAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mContentObserver = new GoogleNewsContentObserver();
        mContentObserver.register(getActivity().getContentResolver());

        getLoaderManager().initLoader(TOPIC_LOADER, null, mAdapter);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        getLoaderManager().restartLoader(TOPIC_LOADER, null, mAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        GoogleNewsTopic topic = mAdapter.getItem(position);
        if (topic.isNewsTopic()) {
            Intent intent = new Intent(getActivity(), DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_TOPIC_TITLE, topic.title);
            intent.putExtra(DetailActivity.EXTRA_TOPIC_CONTENT, topic.content);
            intent.putExtra(DetailActivity.EXTRA_TOPIC_IMAGE_URL, topic.originImage);
            intent.putExtra(DetailActivity.EXTRA_TOPIC_ANCHOR_URL, topic.url);
            getActivity().startActivity(intent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContentObserver.unregister(getActivity().getContentResolver());
    }

    private class GoogleNewsContentObserver extends ContentObserver {

        public GoogleNewsContentObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            getLoaderManager().restartLoader(TOPIC_LOADER, null, mAdapter);
        }

        public void register(ContentResolver resolver) {
            resolver.registerContentObserver(GoogleNewsContract.CONTENT_URI, true, this);
        }

        public void unregister(ContentResolver resolver) {
            resolver.unregisterContentObserver(this);
        }

    }

}
