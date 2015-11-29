package org.quuux.newsie.data;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class AtomParser extends BaseParser {
    public AtomParser(final XmlPullParser parser, final String url) {
        super(parser, url);
    }

    @Override
    public void parse() throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, getNamespace(), "feed");
        final Feed feed = new Feed(url);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("title")) {
                final String title = readSimpleValue("title");
                feed.setTitle(title);
            } else if (name.equals("subtitle")) {
                final String subtitle = readSimpleValue("subtitle");
                feed.setTitle(subtitle);
            } else if (name.equals("entry")) {
                final FeedItem item = readEntry();
                feed.addItem(item);

            } else {
                skip();
            }
        }

        feeds.add(feed);
    }

    private FeedItem readEntry() throws IOException, XmlPullParserException  {

        final FeedItem item = new FeedItem();

        parser.require(XmlPullParser.START_TAG, getNamespace(), "entry");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("title")) {
                final String title = readSimpleValue("title");
                item.setTitle(title);
            } else if (name.equals("summary")) {
                final String description = readSimpleValue("summary");
                item.setDescription(description);
            } else if (name.equals("content")) {
                final String content = readSimpleValue("content");
                item.setContent(content);
            } else if (name.equals("link")) {
                final String url = readSimpleValue("link");
                item.setUrl(url);
            } else {
                skip();
            }
        }

        return item;


    }

}
