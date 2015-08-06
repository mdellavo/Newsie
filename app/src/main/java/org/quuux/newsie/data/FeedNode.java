package org.quuux.newsie.data;

import java.util.List;

public interface FeedNode {
    String getDisplayName();
    List<FeedNode> getFeeds();

    String getIconUrl();
}
