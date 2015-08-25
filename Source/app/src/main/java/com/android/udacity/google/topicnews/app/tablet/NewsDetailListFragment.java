package com.android.udacity.google.topicnews.app.tablet;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.android.udacity.google.topicnews.app.GoogleNewsApplication;
import com.android.udacity.google.topicnews.app.R;
import com.android.udacity.google.topicnews.app.google.GoogleNewsTopic;
import com.android.udacity.google.topicnews.app.provider.GoogleNewsContract;

public class NewsDetailListFragment extends ListFragment {

    private static final int DETAIL_LOADER = 3003;

    public static final String EXTRA_NEWS_GENRE = "news_genre";

    private static final String EXTRA_VIEWING_GENRE = "viewing_genre";

    private static final long WEBVIEW_CACHE_SIZE = 8 * 1024 * 1024;

    private GoogleNewsDetailListAdapter mAdapter = null;

    private String mViewingGenre = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_VIEWING_GENRE)) {
            mViewingGenre = savedInstanceState.getString(EXTRA_VIEWING_GENRE);
        } else {
            Bundle bundle = getArguments();
            if (bundle != null && bundle.containsKey(EXTRA_NEWS_GENRE)) {
                mViewingGenre = bundle.getString(EXTRA_NEWS_GENRE);
            }
        }

        View view = super.onCreateView(inflater, container, savedInstanceState);

        mAdapter = new GoogleNewsDetailListAdapter(getActivity(), mViewingGenre);
        setListAdapter(mAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        setEmptyText(getString(R.string.list_content_no_topic));
        if (mViewingGenre != null) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, mAdapter);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mViewingGenre != null) {
            outState.putString(EXTRA_VIEWING_GENRE, mViewingGenre);
        }
        super.onSaveInstanceState(outState);
    }

    public void updateCursor(String genre) {
        mAdapter.setGenre(genre);
        getLoaderManager().restartLoader(DETAIL_LOADER, null, mAdapter);
    }

    public void clearCursor() {
        mAdapter.swapCursor(null);
    }

    private static class GoogleNewsDetailListAdapter extends CursorAdapter
            implements LoaderManager.LoaderCallbacks<Cursor> {

        private interface Columns {
            int DATE = 0;
            int TITLE = 1;
            int CONTENT = 2;
            int IMAGE_URL = 3;
            int ANCHOR_URL = 4;
            int ID = 5;
            int PUBLISHER = 6;
            int MAX_INDEXES = 7;
        }

        private static final String[] QUERY_PROJECTION = new String[Columns.MAX_INDEXES];
        static {
            QUERY_PROJECTION[Columns.ID] = GoogleNewsContract._ID;
            QUERY_PROJECTION[Columns.DATE] = GoogleNewsContract.COLUMN_DATE;
            QUERY_PROJECTION[Columns.PUBLISHER] = GoogleNewsContract.COLUMN_PUBLISHER;
            QUERY_PROJECTION[Columns.TITLE] = GoogleNewsContract.COLUMN_TITLE;
            QUERY_PROJECTION[Columns.CONTENT] = GoogleNewsContract.COLUMN_CONTENT;
            QUERY_PROJECTION[Columns.IMAGE_URL] = GoogleNewsContract.COLUMN_IMAGE_URL;
            QUERY_PROJECTION[Columns.ANCHOR_URL] = GoogleNewsContract.COLUMN_URL;
        }

        private String mGenre = null;

        public GoogleNewsDetailListAdapter(Context context, String gnere) {
            super(context, null, 0);
            mGenre = gnere;
        }

        public String getGenre() {
            return mGenre;
        }

        public void setGenre(String genre) {
            mGenre = genre;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.fragment_detail, parent, false);

            ViewHolder viewHolder = new ViewHolder(view);
            setupWebView(viewHolder.webView);
            view.setTag(viewHolder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder viewHolder = (ViewHolder) view.getTag();
            if (viewHolder == null) {
                viewHolder = new ViewHolder(view);
            }

            String title = cursor.getString(Columns.TITLE);
            String content = cursor.getString(Columns.CONTENT);
            String imageUrl = cursor.getString(Columns.IMAGE_URL);
            String anchor = cursor.getString(Columns.ANCHOR_URL);
            long publishedData = cursor.getLong(Columns.DATE);
            String publisher = cursor.getString(Columns.PUBLISHER);

            GoogleNewsTopic topic = GoogleNewsTopic.newTopicNews(title, content, anchor, publisher, publishedData, imageUrl, null);
            GoogleNewsApplication.debug(getClass().getSimpleName(), buildHtmlPage(topic));
            viewHolder.webView.loadData(buildHtmlPage(topic), "text/html", "utf-8");

            viewHolder.title = title;
            viewHolder.url = anchor;
        }

        private void setupWebView(final WebView webView) {
            WebSettings settings = webView.getSettings();
            settings.setAppCacheEnabled(true);
            settings.setAppCacheMaxSize(WEBVIEW_CACHE_SIZE);
            settings.setAppCachePath(mContext.getCacheDir().getPath());

            webView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    WebView.HitTestResult result = null;
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_UP:
                            result = webView.getHitTestResult();
                            break;
                    }

                    if (result == null) {
                        return false;
                    }

                    String anchor = getAnchor(result);
                    if (anchor != null) {
                        return false;
                    }

                    ViewHolder viewHolder = (ViewHolder) webView.getTag();
                    if (viewHolder == null) {
                        return true;
                    }

                    GoogleNewsApplication.debug("GoogleNewsDetailListAdapter", "title=" + viewHolder.title);
                    GoogleNewsApplication.debug("GoogleNewsDetailListAdapter", "url=" + viewHolder.url);

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, viewHolder.title);
                    intent.putExtra(Intent.EXTRA_TEXT, viewHolder.url);

                    mContext.startActivity(intent);
                    return true;
                }

                private String getAnchor(WebView.HitTestResult result) {
                    switch (result.getType()) {
                        case WebView.HitTestResult.SRC_ANCHOR_TYPE:
                        case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
                            return result.getExtra();
                    }
                    return null;
                }
            });
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (mGenre == null) {
                return null;
            }
            return new CursorLoader(mContext,
                    GoogleNewsContract.CONTENT_URI,
                    getQueryProjection(),
                    getQuerySelection(),
                    getQuerySelectionArgs(),
                    getQuerySortOrder());
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            swapCursor(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            swapCursor(null);
        }

        protected String getQuerySortOrder() {
            return QUERY_PROJECTION[Columns.DATE] + " DESC";
        }

        protected String[] getQueryProjection() {
            return QUERY_PROJECTION;
        }

        protected String getQuerySelection() {
            return GoogleNewsContract.COLUMN_GENRE + " = ?";
        }

        protected String[] getQuerySelectionArgs() {
            return new String[] { mGenre };
        }

        private String buildHtmlPage(GoogleNewsTopic topic) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("<html><head><title>");
            stringBuffer.append(topic.title);
            stringBuffer.append("</title></head><body>");
            if (topic.url != null) {
                stringBuffer.append("<div align=\"right\"><small>[<a href=\"clicked\">");
                stringBuffer.append(mContext.getString(R.string.action_share));
                stringBuffer.append("</a>]</small></div>");
            }
            stringBuffer.append("<h3>");
            stringBuffer.append(topic.title);
            stringBuffer.append("</h3>" );
            stringBuffer.append("<div align=\"right\">");
            stringBuffer.append(topic.publisher);
            stringBuffer.append("&nbsp-&nbsp");
            stringBuffer.append(topic.getPublishedDate());
            stringBuffer.append("</div>");
            stringBuffer.append("<hr>");
            stringBuffer.append("<p>");
            if (topic.originImage != null) {
                stringBuffer.append("<img src=\"");
                stringBuffer.append(topic.originImage);
                stringBuffer.append("\" width=\"50%\" align=\"right\">");
            }
            stringBuffer.append(topic.content);
            stringBuffer.append("</p>");
            if (topic.url != null) {
                stringBuffer.append("<a href=\"");
                stringBuffer.append(topic.url);
                stringBuffer.append("\">detail ...</a>");
            }
            stringBuffer.append("</body></html>");
            return stringBuffer.toString();
        }

    }

    private static class ViewHolder {
        final WebView webView;
        String title = null;
        String url = null;

        ViewHolder(View view) {
            webView = (WebView) view;
        }
    }

}
