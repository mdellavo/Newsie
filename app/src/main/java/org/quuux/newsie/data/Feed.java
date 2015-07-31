package org.quuux.newsie.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Feed {
    private String title;
    private String description;
    private String url;
    private int ttl;
    private Date pubDate;
    private List<FeedItem> items = new ArrayList<>();

    public Feed() {}

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
        items.add(item);
    }

    public List<FeedItem> getItems() {
        return items;
    }
}
