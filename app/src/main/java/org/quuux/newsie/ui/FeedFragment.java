package org.quuux.newsie.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
    private VerticalViewPager list;
    private Adapter adapter;

    private SparseArray<WebView> webviews = new SparseArray<>();

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
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            final String feedUrl = getArguments().getString(ARG_FEED_URL);
            feed = FeedCache.getInstance().getFeed(feedUrl);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_feed, container, false);

        list = (VerticalViewPager) view.findViewById(R.id.list);

        adapter = new Adapter(feed);
        list.setAdapter(adapter);
        list.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                list.attachWebView(webviews.get(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

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

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_feed, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        final boolean rv;
        switch (item.getItemId()) {

            case R.id.action_mark_all_as_read:
                FeedCache.getInstance().markFeedAsRead(feed);
                rv = true;
                break;

            default:
                rv = super.onOptionsItemSelected(item);
        }

        return rv;
    }


    public interface Listener {
    }

    static class FeedItemViewHolder {

        FeedItem item;

        View itemView;
        TextView title;
        WebView content;

        @SuppressLint("SetJavaScriptEnabled")
        public FeedItemViewHolder(final FeedItem item, final View itemView) {

            this.item = item;

            this.itemView = itemView;
            title = (TextView)itemView.findViewById(R.id.title);
            content = (WebView)itemView.findViewById(R.id.content);

            final WebSettings settings = content.getSettings();
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);

            settings.setLoadsImagesAutomatically(true);
            settings.setLoadWithOverviewMode(true);
            settings.setUseWideViewPort(true);
            settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

            settings.setSupportZoom(true);
            settings.setBuiltInZoomControls(true);
            settings.setDisplayZoomControls(false);

            CookieManager.getInstance().setAcceptCookie(true);

            settings.setJavaScriptEnabled(true);

            content.setBackgroundColor(content.getResources().getColor(android.R.color.white));
            content.onResume();
        }

        void bind() {
            title.setText(item.getTitle());
            final String mime = "text/html";
            final String encoding = "utf-8";
            final String template = title.getContext().getString(R.string.feed_item_boilerplate);
            content.loadDataWithBaseURL(null, item.getDisplayContent(template), mime, encoding, null);
        }
    }

    class Adapter extends PagerAdapter {

        final Feed feed;

        public Adapter(final Feed feed) {
            this.feed = feed;
        }

        @Override
        public Object instantiateItem(final ViewGroup parent, final int position) {
            final FeedItem item = this.feed.getItems().get(position);
            Log.d(TAG, "instantiate position=%s / item=%s", position, item);

            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            final View view = inflater.inflate(R.layout.feed_item_item, null);
            final FeedItemViewHolder holder = new FeedItemViewHolder(item, view);
            view.setTag(holder);
            holder.bind();

            parent.addView(view);

            webviews.put(position, holder.content);
            if (position == 0)
                list.attachWebView(webviews.get(0));

            if (!item.isRead()) {
                item.markRead();
                FeedCache.getInstance().commitFeed(feed);
            }

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
            webviews.delete(position);
        }

        @Override
        public int getCount() {
            return feed.getItems().size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }

}
