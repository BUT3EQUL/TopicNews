package com.android.udacity.google.topicnews.app;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;


public class DetailActivity extends ActionBarActivity {

    static final String EXTRA_TOPIC_TITLE = "topic_title";
    static final String EXTRA_TOPIC_CONTENT = "topic_content";
    static final String EXTRA_TOPIC_IMAGE_URL = "topic_image_url";
    static final String EXTRA_TOPIC_ANCHOR_URL = "topic_detail_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
    }

}
