package org.quuux.newsie.data;


import android.os.AsyncTask;
import android.util.Xml;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.quuux.feller.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FeedUtils {

    private static final String TAG = Log.buildTag(FeedUtils.class);

    public interface FeedDiscoveryListener {
        void onFeedDiscovery(List<Feed> feeds);
    }

    public static List<Feed> discoverFeeds(final String url) throws IOException {
        final Document doc = Jsoup.connect(url).get();
        final Elements links = doc.select("link[rel=alternate][type=application/rss+xml][href]");

        final List<Feed> feeds = new ArrayList<>();
        for (Element link : links) {
            final Feed feed = new Feed(link.attr("href"));
            feed.setTitle(link.attr("title"));
            feeds.add(feed);
        }

        return feeds;
    }

    public static void discoverFeedAsync(final String url, final FeedDiscoveryListener listener) {
        new AsyncTask<Void, Void, List<Feed>>() {
            @Override
            protected List<Feed> doInBackground(Void... params) {
                try {
                    return discoverFeeds(url);
                } catch (IOException e) {
                    Log.e(TAG, "error discovering feed", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<Feed> feeds) {
                super.onPostExecute(feeds);
                if (listener != null)
                    listener.onFeedDiscovery(feeds);
            }
        }.execute();
    }

    private static List<Feed> parse(InputStream in, final String url) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();

            final String startTag = parser.getName();
            BaseParser p;
            if ("rss".equals(startTag))
                p = new RSSParser(parser, url);
            else if ("feed".equals(startTag))
                p = new AtomParser(parser, url);
            else
                return new ArrayList<>();
            p.parse();
            return p.getFeeds();
        } finally {
            in.close();
        }
    }

    private static InputStream fetchUrl(final String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        return conn.getInputStream();
    }

    public static List<Feed> parseUrl(final String url) {
        try {
            return parse(new BufferedInputStream(fetchUrl(url)), url);
        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "error fetching feed", e);
        }

        return null;
    }

    public interface FeedParseListener {
        void onFeedParsed(final List<Feed> feeds);
    }


}
