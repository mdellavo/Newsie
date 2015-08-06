package org.quuux.newsie.ui;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.quuux.newsie.R;
import org.quuux.newsie.data.Feed;


public class IndexFragment extends Fragment implements FeedsAdapter.Listener {

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
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_index, container, false);

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
    public void onFeedClicked(Feed feed) {
        listener.openFeed(feed);
    }

    public interface Listener {
        void openFeed(Feed feed);
    }

}
