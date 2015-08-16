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

    public String getDisplayContent() {
        final StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("<html>");
        contentBuilder.append("<body>");

        if (!TextUtils.isEmpty(getContent()))
            contentBuilder.append(getContent());
        else if (!TextUtils.isEmpty(getDescription()))
            contentBuilder.append(getDescription());
        else
            contentBuilder.append("(no content)");

        contentBuilder.append("</body>");
        contentBuilder.append("</html>");

        return contentBuilder.toString();
    }
}
