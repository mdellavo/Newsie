package org.quuux.newsie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.quuux.feller.Log;
import org.quuux.newsie.data.FeedCache;
import org.quuux.newsie.data.Feed;
import org.quuux.newsie.data.FeedUtils;

import java.util.List;
import java.util.regex.Matcher;


public class AddFeedActivity extends AppCompatActivity implements FeedUtils.FeedDiscoveryListener, AdapterView.OnItemClickListener {

    private static final String TAG = Log.buildTag(AddFeedActivity.class);
    ListView list;
    Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_feed);

        list = (ListView) findViewById(R.id.list);
        list.setOnItemClickListener(this);

        load();
    }

    private String extractUrl(final String text) {
        String url = null;
        final Matcher matcher = Patterns.WEB_URL.matcher(text);
        while (matcher.find()) {
            final String nextUrl = matcher.group();
            if (url == null || nextUrl.length() > url.length())
                url = nextUrl;
        }

        return url;
    }

    public String extractUrl(final Intent intent) {
        return extractUrl(intent.getStringExtra(Intent.EXTRA_TEXT));
    }

    private void load() {
        final String url = extractUrl(getIntent());
        if (url == null) {
            onLoadError();
            return;
        }

        FeedUtils.discoverFeedAsync(url, this);
    }

    private void onLoadError() {
    }

    @Override
    public void onFeedDiscovery(final List<Feed> feeds) {
        adapter = new Adapter(feeds);
        list.setAdapter(adapter);
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, int position, long id) {
        final Feed feed = (Feed) adapter.getItem(position);
        FeedCache.getInstance().addFeed(feed);
    }

    static class Holder {
        TextView title, url;
    }

    class Adapter extends BaseAdapter {

        final List<Feed> feeds;

        public Adapter(final List<Feed> feeds) {
            this.feeds = feeds;
        }

        @Override
        public int getCount() {
            return feeds.size();
        }

        @Override
        public Object getItem(int position) {
            return feeds.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View view = convertView != null ? convertView : newView(parent);
            bindView(view, (Feed)getItem(position));
            return view;
        }

        private View newView(final ViewGroup parent) {
            final LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            final View v = inflater.inflate(R.layout.add_feed_item, parent, false);
            final Holder holder = new Holder();
            holder.title = (TextView)v.findViewById(R.id.title);
            holder.url = (TextView)v.findViewById(R.id.url);
            v.setTag(holder);
            return v;
        }

        private void bindView(View view, Feed item) {
            final Holder holder = (Holder) view.getTag();
            holder.title.setText(item.getTitle());
            holder.url.setText(item.getUrl());
        }
    }
}
