package org.quuux.newsie.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;

import org.quuux.newsie.Log;
import org.quuux.sack.Sack;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public FeedGroup getRoot() {
        return root;
    }

    public Feed getFeed(final String feedUrl) {
        return feedMap.get(feedUrl);
    }

    public void scanFeeds(final Context context, final Runnable onComplete) {
        feedMap.clear();
        new ScanFeedsTask(onComplete).execute(context);
    }

    public void addFeed(final Context context, final Feed feed) {
        if (!feedMap.containsKey(feed.getUrl())) {
            root.addFeed(feed);
            indexFeed(feed);
        }

        updateFeed(context, feed);
    }

    private void indexFeed(final Feed feed) {
        feedMap.put(feed.getUrl(), feed);
    }

    private Feed loadFeed(final File path) {

        Log.d(TAG, "loading feed: %s", path);

        final Sack<Feed> sack = Sack.open(Feed.class, path);
        final Pair<Sack.Status, Feed> result = sack.doLoad();
        if (result.first == Sack.Status.ERROR) {
            Log.d(TAG, "error loading feed: %s", path);
            return null;
        }

        indexFeed(result.second);

        return result.second;
    }

    private FeedGroup loadFeedGroup(final File path) {

        Log.d(TAG, "loaded feed group: %s", path);

        final FeedGroup group = new FeedGroup(path.getName());

        final String[] filenames = path.list();
        if (filenames != null) {
            for (String filename : filenames) {
                final File file = new File(path, filename);

                FeedNode feed = null;
                if (file.isDirectory()) {
                    feed = loadFeedGroup(file);
                } else if (filename.endsWith(".json")) {
                    feed = loadFeed(file);
                }

                if (feed != null) {
                    group.addFeed(feed);
                }
            }
        }

        return group;
    }

    public void updateFeed(final Context context, final Feed feed) {
        new UpdateFeedTask(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, feed);
    }

    class UpdateFeedTask extends AsyncTask<Feed, Void, List<Feed>> {
        final WeakReference<Context> context;

        public UpdateFeedTask(final Context context) {
            this.context = new WeakReference<Context>(context);
        }

        @Override
        protected List<Feed> doInBackground(Feed... feeds) {

            final Context context = this.context.get();
            if (context == null)
                return null;

            final File path = CacheManager.getFeedPath(context, feeds[0]);
            path.getParentFile().mkdirs();

            final long t1 = System.currentTimeMillis();
            final List<Feed> rv = FeedUtils.parseUrl(feeds[0].getUrl());
            final long t2 = System.currentTimeMillis();

            Log.d(TAG, "processed feed %s in %sms", feeds[0].getUrl(), t2-t1);

            return rv;
        }

        @Override
        protected void onPostExecute(List<Feed> updatedFeeds) {
            super.onPostExecute(updatedFeeds);

            if (updatedFeeds == null)
                return;

            final Context context = this.context.get();
            if (context == null)
                return;

            for (Feed updatedFeed : updatedFeeds) {
                final Feed feed = feedMap.get(updatedFeed.getUrl());
                feed.refresh(updatedFeed);
                Sack<Feed> sack = Sack.open(Feed.class, CacheManager.getFeedPath(context, feed));
                sack.commit(feed);
            }
        }
    }

    class ScanFeedsTask extends AsyncTask<Context, Void, FeedGroup> {
        final Runnable onComplete;
        public ScanFeedsTask(Runnable onComplete) {
            this.onComplete = onComplete;
        }

        @Override
        protected FeedGroup doInBackground(Context... params) {
            return loadFeedGroup(CacheManager.getFeedsPath(params[0]));
        }

        @Override
        protected void onPostExecute(FeedGroup feedGroup) {
            super.onPostExecute(feedGroup);
            root = feedGroup;

            if (onComplete != null)
                onComplete.run();
        }
    }
}
