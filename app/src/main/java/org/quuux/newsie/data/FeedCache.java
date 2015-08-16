package org.quuux.newsie.data;

import android.os.AsyncTask;

import org.quuux.newsie.EventBus;
import org.quuux.newsie.Log;
import org.quuux.newsie.events.FeedUpdated;
import org.quuux.newsie.events.FeedsLoaded;
import org.quuux.newsie.events.FeedsUpdated;
import org.quuux.newsie.events.FeedsUpdating;
import org.quuux.newsie.tasks.ScanFeedsTask;
import org.quuux.newsie.tasks.UpdateFeedTask;
import org.quuux.sack.Sack;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class FeedCache {

    private static final String TAG = Log.buildTag(FeedCache.class);
    private static FeedCache instance;

    private FeedGroup root = null;
    private Map<String, Feed> feedMap = new HashMap<>();

    protected FeedCache() {}

    public static FeedCache getInstance() {
        if (instance == null)
            instance = new FeedCache();
        return instance;
    }

    public boolean hasRoot() {
        return root != null;
    }

    public void setRoot(FeedGroup feedGroup) {
        root = feedGroup;
        feedMap.clear();
        indexFeed(root);
        EventBus.getInstance().post(new FeedsLoaded());
    }

    public FeedGroup getRoot() {
        return root;
    }

    public Feed getFeed(final String feedUrl) {
        return feedMap.get(feedUrl);
    }

    public void scanFeeds() {
        new ScanFeedsTask().execute();
    }

    public void addFeed(final Feed feed) {
        if (getFeed(feed.getUrl()) == null) {
            root.addFeed(feed);
            indexFeed(feed);
            updateFeed(feed);
        }
    }

    private void indexFeed(final FeedNode node) {
        if (node instanceof Feed) {
            final Feed feed = (Feed)node;
            feedMap.put(feed.getUrl(), feed);
        }

        final List<FeedNode> children = node.getFeeds();
        for (FeedNode child : children) {
            indexFeed(child);
        }
    }

    final AtomicInteger updateCounter = new AtomicInteger();

    public boolean isUpdating() {
        return updateCounter.get() > 0;
    }

    public void onUpdateStarted() {
        final int count = updateCounter.incrementAndGet();
        if (count > 0) {
            EventBus.getInstance().post(new FeedsUpdating());
        }
    }

    public void onUpdateComplete() {
        final int count = updateCounter.decrementAndGet();
        if (count == 0) {
            EventBus.getInstance().post(new FeedsUpdated());
        }
    }

    public void updateFeed(final Feed feed) {
        onUpdateStarted();
        new UpdateFeedTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, feed);
    }

    public void updateFeeds() {
        for (Feed feed : feedMap.values()) {
            updateFeed(feed);
        }
    }


    public void onFeedUpdated(Feed updatedFeed) {
        final Feed feed = getFeed(updatedFeed.getUrl());
        feed.refresh(updatedFeed);
        commitFeed(feed);
    }

    public void commitFeed(final Feed feed) {
        final File path = CacheManager.getFeedPath(feed);
        Sack<Feed> sack = Sack.open(Feed.class, path);
        sack.commit(feed, new Sack.Listener<Feed>() {
            @Override
            public void onResult(Sack.Status status, Feed feed) {
                Log.d(TAG, "committed %s: %s", path, status);
            }
        });
        EventBus.getInstance().post(new FeedUpdated(feed));

    }

    public void markFeedAsRead(final Feed feed) {
        boolean dirty = false;

        for (FeedItem item : feed.getItems()) {
            if (!item.isRead()) {
                dirty = true;
                item.markRead();
            }
        }

        if (dirty)
            commitFeed(feed);
    }

    public void markAllFeedsAsRead() {
        for (Feed feed : feedMap.values()) {
            markFeedAsRead(feed);
        }
    }
}
