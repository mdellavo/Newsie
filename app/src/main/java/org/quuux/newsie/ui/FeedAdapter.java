package org.quuux.newsie.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.quuux.newsie.Log;
import org.quuux.newsie.R;
import org.quuux.newsie.data.DataStore;
import org.quuux.newsie.data.Feed;
import org.quuux.newsie.data.FeedGroup;

import java.util.LinkedList;
import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    private static final String TAG = Log.buildTag(FeedAdapter.class);
    private Listener listener;

    public interface Listener {
        void onFeedClicked(Feed feed);
    }

    public static class FeedViewHolder extends RecyclerView.ViewHolder {
        final TextView title, url;

        public FeedViewHolder(View itemView) {
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.title);
            url = (TextView)itemView.findViewById(R.id.url);
        }
    }

    private final List<Feed> feeds = new LinkedList<>();

    public FeedAdapter() {
        update();
    }

    @Override
    public FeedViewHolder onCreateViewHolder(ViewGroup parent, int itemType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View view = inflater.inflate(R.layout.feed_item, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FeedViewHolder viewHolder, int position) {
        final Feed feed = feeds.get(position);
        viewHolder.title.setText(feed.getTitle());
        viewHolder.url.setText(feed.getUrl());

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.onFeedClicked(feed);
            }
        });
    }

    @Override
    public int getItemCount() {
        return feeds.size();
    }

    public void update() {
        feeds.clear();

        final DataStore dataStore = DataStore.getInstance();
        addFeeds(dataStore.getFeeds());
    }

    private void addFeeds(List<Feed> feeds) {
        for (final Feed feed : feeds) {
            this.feeds.add(feed);
            if (feed instanceof FeedGroup)
                addFeeds(((FeedGroup) feed).getFeeds());
        }

        Log.d(TAG, "added %s feeds", feeds.size());
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

}
