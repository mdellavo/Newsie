package org.quuux.newsie.events;


import org.quuux.newsie.data.Feed;

public class FeedUpdated {
    public Feed feed;
    public FeedUpdated(final Feed feed) {
        this.feed = feed;
    }
}
