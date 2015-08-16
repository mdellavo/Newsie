package org.quuux.newsie.data;

import android.net.Uri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Feed implements FeedNode {
    private String title;
    private String description;
    private String url;
    private String link;
    private int ttl;
    private Date pubDate;
    private List<FeedItem> items = new ArrayList<>();

    public Feed(final String url, final String title) {
        this.url = url;
        this.title = title;
    }

    public Feed(final String url) {
        this(url, null);
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public Date getPubDate() {
        return pubDate;
    }

    public void setPubDate(Date pubDate) {
        this.pubDate = pubDate;
    }

    public void addItem(FeedItem item) {
        if (!items.contains(item))
            items.add(item);
    }

    public List<FeedItem> getItems() {
        return items;
    }

    @Override
    public String getDisplayName() {
        return getTitle();
    }

    @Override
    public List<FeedNode> getFeeds() {
        return Collections.emptyList();
    }

    @Override
    public String getIconUrl() {
        final String url = getUrl();
        if (url == null)
            return null;
        final Uri uri = Uri.parse(url);
        return new Uri.Builder().scheme("http").authority(uri.getHost()).appendPath("favicon.ico").toString();
    }

    public void refresh(Feed other) {
        this.title = other.title;
        this.description = other.description;
        this.ttl = other.ttl;
        this.pubDate = other.pubDate;
        addItems(other.items);
    }

    private void addItems(final List<FeedItem> items) {
        for (FeedItem item : items)
            addItem(item);
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getUnreadCount() {
        int rv = 0;
        for (FeedItem item : items) {
            if (!item.isRead())
                rv++;
        }
        return rv;
    }
}
