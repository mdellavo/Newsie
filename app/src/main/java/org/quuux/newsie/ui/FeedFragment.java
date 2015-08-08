package org.quuux.newsie.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import org.quuux.newsie.Log;
import org.quuux.newsie.R;
import org.quuux.newsie.data.FeedCache;
import org.quuux.newsie.data.Feed;
import org.quuux.newsie.data.FeedItem;

public class FeedFragment extends Fragment {
    private static final String TAG = Log.buildTag(FeedFragment.class);

    private static final String ARG_FEED_URL = "feed_url";

    private Feed feed;

    private Listener listener;
    private RecyclerView list;
    private LinearLayoutManager layoutManager;
    private Adapter adapter;

    public static FeedFragment newInstance(final Feed feed) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        args.putString(ARG_FEED_URL, feed.getUrl());
        return fragment;
    }

    public FeedFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            final String feedUrl = getArguments().getString(ARG_FEED_URL);
            feed = FeedCache.getInstance().getFeed(feedUrl);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_feed, container, false);

        list = (RecyclerView) view.findViewById(R.id.list);
        layoutManager = new LinearLayoutManager(getActivity());
        list.setLayoutManager(layoutManager);

        adapter = new Adapter(feed);
        list.setAdapter(adapter);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (Listener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement Listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface Listener {
    }

    static class FeedItemViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        WebView content;

        @SuppressLint("SetJavaScriptEnabled")
        public FeedItemViewHolder(View itemView) {
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.title);
            content = (WebView)itemView.findViewById(R.id.content);

            final WebSettings settings = content.getSettings();
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);

            settings.setLoadsImagesAutomatically(true);

            CookieManager.getInstance().setAcceptCookie(true);

            settings.setJavaScriptEnabled(true);

            content.setBackgroundColor(content.getResources().getColor(android.R.color.white));
        }
    }

    static class Adapter extends RecyclerView.Adapter<FeedItemViewHolder> {

        final Feed feed;
        public Adapter(final Feed feed) {
            this.feed = feed;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public FeedItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            final View view = inflater.inflate(R.layout.feed_item_item, parent, false);
            return new FeedItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(FeedItemViewHolder holder, int position) {
            final FeedItem item = feed.getItems().get(position);
            holder.title.setText(item.getTitle());
            final String mime = "text/html";
            final String encoding = "utf-8";

            final StringBuilder contentBuilder = new StringBuilder();
            contentBuilder.append("<html>");
            contentBuilder.append("<body>");

            if (!TextUtils.isEmpty(item.getContent()))
                contentBuilder.append(item.getContent());
            else if (!TextUtils.isEmpty(item.getDescription()))
                contentBuilder.append(item.getDescription());
            else
                contentBuilder.append("(no content)");

            contentBuilder.append("</body>");
            contentBuilder.append("</html>");

            final String content = contentBuilder.toString();
            Log.d(TAG, "content= %s", content);

            holder.content.loadDataWithBaseURL(null, content, mime, encoding, null);
        }

        @Override
        public int getItemCount() {
            return feed.getItems().size();
        }
    }

}
