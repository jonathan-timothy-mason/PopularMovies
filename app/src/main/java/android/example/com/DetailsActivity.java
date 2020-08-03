package android.example.com;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.example.com.Database.FavouritesDatabase;
import android.example.com.Database.Movie;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Screen displays details of a particular movie.
 */
public class DetailsActivity extends AppCompatActivity implements VideosAdapter.VideoSelectionListener, LoaderManager.LoaderCallbacks<String> {

    // Intent extra data names.
    public final static String IDENTIFIER = "IDENTIFIER";
    public final static String TITLE = "TITLE";
    public final static String IMAGE_PATH = "IMAGE_PATH";
    public final static String IMAGE_WIDTH = "IMAGE_WIDTH";
    public final static String IMAGE_HEIGHT = "IMAGE_HEIGHT";
    public final static String OVERVIEW = "OVERVIEW";
    public final static String RATING = "RATING";
    public final static String RELEASE_YEAR = "RELEASE_YEAR";
    public final static String FAVOURITE = "FAVOURITE";

    private TextView mTitleTextView;
    private ImageView mImageView;
    private TextView mOverviewTextView;
    private TextView mRatingTextView;
    private TextView mReleaseYearTextView;
    private RecyclerView mVideosRecyclerView;
    private TextView mReviewsTextView;
    private ImageButton mFavouritesImageButton;

    private Movie mMovie;
    private boolean mFavourite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mFavourite = false;

        // Retrieve controls for population.
        mTitleTextView = this.findViewById(R.id.details_title);
        mImageView = this.findViewById(R.id.details_image);
        mOverviewTextView = this.findViewById(R.id.details_overview);
        mRatingTextView = this.findViewById(R.id.details_rating);
        mReleaseYearTextView = this.findViewById(R.id.details_release_year);
        mVideosRecyclerView = this.findViewById(R.id.video_recycler_view);
        mReviewsTextView = this.findViewById(R.id.reviews);
        mFavouritesImageButton = this.findViewById(R.id.favourites_button);

        // Set up to use GridLayoutManager (videos adapter created after videos
        // have loaded).
        mVideosRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Retrieve data passed using intent and populate controls.
        this.populateControlsFromIntent();

