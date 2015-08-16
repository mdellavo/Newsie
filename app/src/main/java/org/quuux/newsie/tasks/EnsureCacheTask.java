package org.quuux.newsie.tasks;

import android.os.AsyncTask;

import org.quuux.newsie.Log;
import org.quuux.newsie.data.CacheManager;

import java.io.File;

public class EnsureCacheTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = Log.buildTag(EnsureCacheTask.class);

    @Override
    protected Void doInBackground(Void... params) {

        final File root = CacheManager.getFeedsPath();

        if (!root.exists()) {
            final boolean rv = root.mkdirs();
            if (!rv)
                Log.e(TAG, "could not create root: %s", root);
        }

        return null;
    }
}
