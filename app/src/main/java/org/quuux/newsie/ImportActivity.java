package org.quuux.newsie;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.quuux.newsie.data.Feed;
import org.quuux.newsie.data.FeedCache;
import org.quuux.newsie.data.FeedGroup;
import org.quuux.newsie.data.FeedNode;
import org.quuux.newsie.data.OPMLParser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImportActivity extends AppCompatActivity implements OPMLParser.ParseListener, View.OnClickListener {

    private static final String TAG = Log.buildTag(ImportActivity.class);
    private ListView listView;
    private Button submitButton;
    private Adapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        listView = (ListView)findViewById(R.id.list);
        submitButton = (Button) findViewById(R.id.submit);
        submitButton.setOnClickListener(this);

        final File file = new File(getIntent().getData().getPath());

        Log.d(TAG, "import: %s", file);
        OPMLParser.parseAsync(file, this);
   }

    @Override
    public void onParsed(List<OPMLParser.Node> nodes) {
        adapter = new Adapter(nodes);
        listView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submit:
                importFeeds(adapter.getImportedFeeds());
                break;
        }
    }

    private void importFeeds(List<Feed> importedFeeds) {
        for (Feed feed : importedFeeds) {
            FeedCache.getInstance().addFeed(feed);
        }
    }

    static class Holder {
        ImageView iconView;
        TextView titleView;
    }

    class Adapter extends BaseAdapter {

        private final List<FeedNode> nodes = new ArrayList<>();

        public Adapter(List<OPMLParser.Node> nodes) {
            populate(nodes);
        }

        private void populate(List<OPMLParser.Node> importedNodes) {
            nodes.clear();
            addNodes(importedNodes);
        }

        private void addNodes(List<OPMLParser.Node> importedNodes) {
            for (final OPMLParser.Node importedNode : importedNodes) {
                final boolean hasChildren = importedNode.children.size() > 0;
                final FeedNode feedNode = hasChildren ? new FeedGroup(importedNode.title) : new Feed(importedNode.url, importedNode.title);
                nodes.add(feedNode);
                if (hasChildren)
                    addNodes(importedNode.children);
            }
        }

        @Override
        public int getCount() {
            return nodes.size();
        }

        @Override
        public Object getItem(int position) {
            return nodes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return nodes.get(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View view = convertView != null ? convertView : newView(parent);
            bindView(view, (FeedNode) getItem(position));
            return view;
        }

        private void bindView(View view, FeedNode item) {
            final Holder holder = (Holder) view.getTag();
            holder.titleView.setText(item.getDisplayName());
            Picasso.with(holder.iconView.getContext()).load(item.getIconUrl()).into(holder.iconView);
        }

        public View newView(final ViewGroup parent) {
            final LayoutInflater inflater = LayoutInflater.from(ImportActivity.this);
            final View view = inflater.inflate(R.layout.imported_node_item, parent, false);
            final Holder holder = new Holder();
            holder.iconView = (ImageView) view.findViewById(R.id.icon);
            holder.titleView = (TextView) view.findViewById(R.id.title);
            view.setTag(holder);
            return view;
        }

        public List<Feed> getImportedFeeds() {
            final List<Feed> rv = new ArrayList<>();
            for (FeedNode node : nodes) {
                if (node instanceof Feed) {
                    rv.add((Feed) node);
                }
            }
            return rv;
        }
    }
}
