package org.quuux.newsie.data;

import android.net.Uri;

import org.quuux.newsie.tasks.EnsureCacheTask;

import java.io.File;

public class CacheManager {

    private static File base;

    public static void setCacheDir(final File base) {
        CacheManager.base = base;
        new EnsureCacheTask().execute();
    }

    public static File getFeedsPath() {
        return new File(base, "feeds");
    }

    public static File getFeedPath(final Feed feed) {
        final String url = feed.getUrl();
        final Uri uri = Uri.parse(url);

        final StringBuilder sb = new StringBuilder();
        sb.append("feed-");
        sb.append(uri.getHost());

        for (final String part : uri.getPathSegments()) {
            sb.append("-");
            sb.append(part);
        }

        sb.append("-");
        sb.append(url.hashCode());
        sb.append(".json");

        return new File(getFeedsPath(), sb.toString());
    }

}
