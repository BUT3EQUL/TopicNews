package com.android.udacity.google.topicnews.app.tablet;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.udacity.google.topicnews.app.R;
import com.android.udacity.google.topicnews.app.google.GoogleNewsTopic;
import com.android.udacity.google.topicnews.app.json.AbstractProjectionAdapter;
import com.android.udacity.google.topicnews.app.provider.GoogleNewsContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoogleNewsGenreListAdapter extends AbstractProjectionAdapter {

    private static final String LOG_TAG = GoogleNewsGenreListAdapter.class.getSimpleName();

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
        StringBuffer buffer = new StringBuffer();

        String[] preferenceKeys = getContext().getResources().getStringArray(R.array.pref_genre_keys);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        for (String key : preferenceKeys) {
            boolean enabled = preferences.getBoolean(key, true);
            if (enabled) {
                if (buffer.length() > 0) {
                    buffer.append(" OR ");
                }
                buffer.append(GoogleNewsContract.COLUMN_GENRE);
                buffer.append(" = ?");
            }
        }
        return buffer.toString();
    }

    @Override
    protected String[] getQuerySelectionArgs() {
        String[] preferenceKeys = getContext().getResources().getStringArray(R.array.pref_genre_keys);
        String[] genres = getContext().getResources().getStringArray(R.array.pref_genre_keys);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        String[] enabledGenre = new String[preferenceKeys.length];
        int index = 0;

        for (int i = 0; i < preferenceKeys.length; i++) {
            boolean enabled = preferences.getBoolean(preferenceKeys[i], true);
            if (enabled) {
                enabledGenre[index++] = genres[i];
            }
        }

        if (index > 0) {
            String[] selections = new String[index];
            System.arraycopy(enabledGenre, 0, selections, 0, index);
            return selections;
        } else {
            return null;
        }
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
        if (cursor.getCount() == 0) {
            return;
        }
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

        HashMap<String, Integer> rankMap = new HashMap<>();
        final ArrayList<String> genreList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                String genre = cursor.getString(Columns.GENRE);
                if (rankMap.containsKey(genre)) {
                    int rank = rankMap.get(genre);
                    rankMap.put(genre, ++rank);
                } else {
                    genreList.add(genre);
                    rankMap.put(genre, 1);
                }
            } while (cursor.moveToNext());
        }

        if (genreList.size() == 1) {
            Activity activity = mParent.getActivity();
            if (activity instanceof GenreListFragment.Callback) {
                final GenreListFragment.Callback callback = (GenreListFragment.Callback) activity;
                Handler handler = callback.getHandler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mParent.mListView.setItemChecked(0, true);
                        callback.onItemClick(genreList.get(0));
                    }
                });
            }
        }
    }

    List<String> getGenreList() {
        return mGenreList;
    }

    private class ViewHolder {
        final TextView titleView;

        ViewHolder(View view) {
            titleView = (TextView) view.findViewById(R.id.title);
        }
    }

}
