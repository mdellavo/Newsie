package org.quuux.newsie.data;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseParser {

    final XmlPullParser parser;
    final String url;

    final List<Feed> feeds = new ArrayList<>();

    public BaseParser(final XmlPullParser parser, final String url) {
        this.parser = parser;
        this.url = url;
    }

    public abstract String getNamespace();
    public abstract void parse() throws XmlPullParserException, IOException;

    public List<Feed> getFeeds() {
        return feeds;
    }

    public String readSimpleValue(final String tag) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, getNamespace(), tag);
        String rv = readText();
        parser.require(XmlPullParser.END_TAG, getNamespace(), tag);
        return rv;
    }

    public String readText() throws IOException, XmlPullParserException {
        return parser.nextText();
    }

    public void skip() throws XmlPullParserException, IOException {
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
