package org.quuux.newsie.data;


import android.os.AsyncTask;
import android.util.Xml;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.quuux.newsie.Log;
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

    final static String ns = null;
    private static List<Feed> parse(InputStream in, final String url) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser, url);
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

    private static List<Feed> readFeed(XmlPullParser parser, final String url) throws XmlPullParserException, IOException {

        final List<Feed> feeds = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, ns, "rss");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("channel")) {
                final Feed feed = readChannel(parser, url);
                feeds.add(feed);
            } else {
                skip(parser);
            }
        }
        return feeds;
    }

    private static Feed readChannel(XmlPullParser parser, final String url) throws IOException, XmlPullParserException {

        final Feed feed = new Feed(url);

        parser.require(XmlPullParser.START_TAG, ns, "channel");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("title")) {
                final String title = readSimpleValue(parser, "title");
                feed.setTitle(title);
            } else if (name.equals("description")) {
                final String description = readSimpleValue(parser, "description");
                feed.setDescription(description);
            } else if (name.equals("link")) {
                final String link = readSimpleValue(parser, "link");
                feed.setLink(link);
            } else if (name.equals("item")) {
                final FeedItem item = readItem(parser);
                feed.addItem(item);
            } else {
                skip(parser);
            }
        }
        return feed;
    }

    private static FeedItem readItem(XmlPullParser parser) throws IOException, XmlPullParserException {
        final FeedItem item = new FeedItem();

        parser.require(XmlPullParser.START_TAG, ns, "item");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            Log.d(TAG, "name: %s", name);
            if (name.equals("title")) {
                final String title = readSimpleValue(parser, "title");
                item.setTitle(title);
            } else if (name.equals("description")) {
                final String description = readSimpleValue(parser, "description");
                item.setDescription(description);
            } else if (name.equals("content:encoded")) {
                final String content = readSimpleValue(parser, "content:encoded");
                item.setContent(content);
            } else if (name.equals("link")) {
                final String url = readSimpleValue(parser, "link");
                item.setUrl(url);
            } else if (name.equals("guid")) {
                final String guid = readSimpleValue(parser, "guid");
                item.setGuid(guid);
            } else {
                skip(parser);
            }
        }

        return item;
    }

    private static String readSimpleValue(XmlPullParser parser, final String tag) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, tag);
        String rv = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, tag);
        return rv;
    }

    private static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        return parser.nextText();
    }

    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
