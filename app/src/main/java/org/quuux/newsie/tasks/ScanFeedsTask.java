package org.quuux.newsie.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;

import org.quuux.newsie.Log;
import org.quuux.newsie.data.CacheManager;
import org.quuux.newsie.data.Feed;
import org.quuux.newsie.data.FeedCache;
import org.quuux.newsie.data.FeedGroup;
import org.quuux.newsie.data.FeedNode;
import org.quuux.sack.Sack;

import java.io.File;
import java.util.Arrays;

public class ScanFeedsTask extends AsyncTask<Void, Void, FeedGroup> {
    private static final String TAG = Log.buildTag(ScanFeedsTask.class);

    @Override
    protected FeedGroup doInBackground(Void... params) {
        return loadFeedGroup(CacheManager.getFeedsPath());
    }

    @Override
    protected void onPostExecute(FeedGroup feedGroup) {
        super.onPostExecute(feedGroup);
        FeedCache.getInstance().setRoot(feedGroup);
    }

    private Feed loadFeed(final File path) {

        Log.d(TAG, "loading feed: %s", path);

        final Sack<Feed> sack = Sack.open(Feed.class, path);
        final Pair<Sack.Status, Feed> result = sack.doLoad();
        if (result.first == Sack.Status.ERROR) {
            Log.d(TAG, "error loading feed: %s", path);
            return null;
        }
        return result.second;
    }

    private FeedGroup loadFeedGroup(final File path) {

        Log.d(TAG, "loaded feed group: %s", path);

        final FeedGroup group = new FeedGroup(path.getName());

        final File[] files = path.listFiles();

        Log.d(TAG, "filenames: %s", Arrays.toString(files));

        if (files != null) {
            for (File file : files) {

                Log.d(TAG, "consider: %s", file);

                FeedNode feed = null;
                if (file.isDirectory()) {
                    feed = loadFeedGroup(file);
                } else if (file.getName().endsWith(".json")) {
                    feed = loadFeed(file);
                }

                if (feed != null) {
                    group.addFeed(feed);
                }
            }
        }

        return group;
    }

}
