package org.quuux.newsie.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DataStore {
    
    private static DataStore instance;

    private Map<String, Feed> feedMap = new HashMap<>();

    protected DataStore() {}

    public static DataStore getInstance() {
        if (instance == null)
            instance = new DataStore();
        return instance;
    }

    public List<Feed> getFeeds() {
        return new ArrayList<>(feedMap.values());
    }

    public Feed addFeed(final Feed feed) {
        feedMap.put(feed.getUrl(), feed);
        return feed;
    }

    public Feed getFeed(String feedUrl) {
        return feedMap.get(feedUrl);
    }
}
