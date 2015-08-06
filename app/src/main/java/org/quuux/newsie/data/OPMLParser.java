package org.quuux.newsie.data;


import android.os.AsyncTask;
import android.util.Xml;

import org.quuux.newsie.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OPMLParser {


    private static final String TAG = Log.buildTag(OPMLParser.class);

    public static class Node {
        public String title, url;
        public List<Node> children = new ArrayList<>();
    }

    final static String ns = null;
    public  static List<Node> parse(final InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readOPML(parser);
        } finally {
            in.close();
        }
    }

    private static List<Node> readOPML(final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "opml");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if (name.equals("body")) {
                return readBody(parser);
            } else {
                skip(parser);
            }
        }

        return null;
    }

    private static List<Node> readBody(final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "body");

        final List<Node> nodes = new ArrayList<>();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if (name.equals("outline")) {
                final Node node = readOutline(parser);
                if (node != null)
                    nodes.add(node);
            } else {
                skip(parser);
            }
        }

        return nodes;
    }

    private static Node readOutline(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "outline");

        final Node node = new Node();
        node.url = parser.getAttributeValue(ns, "xmlUrl");
        node.title = parser.getAttributeValue(ns, "title");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if (name.equals("outline")) {
                final Node child = readOutline(parser);
                if (child != null)
                    node.children.add(child);
            } else {
                skip(parser);
            }
        }

        return node;
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

    public static void parseAsync(final File file, final ParseListener listener) {
        new ParseTask(listener).execute(file);
    }

    public interface ParseListener {
        void onParsed(List<Node> nodes);
    }

    static class ParseTask extends AsyncTask<File, Void, List<Node>> {

        private final ParseListener listener;

        public ParseTask(ParseListener listener) {
            this.listener = listener;
        }

        @Override
        protected List<Node> doInBackground(File... params) {

            List<OPMLParser.Node> nodes = null;
            try {
                nodes = OPMLParser.parse(new FileInputStream(params[0]));
                Log.d(TAG, "parsed %s nodes", nodes.size());
            } catch (XmlPullParserException | IOException e) {
                Log.e(TAG, "error parsing file: %s", e, params[0]);
            }

            return nodes;
        }

        @Override
        protected void onPostExecute(List<Node> nodes) {
            super.onPostExecute(nodes);
            if (listener != null)
                listener.onParsed(nodes);
        }
    }
}
