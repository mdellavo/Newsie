package org.quuux.newsie.ui;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import org.quuux.newsie.EventBus;
import org.quuux.newsie.R;
import org.quuux.newsie.data.Feed;
import org.quuux.newsie.data.FeedCache;
import org.quuux.newsie.events.FeedUpdated;
import org.quuux.newsie.events.FeedsUpdated;
import org.quuux.newsie.events.FeedsUpdating;


public class IndexFragment extends Fragment implements FeedsAdapter.Listener, SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout ptr;
    private RecyclerView list;
    private Listener listener;
    private LinearLayoutManager layoutManager;
    private FeedsAdapter adapter;

    public static IndexFragment newInstance() {
        IndexFragment fragment = new IndexFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public IndexFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_index, container, false);

        ptr = (SwipeRefreshLayout)view.findViewById(R.id.ptr);
        ptr.setOnRefreshListener(this);

        list = (RecyclerView)view.findViewById(R.id.list);

        list.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        list.setLayoutManager(layoutManager);

        adapter = new FeedsAdapter();
        adapter.setListener(this);
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

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getInstance().register(this);

        ptr.setRefreshing(FeedCache.getInstance().isUpdating());
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getInstance().unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_index, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        final boolean rv;
        switch (item.getItemId()) {

            case R.id.action_mark_all_as_read:
                FeedCache.getInstance().markAllFeedsAsRead();
                rv = true;
                break;

            default:
                rv = super.onOptionsItemSelected(item);
        }

        return rv;
    }

    @Override
    public void onFeedClicked(Feed feed) {
        listener.openFeed(feed);
    }

    @Override
    public void onRefresh() {
        FeedCache.getInstance().updateFeeds();
    }

    @Subscribe
    public void onFeedUpdated(final FeedUpdated event) {
        adapter.update();
    }

    @Subscribe
    public void onFeedsUpdated(final FeedsUpdated event) {
        ptr.setRefreshing(false);
        adapter.update();
    }

    @Subscribe
    public void onFeedsUpdating(final FeedsUpdating event) {
        ptr.setRefreshing(true);
    }

    public interface Listener {
        void openFeed(Feed feed);
    }

}
