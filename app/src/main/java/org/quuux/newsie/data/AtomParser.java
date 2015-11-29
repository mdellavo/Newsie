package org.quuux.newsie.data;

import org.quuux.newsie.data.BaseParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class AtomParser extends BaseParser {
    public AtomParser(final XmlPullParser parser, final String url) {
        super(parser, url);
    }

    @Override
    public String getNamespace() {
        return null;
    }

    @Override
    public void parse() throws XmlPullParserException, IOException {
    }
}
