package org.quuux.newsie.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.quuux.newsie.Log;
import org.quuux.newsie.R;
import org.quuux.newsie.data.FeedCache;
import org.quuux.newsie.data.Feed;
import org.quuux.newsie.data.FeedGroup;
import org.quuux.newsie.data.FeedNode;

import java.util.LinkedList;
import java.util.List;

public class FeedsAdapter extends RecyclerView.Adapter<FeedsAdapter.FeedViewHolder> {

    private static final String TAG = Log.buildTag(FeedsAdapter.class);
    private Listener listener;

    public interface Listener {
        void onFeedClicked(Feed feed);
    }

    public static class FeedViewHolder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView title;

        public FeedViewHolder(View itemView) {
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.title);
            icon = (ImageView)itemView.findViewById(R.id.icon);
        }
    }

    private final List<FeedNode> feeds = new LinkedList<>();

    public FeedsAdapter() {
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

        final FeedNode feed = feeds.get(position);
        viewHolder.title.setText(feed.getDisplayName());

        Picasso.with(viewHolder.icon.getContext()).load(feed.getIconUrl()).into(viewHolder.icon);

        if (feed instanceof Feed) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onFeedClicked((Feed) feed);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return feeds.size();
    }

    public void update() {
        feeds.clear();

        final FeedCache cache = FeedCache.getInstance();
        addFeeds(cache.getRoot());
    }

    private void addFeeds(FeedGroup group) {
        for (final FeedNode feed : group.getFeeds()) {
            this.feeds.add(feed);
            if (feed instanceof FeedGroup)
                addFeeds((FeedGroup) feed);
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

}
