package android.example.com.Database;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

/**
 * ViewModel to persist movies.
 */
public class MoviesViewModel extends AndroidViewModel {

    /**
     * Constructor.
     * @param application Popular Movies app.
     */
    public MoviesViewModel(Application application) {
        super(application);

        mFavouriteMovies = FavouritesDatabase.getInstance(getApplication()).favouritesDAO().load();
    }

    private LiveData<List<Movie>> mFavouriteMovies;
    /**
     * Get favourite movies.
     * @return Favourite movies.
     */
    public LiveData<List<Movie>> getFavouriteMovies() {
        return mFavouriteMovies;
    }
}
