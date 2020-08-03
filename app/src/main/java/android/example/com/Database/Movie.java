package android.example.com.Database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Class represents a Movie DB API movie.
 */
@Entity(tableName = "Movies")
public class Movie {

    /**
     * Constructor.
     * @param identifier ID of movie; not auto-generated (using name of id prevented Room
     * from building implementation class).
     * @param title Title of movie.
     * @param imagePath Path to image of movie.
     * @param overview Summary of movie.
     * @param rating Average review rating of movie.
     * @param releaseYear Year movie released.
     */
    public Movie(int identifier, String title, String imagePath, String overview, double rating, int releaseYear)
    {
        mIdentifier = identifier;
        mTitle = title;
        mImagePath = imagePath;
        mOverview = overview;
        mRating = rating;
        mReleaseYear = releaseYear;
    }

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "Identifier")
    private int mIdentifier;
    /**
     * Get ID of movie.
     * @return ID of movie.
     */
    public int getIdentifier()
    {
        return mIdentifier;
    }

    @ColumnInfo(name = "Title")
    private String mTitle;
    /**
     * Get title of movie.
     * @return Title of movie.
     */
    public String getTitle()
    {
        return mTitle;
    }

    @ColumnInfo(name = "ImagePath")
    private String mImagePath;
    /**
     * Get path to image of movie.
     * @return Path to image of movie.
     */
    public String getImagePath()
    {
        return mImagePath;
    }

    @ColumnInfo(name = "Overview")
    private String mOverview;
    /**
     * Get summary of movie.
     * @return Summary of movie.
     */
    public String getOverview()
    {
        return mOverview;
    }

    @ColumnInfo(name = "Rating")
    private double mRating;
    /**
     * Get average review rating of movie.
     * @return Average review rating of movie.
     */
    public double getRating()
    {
        return mRating;
    }

    @ColumnInfo(name = "ReleaseYear")
    private int mReleaseYear;
    /**
     * Get year movie released.
     * @return Year movie released.
     */
    public int getReleaseYear()
    {
        return mReleaseYear;
    }
}
