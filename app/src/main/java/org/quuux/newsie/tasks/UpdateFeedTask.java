package org.quuux.newsie.tasks;

import android.os.AsyncTask;

import org.quuux.newsie.Log;
import org.quuux.newsie.data.CacheManager;
import org.quuux.newsie.data.Feed;
import org.quuux.newsie.data.FeedCache;
import org.quuux.newsie.data.FeedUtils;

import java.io.File;
import java.util.List;

public class UpdateFeedTask extends AsyncTask<Feed, Void, List<Feed>> {
    private static final String TAG = Log.buildTag(UpdateFeedTask.class);

    @Override
    protected List<Feed> doInBackground(Feed... feeds) {

        final File path = CacheManager.getFeedPath(feeds[0]);
        path.getParentFile().mkdirs();

        final long t1 = System.currentTimeMillis();
        final List<Feed> rv = FeedUtils.parseUrl(feeds[0].getUrl());
        final long t2 = System.currentTimeMillis();

        Log.d(TAG, "processed feed %s in %sms", feeds[0].getUrl(), t2 - t1);

        return rv;
    }

    @Override
    protected void onPostExecute(List<Feed> updatedFeeds) {
        super.onPostExecute(updatedFeeds);

        final FeedCache cache = FeedCache.getInstance();
        if (updatedFeeds != null) {
            for (Feed updatedFeed : updatedFeeds) {
                cache.onFeedUpdated(updatedFeed);
            }
        }

        cache.onUpdateComplete();
    }
}
