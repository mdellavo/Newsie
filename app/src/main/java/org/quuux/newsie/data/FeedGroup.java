package org.quuux.newsie.data;

import java.util.LinkedList;
import java.util.List;

public class FeedGroup implements FeedNode {

    final String name;
    final List<FeedNode> feeds = new LinkedList<>();

    public FeedGroup(final String name) {
        this.name = name;
    }

    @Override
    public String getDisplayName() {
            return name;
    }

    public List<FeedNode> getFeeds() {
        return feeds;
    }

    public void addFeed(FeedNode feed) {
        feeds.add(feed);
    }

}
