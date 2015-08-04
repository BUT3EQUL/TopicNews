package com.android.udacity.google.topicnews.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import java.net.URL;


public class DetailFragment extends Fragment {

    private static final String MIME_TYPE = "text/html";

    private static final String ENCODING = "utf-8";

    private WebView mWebView = null;

    private ShareActionProvider mShareActionProvider = null;

    private String mTitle = null;

    private String mAnchorSource = null;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mWebView = (WebView) rootView.findViewById(R.id.webview);

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            mTitle = intent.getStringExtra(DetailActivity.EXTRA_TOPIC_TITLE);
            String content = intent.getStringExtra(DetailActivity.EXTRA_TOPIC_CONTENT);
            URL imageSource = (URL) intent.getSerializableExtra(DetailActivity.EXTRA_TOPIC_IMAGE_URL);
            URL anchorSource = (URL) intent.getSerializableExtra(DetailActivity.EXTRA_TOPIC_ANCHOR_URL);
            mAnchorSource = anchorSource != null ? anchorSource.toString() : null;

            mWebView.loadData(buildHtmlPage(mTitle, content, imageSource, anchorSource),
                    MIME_TYPE,
                    ENCODING);
            rootView.setContentDescription(content);
        }

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);
        MenuItem shareAction = menu.findItem(R.id.action_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareAction);
        if (mAnchorSource != null) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, mTitle);
            intent.putExtra(Intent.EXTRA_TEXT, mAnchorSource);

            mShareActionProvider.setShareIntent(intent);
        }
    }

    private String buildHtmlPage(String title, String content, URL imageSource, URL anchorSource) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<html><head><title>");
        stringBuffer.append(title);
        stringBuffer.append("</title></head><body><h3>");
        stringBuffer.append(title);
        stringBuffer.append("</h3><hr>");
        stringBuffer.append("<p><img src=\"");
        stringBuffer.append(imageSource);
        stringBuffer.append("\" width=\"50%\" align=\"right\">");
        stringBuffer.append(content);
        stringBuffer.append("</p><a href=\"");
        stringBuffer.append(anchorSource);
        stringBuffer.append("\">detail ...</a></body></html>");
        return stringBuffer.toString();
    }

}
