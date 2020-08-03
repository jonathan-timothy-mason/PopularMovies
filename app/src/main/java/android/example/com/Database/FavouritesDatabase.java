package android.example.com.Database;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import java.util.List;

/**
 * Favourites database; a singleton.
 */
@Database(entities = {Movie.class}, version = 1, exportSchema = false)
public abstract class FavouritesDatabase extends RoomDatabase {

    private final static String DATABASE_NAME = "Favourites";
    private final static Object LOCK = new Object();

    private static FavouritesDatabase mInstance = null;

    /**
     * Implement singleton, creating single instance of favourites database, if not created,
     * or simply retrieving previously created instance.
     * @param context Context within which to create database, if necessary.
     * @return Favourites database.
     */
    public static FavouritesDatabase getInstance(Context context) {
        if (mInstance == null) {
            synchronized (LOCK) {
                mInstance = Room.databaseBuilder(context.getApplicationContext(), FavouritesDatabase.class, FavouritesDatabase.DATABASE_NAME).build();
            }
        }

        return mInstance;
    }

    /**
     * Room DAO interface.
     */
    @Dao
    public interface FavouritesDAO {

        /**
         * Get all favourite movies.
         * @return List of favourite movies.
         */
        @Query("SELECT * FROM Movies ORDER BY Title")
        LiveData<List<Movie>> load();

        /**
         * Find out whether movie specified by id is a favourite, i.e. does it exist?
         * @param identifierToCheck ID of movie to check.
         * @return Whether movie is a favourite.
         */
        @Query("SELECT COUNT(identifier) FROM Movies WHERE identifier = :identifierToCheck")
        boolean isFavourite(int identifierToCheck);

        /**
         * Insert movie into favourites database.
         * @param favourite Movie to insert.
         */
        @Insert
        void insert(Movie favourite);

        /**
         * Delete movie from favourites database.
         * @param favourite Movie to delete.
         */
        @Delete
        void delete(Movie favourite);
    }

    /**
     * Get Room DAO instance.
     * @return Room DAO instance.
     */
    public abstract FavouritesDAO favouritesDAO();
}