        // First time activity is created, check if movie is a favourite by seeing if it exists
        // in favourites database. After that, it is to be persisted as instance state.
        if((savedInstanceState == null) || (!savedInstanceState.containsKey(FAVOURITE))) {
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                /**
                 * Run database query on separate thread.
                 */
                @Override
                public void run() {
                    mFavourite = FavouritesDatabase.getInstance(getApplicationContext()).favouritesDAO().isFavourite(mMovie.getIdentifier());

                    runOnUiThread(new Runnable() {
                        /**
                         * Update favourites button on UI thread.
                         */
                        @Override
                        public void run() {
                            updateFavouritesButton();
                        }
                    });
                }
            });
        }
        else {
            mFavourite = savedInstanceState.getBoolean(FAVOURITE); // Retrieve from instance state.
        }

        // Setup loaders to load any videos and reviews about the current movie.
        this.setupLoaders();
    }

    /**
     * Retrieve data passed using intent and populate controls.
     */
    private void populateControlsFromIntent()
    {
        Intent intent = this.getIntent();

        int identifier = -1;
        if(intent.hasExtra(IDENTIFIER))
            identifier = intent.getIntExtra(IDENTIFIER, identifier);

        String title = "";
        if(intent.hasExtra(TITLE)) {
            title = intent.getStringExtra(TITLE);
            if ((title != null) && (title.length() > 0))
                mTitleTextView.setText(title);
        }

        String imagePath = "";
        if(intent.hasExtra(IMAGE_PATH) && intent.hasExtra(IMAGE_WIDTH) && intent.hasExtra(IMAGE_HEIGHT)) {
            imagePath = intent.getStringExtra(IMAGE_PATH);
            int imageWidth = intent.getIntExtra(IMAGE_WIDTH, Integer.MIN_VALUE);
            int imageHeight = intent.getIntExtra(IMAGE_HEIGHT, Integer.MIN_VALUE);
            if((imageWidth != Integer.MIN_VALUE) && (imageHeight != Integer.MIN_VALUE))
                Picasso.get().load(intent.getStringExtra(IMAGE_PATH)).resize(imageWidth, imageHeight).into(mImageView);
        }

        String overview = "";
        if(intent.hasExtra(OVERVIEW)) {
            overview = intent.getStringExtra(OVERVIEW);
            if ((overview != null) && (overview.length() > 0))
                mOverviewTextView.setText(overview);
        }

        Double rating = Double.NaN;
        if(intent.hasExtra(RATING)) {
            rating = intent.getDoubleExtra(RATING, rating);
            if(!rating.isNaN()) {
                mRatingTextView.setText(new DecimalFormat("#.#").format(rating));
                mRatingTextView.append(this.getResources().getString(R.string.max_rating_suffix));
            }
        }

        int releaseYear = Integer.MIN_VALUE;
        if(intent.hasExtra(RELEASE_YEAR)) {
            releaseYear = intent.getIntExtra(RELEASE_YEAR, Integer.MIN_VALUE);
            if(releaseYear != Integer.MIN_VALUE)
                mReleaseYearTextView.setText(String.valueOf(releaseYear));
        }

        mMovie = new Movie(identifier, title, imagePath, overview, rating, releaseYear);
    }

    /**
     * Setup loaders to load any videos and reviews about the current movie using the
     * Movie DB API.
     */
    private void setupLoaders()
    {
        // Pass URLs to loader.
        Bundle videoArgs = new Bundle();
        videoArgs.putString(MoviesAsyncTaskLoader.MOVIE_URL_EXTRA, Utils.CreateGetVideosURL(mMovie.getIdentifier()).toString());
        Bundle reviewsArgs = new Bundle();
        reviewsArgs.putString(MoviesAsyncTaskLoader.MOVIE_URL_EXTRA, Utils.CreateGetReviewsURL(mMovie.getIdentifier()).toString());

        // There are two loaders, one for the videos and one for the reviews.
        // Each loads from the Movie DB API once and then works as a cache.
        LoaderManager.getInstance(this).initLoader(Utils.MOVIEDB_VIDEOS_LOADER_ID, videoArgs, this);
        LoaderManager.getInstance(this).initLoader(Utils.MOVIEDB_REVIEWS_LOADER_ID, reviewsArgs, this);
    }

    /**
     * Handle button press to toggle whether movie s a favourite.
     * @param view Favourites button.
     */
    public void toggleFavourite(View view) {
        mFavourite = !mFavourite;
        this.updateFavouritesButton();
    }

    /**
     * Updated image of button to reflect whether movie is a favourite.
     */
    private void updateFavouritesButton() {
        if(mFavourite)
            mFavouritesImageButton.setImageResource(R.drawable.ic_favorite);
        else
            mFavouritesImageButton.setImageResource(R.drawable.ic_not_favorite);
    }

    /*************************************
     * Implement VideoSelectionListener. *
     *************************************/

    /**
     * Handle selection of video to launch it in a separate app.
     * @param selectedVideo Selected video.
     */
    public void onVideoSelected(Video selectedVideo)
    {
        Uri uri = Utils.CreateYouTubeURI(selectedVideo.getKey());

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        // Make sure device supports playing video.
        if(intent.resolveActivity(getPackageManager()) != null)
            startActivity(intent);
    }

    /********************************************
     * Implement LoaderManager.LoaderCallbacks. *
     ********************************************/

    /**
     * Create loader for querying of Movie DB API, www.themoviedb.org, using a background thread.
     * @param id ID of loader.
     * @param args Arguments passed into loader, i.e. URL of Movie DB API.
     * @return New instance of loader.
     */
    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable final Bundle args) {
        return new MoviesAsyncTaskLoader(this, args);
    }

    /**
     * Query complete: apply resulting JSON vidoes and reviews data to main user
     * interface thread.
     * <p>Run on main user interface thread.</p>
     * @param loader Instance of loader.
     * @param data Resulting JSON movies data.
     */
    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        if(loader.getId() == Utils.MOVIEDB_VIDEOS_LOADER_ID) {
            // If no data was loaded, tell user and abort.
            if ((data == null) || (data.length() <= 0)) {
                Toast.makeText(DetailsActivity.this, DetailsActivity.this.getString(R.string.err_no_video_data), Toast.LENGTH_SHORT).show();
                return;
            }

            // Convert JSON to list of videos.
            ArrayList<Video> videos = Utils.parseVideosJson(data);

            // If no data was loaded, tell user and abort.
            if (videos.size() <= 0) {
                Toast.makeText(DetailsActivity.this, DetailsActivity.this.getString(R.string.warn_no_videos), Toast.LENGTH_SHORT).show();
                return;
            }

            // Create videos adapter (set after videos loaded to begin displaying).
            VideosAdapter videosAdapter = new VideosAdapter(DetailsActivity.this);

            // Copy loaded videos to adapter for RecyclerView.
            videosAdapter.getVideos().addAll(videos);

            // Set adapter of RecycleView (this causes it to update itself).
            mVideosRecyclerView.setAdapter(videosAdapter);
        }
        else { // MOVIEDB_REVIEWS_LOADER_ID
            // If no data was loaded, tell user and abort.
            if ((data == null) || (data.length() <= 0)) {
                Toast.makeText(DetailsActivity.this, DetailsActivity.this.getString(R.string.err_no_review_data), Toast.LENGTH_SHORT).show();
                return;
            }

            // Convert JSON to list of reviews.
            ArrayList<Review> reviews = Utils.parseReviewsJson(data);

            // If no data was loaded, tell user and abort.
            if (reviews.size() <= 0) {
                Toast.makeText(DetailsActivity.this, DetailsActivity.this.getString(R.string.warn_no_reviews), Toast.LENGTH_SHORT).show();
                return;
            }

            // Populate review TextView.
            String allReviewsText = "";
            String authorDecorator = getString(R.string.author_decorator);
            for(Review r: reviews) {
                allReviewsText += r.getContent() + "\n\n" + authorDecorator + " " + r.getAuthor() + " " + authorDecorator + "\n\n\n";
            }
            mReviewsTextView.setText(allReviewsText);
        }
    }

    /**
     * Implemented, but not used.
     */
    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) { /* Do nothing. */ }

    /**
     * Save whether movie is a favourite to instance state to save having to requery database.
     * @param outState Bundle containing instance state.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(FAVOURITE, mFavourite);
    }

    /**
     * Save whether movie is a favourite to database when the activity is closing.
     */
    @Override
    protected void onPause() {
        super.onPause();

        // Thanks to Adil Soomro's answer to "How to know activity has been finished?",
        // https://stackoverflow.com/questions/8756938/how-to-know-activity-has-been-finished.
        if(this.isFinishing()) {
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                /**
                 * Run database query on separate thread.
                 */
                @Override
                public void run() {
                    // Check whether movie is a favourite as far as database is concerned.
                    boolean isFavourite = FavouritesDatabase.getInstance(getApplicationContext()).favouritesDAO().isFavourite(mMovie.getIdentifier());

                    // If state of button and database are different, insert or delete movie
                    // accordingly.
                    if(isFavourite != mFavourite) {
                        if(mFavourite)
                            FavouritesDatabase.getInstance(getApplicationContext()).favouritesDAO().insert(mMovie);
                        else
                            FavouritesDatabase.getInstance(getApplicationContext()).favouritesDAO().delete(mMovie);
                    }
                }
            });
        }
    }
}
