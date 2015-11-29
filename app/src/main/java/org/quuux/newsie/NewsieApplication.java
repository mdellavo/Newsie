package org.quuux.newsie;

import android.app.Application;
import android.net.http.HttpResponseCache;

import org.quuux.feller.Log;
import org.quuux.newsie.data.CacheManager;
import org.quuux.newsie.data.FeedCache;

import java.io.File;
import java.io.IOException;

public class NewsieApplication extends Application {
    private static final String TAG = Log.buildTag(NewsieApplication.class);

    @Override
    public void onCreate() {
        super.onCreate();

        CacheManager.setCacheDir(getExternalCacheDir());
        FeedCache.getInstance().scanFeeds();

        try {
            final File httpCacheDir = new File(getExternalCacheDir(), "http");
            final long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
            HttpResponseCache.install(httpCacheDir, httpCacheSize);
        } catch (IOException e) {
            Log.e(TAG, "error setting http cache", e);
        }
    }
}
