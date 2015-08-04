package com.android.udacity.google.topicnews.app.google;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.udacity.google.topicnews.app.GoogleNewsApplication;
import com.android.udacity.google.topicnews.app.R;
import com.android.udacity.google.topicnews.app.json.AbstractProjectionAdapter;
import com.android.udacity.google.topicnews.app.provider.GoogleNewsContract;
import com.android.udacity.google.topicnews.app.widget.NetImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GoogleNewsListAdapter extends AbstractProjectionAdapter {

    private static final String LOG_TAG = GoogleNewsListAdapter.class.getSimpleName();

    private static final int CATEGORY_HEADER_VIEW_TYPE = 0;

    private static final int NEWS_TOPIC_VIEW_TYPE = 1;

    private static final int PADDING = 10;

    private interface Columns {
        int GENRE = 0;
        int TITLE = 1;
        int CONTENT = 2;
        int DATE = 3;
        int PUBLISHER = 4;
        int THUMBNAIL = 5;
        int URL = 6;
        int ORIGIN_IMAGE = 7;
        int MAX_INDEXES = 8;
    }

    private static final String[] QUERY_PROJECTON = new String[Columns.MAX_INDEXES];
    static {
        QUERY_PROJECTON[Columns.GENRE] = GoogleNewsContract.COLUMN_GENRE;
        QUERY_PROJECTON[Columns.TITLE] = GoogleNewsContract.COLUMN_TITLE;
        QUERY_PROJECTON[Columns.CONTENT] = GoogleNewsContract.COLUMN_CONTENT;
        QUERY_PROJECTON[Columns.DATE] = GoogleNewsContract.COLUMN_DATE;
        QUERY_PROJECTON[Columns.PUBLISHER] = GoogleNewsContract.COLUMN_PUBLISHER;
        QUERY_PROJECTON[Columns.ORIGIN_IMAGE] = GoogleNewsContract.COLUMN_IMAGE_URL;
        QUERY_PROJECTON[Columns.THUMBNAIL] = GoogleNewsContract.COLUMN_THUMBNAIL_URL;
        QUERY_PROJECTON[Columns.URL] = GoogleNewsContract.COLUMN_URL;
    }

    public GoogleNewsListAdapter(Context context) {
        super(context);
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).isNewsTopic() ? NEWS_TOPIC_VIEW_TYPE : CATEGORY_HEADER_VIEW_TYPE;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        final ViewHolder viewHolder;

        if (view == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            view = layoutInflater.inflate(getLayout(position), null, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        GoogleNewsTopic topic = getItem(position);
        if (topic.isNewsTopic()) {
            viewHolder.titleView.setText(topic.title);
            viewHolder.contentView.setText(topic.content);
            viewHolder.publisherView.setText(topic.publisher);
            viewHolder.thumbnailView.setImageURL(topic.thumbnail);
            viewHolder.publishedDateView.setText(topic.getPublishedDate());
        } else {
            viewHolder.titleView.setText(topic.title);
            viewHolder.titleView.setPadding(PADDING, 0, PADDING, 0);
            viewHolder.titleView.setCompoundDrawablePadding(PADDING);
        }
        return view;
    }

    private int getLayout(int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case CATEGORY_HEADER_VIEW_TYPE:
                return R.layout.list_item_header;
            case NEWS_TOPIC_VIEW_TYPE:
                return R.layout.list_item_topic_news;
            default:
                throw new IllegalArgumentException("unknown view type: " + viewType);
        }
    }

    @Override
    protected String getQuerySortOrder() {
        return QUERY_PROJECTON[Columns.GENRE] + " ASC";
    }

    @Override
    protected String[] getQueryProjection() {
        return QUERY_PROJECTON;
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
    protected void onQueryResult(Map<String, List<GoogleNewsTopic>> map, Cursor cursor) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        map.clear();
        do {
            String genre = cursor.getString(Columns.GENRE);
            GoogleNewsApplication.debug(LOG_TAG, genre + " news detected..." + preferences.getBoolean(genre, true));
            if (!preferences.getBoolean(genre, true)) {
                continue;
            }

            String title = cursor.getString(Columns.TITLE);
            String content = cursor.getString(Columns.CONTENT);
            String publisher = cursor.getString(Columns.PUBLISHER);
            String url = cursor.getString(Columns.URL);
            String image = cursor.getString(Columns.ORIGIN_IMAGE);
            String thumbnail = cursor.getString(Columns.THUMBNAIL);
            long date = cursor.getLong(Columns.DATE);

            List<GoogleNewsTopic> topicList = map.get(genre);
            if (topicList == null) {
                topicList = new ArrayList<>();
                map.put(genre, topicList);
            }

            topicList.add(new GoogleNewsTopic(title, content, url, publisher, date, image, thumbnail));
        } while (cursor.moveToNext());
    }

    private static class ViewHolder {
        final TextView titleView;
        final TextView contentView;
        final NetImageView thumbnailView;
        final TextView publisherView;
        final TextView publishedDateView;

        ViewHolder(View view) {
            titleView = (TextView) view.findViewById(R.id.title);
            contentView = (TextView) view.findViewById(R.id.content);
            thumbnailView = (NetImageView) view.findViewById(R.id.picture);
            publisherView = (TextView) view.findViewById(R.id.publisher);
            publishedDateView = (TextView) view.findViewById(R.id.published_date);
        }
    }

}
