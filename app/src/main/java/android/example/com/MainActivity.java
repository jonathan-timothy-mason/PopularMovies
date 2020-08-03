package android.example.com;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.content.res.Configuration;
import android.example.com.Database.Movie;
import android.example.com.Database.MoviesViewModel;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static androidx.lifecycle.Lifecycle.State.RESUMED;

/**
 * Main screen of app, displaying images of movies.
 */
public class MainActivity extends AppCompatActivity implements MovieImagesAdapter.MovieSelectionListener, LoaderManager.LoaderCallbacks<String> {

    private RecyclerView mMovieImagesRecyclerView;
    private Spinner mSpinner;

    /**
     * Perform initialisation of movies and RecyclerView with creation of activity.
     * @param savedInstanceState Saved state of app; not used.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve main controls.
        mMovieImagesRecyclerView = this.findViewById(R.id.movie_images_recycler_view);
        mSpinner = (Spinner)findViewById(R.id.sort_by_spinner);

        // Set up to use GridLayoutManager (movies adapter created after movies
        // have loaded).
        int numberColumns = 2;
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            numberColumns = 3;
        mMovieImagesRecyclerView.setLayoutManager(new GridLayoutManager(this, numberColumns));

        // Setup spinner to load movies on selection change (a selection change also
        // occurs when the activity is first created or recreated).
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /**
             * Handle selection change to reload movies.
             * @param pos Index of entry in spinner: 0 corresponds to most popular, 1 to highest rated,
             * 2 to favourites.
             */
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                // Prevent spinner selection changes when activity is not active. Its initial selection
                // is applied when activity is active in onResume.
                // Thanks to malinjir's answer to "Android: how do I check if activity is running?",
                // https://stackoverflow.com/questions/5446565/android-how-do-i-check-if-activity-is-running.
                if(getLifecycle().getCurrentState().isAtLeast(RESUMED))
                    applySpinnerSelection(pos);
            }

            /**
             * Implemented, but not used.
             */
            public void onNothingSelected(AdapterView<?> parent) { /* Do nothing. */}
        });
    }

    /**
     * Apply initial selection of spinner when activity is active.
     */
    @Override
    protected void onResume() {
        super.onResume();

        this.applySpinnerSelection(mSpinner.getSelectedItemPosition());
    }

    /**
     * Apply selection of spinner.
     * @param pos Index of spinner item.
     */
    private void applySpinnerSelection(int pos)
    {
        // Clear RecyclerView, in case there are no movies this time.
        this.clearRecyclerView();

        switch (pos) {
            default:
            case 0:
                MainActivity.this.loadMovies(true); // Load data from Movie DB API or cache.
                break;
            case 1:
                MainActivity.this.loadMovies(false); // Load data from Movie DB API or cache.
                break;
            case 2:
                MainActivity.this.setupViewModel(); // Load data from favourites database.
                break;
        }
    }

    /**
     * Load movies from Movie DB API.
     * @param mostPopular Whether to load most popular or highest rated movies.
     */
    public void loadMovies(boolean mostPopular)
    {
        // Pass URL to loader.
        Bundle args = new Bundle();
        args.putString(MoviesAsyncTaskLoader.MOVIE_URL_EXTRA, Utils.CreateDiscoverMoviesURL(mostPopular).toString());

        // There are two loaders, one for the most popular movies, and one for the highest rated.
        // Each loads from the Movie DB API once and then works as a cache.
        if(mostPopular)
            LoaderManager.getInstance(this).initLoader(Utils.MOVIEDB_MOST_POPULAR_LOADER_ID, args, this);
        else
            LoaderManager.getInstance(this).initLoader(Utils.MOVIEDB_HIGHEST_RATED_LOADER_ID, args, this);
    }

    /**
     * Setup up ViewModel to load and cache movies on separate thread for lifetime of app.
     */
    private void setupViewModel() {
        MoviesViewModel viewModel = new ViewModelProvider(this).get(MoviesViewModel.class);
        viewModel.getFavouriteMovies().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(@Nullable List<Movie> movies) {

                if(mSpinner.getSelectedItemPosition() == 2) {
                    // If there are no movies tell user, but continue so that RecyclerView is
                    // cleared to reflect that their are no favourites.
                    if (movies.size() <= 0)
                        Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.err_no_favourites), Toast.LENGTH_SHORT).show();

                    // Update RecyclerView.
                    MainActivity.this.setRecyclerViewAdapter(movies);
                }
            }
        });
    }

    /*************************************
     * Implement MovieSelectionListener. *
     *************************************/

    /**
     * Handle selection of movie to display details of selected movie.
     * @param selectedMovie Selected movie.
     * @param imageWidth Width, to which image has been resized.
     * @param imageHeight Height, to which image has been resized.
     */
    public void onMovieSelected(Movie selectedMovie, int imageWidth, int imageHeight)
    {
        Intent intent = new Intent(this, DetailsActivity.class);

        // Pass data of selected movie as extra data in intent.
        intent.putExtra(DetailsActivity.IDENTIFIER, selectedMovie.getIdentifier());
        intent.putExtra(DetailsActivity.TITLE, selectedMovie.getTitle());
        intent.putExtra(DetailsActivity.IMAGE_PATH, selectedMovie.getImagePath());
        intent.putExtra(DetailsActivity.IMAGE_WIDTH, imageWidth);
        intent.putExtra(DetailsActivity.IMAGE_HEIGHT, imageHeight);
        intent.putExtra(DetailsActivity.OVERVIEW, selectedMovie.getOverview());
        intent.putExtra(DetailsActivity.RATING, selectedMovie.getRating());
        intent.putExtra(DetailsActivity.RELEASE_YEAR, selectedMovie.getReleaseYear());

        // Show movie details screen.
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
     * Query complete: apply resulting JSON movies data to main user
     * interface thread.
     * <p>Run on main user interface thread.</p>
     * @param loader Instance of loader.
     * @param data Resulting JSON movies data.
     */
    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        // If no data was loaded, tell user and abort.
        if((data == null) || (data.length() <= 0)) {
            Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.err_no_movie_data), Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert JSON to list of movies.
        ArrayList<Movie> movies = Utils.parseMoviesJson(data);

        // If no data was loaded, tell user and abort.
        if(movies.size() <= 0) {
            Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.err_no_movies), Toast.LENGTH_SHORT).show();
            return;
        }

        this.setRecyclerViewAdapter(movies);
    }

    /**
     * Implemented, but not used.
     */
    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) { /* Do nothing. */ }

    /**
     * Clear RecyclerView.
     */
    private void clearRecyclerView() {
        this.setRecyclerViewAdapter(new ArrayList<Movie>());
    }

    /**
     * Add moview to adapter and then to RecyclerView.
     * @param movies Movies to add.
     */
    private void setRecyclerViewAdapter(List<Movie> movies) {
        // Determine size to which movies adapter should resize images to fill screen width.
        Rect sizeOfScreen = new Rect();
        getWindowManager().getDefaultDisplay().getRectSize(sizeOfScreen);
        int numColumns = ((GridLayoutManager)mMovieImagesRecyclerView.getLayoutManager()).getSpanCount();
        int width = sizeOfScreen.width() / numColumns;
        int height = (int)(width * 1.5); // Maintain aspect ratio of 2 x 3.

        // Create movies adapter (set after movies loaded to begin displaying).
        MovieImagesAdapter movieImageAdapter = new MovieImagesAdapter(width, height, MainActivity.this);

        // Copy loaded movies to adapter for RecyclerView.
        movieImageAdapter.getMovies().addAll(movies);

        // Set adapter of RecycleView (this causes it to update itself).
        mMovieImagesRecyclerView.setAdapter(movieImageAdapter);
    }
}
