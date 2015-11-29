package org.quuux.newsie.data;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class RSSParser extends BaseParser {

    public RSSParser(final XmlPullParser parser, final String url) {
        super(parser, url);
    }

    public void parse() throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, getNamespace(), "rss");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("channel")) {
                final Feed feed = readChannel();
                feeds.add(feed);
            } else {
                skip();
            }
        }
    }

    public Feed readChannel() throws IOException, XmlPullParserException {

        final Feed feed = new Feed(url);

        parser.require(XmlPullParser.START_TAG, getNamespace(), "channel");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("title")) {
                final String title = readSimpleValue("title");
                feed.setTitle(title);
            } else if (name.equals("description")) {
                final String description = readSimpleValue("description");
                feed.setDescription(description);
            } else if (name.equals("link")) {
                final String link = readSimpleValue("link");
                feed.setLink(link);
            } else if (name.equals("item")) {
                final FeedItem item = readItem();
                feed.addItem(item);
            } else {
                skip();
            }
        }
        return feed;
    }

    public FeedItem readItem() throws IOException, XmlPullParserException {
        final FeedItem item = new FeedItem();

        parser.require(XmlPullParser.START_TAG, getNamespace(), "item");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("title")) {
                final String title = readSimpleValue("title");
                item.setTitle(title);
            } else if (name.equals("description")) {
                final String description = readSimpleValue("description");
                item.setDescription(description);
            } else if (name.equals("content:encoded")) {
                final String content = readSimpleValue("content:encoded");
                item.setContent(content);
            } else if (name.equals("link")) {
                final String url = readSimpleValue("link");
                item.setUrl(url);
            } else if (name.equals("guid")) {
                final String guid = readSimpleValue("guid");
                item.setGuid(guid);
            } else {
                skip();
            }
        }

        return item;
    }

    @Override
    public String getNamespace() {
        return null;
    }
}
