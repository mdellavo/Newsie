package org.quuux.newsie.data;

import android.content.Context;
import android.net.Uri;

import java.io.File;

public class CacheManager {

    public static File getFeedsPath(final Context context) {
        final File base = context.getCacheDir();
        return new File(base, "feeds");
    }

    public static File getFeedPath(final Context context, final Feed feed) {
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

        return new File(getFeedsPath(context), sb.toString());
    }

}
