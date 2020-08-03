package android.example.com;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Loader subclass for querying of Movie DB API, www.themoviedb.org, using a background thread.
 */
public class MoviesAsyncTaskLoader extends AsyncTaskLoader<String> {

    public static final String MOVIE_URL_EXTRA = "MOVIE_URL_EXTRA";

    private Bundle mArgs;

    /**
     * Constructor.
     * @param context Context within which loader is to run.
     * @param args Arguments passed into loader, i.e. URL of Movie DB API.
     */
    public MoviesAsyncTaskLoader(Context context, Bundle args) {
        super(context);

        mArgs = args;
    }

    private String mCachedData = null;
    /**
     * Cached JSON data of last query.
     * @return Cached data or null, if cache is empty.
     */
    public String getCachedData() {
        return mCachedData;
    }

    /**
     * About to run query.
     * <p>Run on main user interface thread.</p>
     */
    @Override
    protected void onStartLoading() {
        super.onStartLoading();

        if(mArgs == null)
            return;
        if(!mArgs.containsKey(MOVIE_URL_EXTRA))
            return;
        String url = mArgs.getString(MOVIE_URL_EXTRA);
        if((url == null) || (url.length() <= 0))
            return;

        // If there is data in the cache just return it to save re-querying.
        if(mCachedData == null)
            this.onForceLoad();
        else
            deliverResult(mCachedData);
    }

    /**
     * Override to cache loaded JSON data.
     * @param data Loaded JSON data.
     */
    @Override
    public void deliverResult(@Nullable String data) {
        mCachedData = data;
        super.deliverResult(data);
    }

    /**
     * Execute URL for Movie DB API.
     * <p>Run on background thread.</p>
     * @return Resulting JSON movies data.
     */
    @Override
    public String loadInBackground() {
        if(Utils.isOnline()) {
            try {
                URL moviesURL = new URL(mArgs.getString(MOVIE_URL_EXTRA));
                HttpURLConnection connection = (HttpURLConnection)moviesURL.openConnection();
                try {
                    InputStream stream = connection.getInputStream();
                    try {
                        // Read stream with Scanner using \A to read entire stream in one go,
                        // automatically handling buffering and UTF-8 to UTF-16 conversion.
                        Scanner scanner = new Scanner(stream);
                        scanner.useDelimiter("\\A");
                        if (scanner.hasNext()) {
                            return scanner.next();
                        }
                    } finally {
                        stream.close();
                    }
                } finally {
                    connection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
};