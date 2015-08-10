package com.android.udacity.google.topicnews.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.android.udacity.google.topicnews.app.sync.GoogleNewsSyncAdapter;
import com.android.udacity.google.topicnews.app.tablet.GenreListFragment;
import com.android.udacity.google.topicnews.app.tablet.NewsDetailListFragment;

public class TopicActivity extends ActionBarActivity
        implements GenreListFragment.Callback {

    private boolean mIsTwoPainMode = false;

    private Handler mHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        setContentView(R.layout.activity_main);

        if (findViewById(R.id.fragment_container) != null) {
            mIsTwoPainMode = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new NewsDetailListFragment(), getString(R.string.tag_fragment_news_detail))
                        .commit();
            }
        } else {
            mIsTwoPainMode = false;
            getSupportActionBar().setElevation(0.0f);
        }

        GoogleNewsSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_topic, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(String genre) {
        GoogleNewsApplication.trace(getClass().getSimpleName(), "onItemClick() 2pain-mode:" + mIsTwoPainMode);
        if (mIsTwoPainMode) {
            Bundle bundle = new Bundle();
            bundle.putString(NewsDetailListFragment.EXTRA_NEWS_GENRE, genre);

            Fragment fragment = new NewsDetailListFragment();
            fragment.setArguments(bundle);

            String tag = getString(R.string.tag_fragment_news_detail);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment, tag)
                    .commit();
        }
    }

    @Override
    public Handler getHandler() {
        return mHandler;
    }
}
