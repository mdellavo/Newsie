package org.quuux.newsie;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.squareup.otto.Subscribe;

import org.quuux.newsie.data.Feed;
import org.quuux.newsie.data.FeedCache;
import org.quuux.newsie.events.FeedsLoaded;
import org.quuux.newsie.ui.FeedFragment;
import org.quuux.newsie.ui.IndexFragment;
import org.quuux.newsie.ui.ProgressFragment;


public class MainActivity extends AppCompatActivity implements IndexFragment.Listener, FeedFragment.Listener {

    private DrawerLayout drawerLayout;
    private ListView drawerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.drawer);

        if (FeedCache.getInstance().hasRoot())
            showIndex();
        else
            showProgress();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getInstance().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getInstance().unregister(this);
    }

    @Subscribe
    public void onFeedsLoaded(final FeedsLoaded event) {
        showIndex();
    }

    private void showProgress() {
        final ProgressFragment frag = ProgressFragment.newInstance();
        fragReplace(frag, "progress", false);
    }

    private void showIndex() {
        final IndexFragment frag = IndexFragment.newInstance();
        fragReplace(frag, "index", false);
    }

    private void fragReplace(final Fragment frag, final String tag, final boolean addToBackStack) {
        final FragmentTransaction t = getFragmentManager().beginTransaction();
        t.replace(R.id.content, frag, tag);
        if (addToBackStack)
            t.addToBackStack(null);
        t.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0 ){
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void openFeed(Feed feed) {
        final FeedFragment frag = FeedFragment.newInstance(feed);
        fragReplace(frag, "feed-" + feed.getUrl(), true);
    }
}
