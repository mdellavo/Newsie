package org.quuux.newsie.data;

import android.text.TextUtils;

import java.util.Date;

public class FeedItem {

    private String guid;
    private String title;
    private String description;
    private String url;
    private int ttl;
    private Date pubDate;
    private String content;
    private boolean read = false;

    private String buildKey() {
        final StringBuilder sb = new StringBuilder();
        String[] keys = {guid, url, description, title, content};
        for (String key : keys)
            if (!TextUtils.isEmpty(key))
                sb.append(key);
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return buildKey().hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        return (o instanceof FeedItem) && ((FeedItem)o).buildKey().equals(buildKey());
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
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

    public void setUrl(String link) {
        this.url = link;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDisplayContent(final String template) {
        final String content;
        if (!TextUtils.isEmpty(getContent()))
            content = getContent();
        else if (!TextUtils.isEmpty(getDescription()))
            content = getDescription();
        else
            content = "<em>(no content)</em>";

        return String.format(template, content);
    }

    public boolean isRead() {
        return read;
    }

    public void markRead() {
        read = true;
    }
}
