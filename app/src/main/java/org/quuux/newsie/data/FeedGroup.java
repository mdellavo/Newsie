package org.quuux.newsie.data;

import java.util.LinkedList;
import java.util.List;

public class FeedGroup extends Feed {
    List<Feed> feeds = new LinkedList<>();
    public List<Feed> getFeeds() {
        return feeds;
    }
}
