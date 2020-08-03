package android.example.com;

import android.example.com.Database.Movie;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static java.util.Calendar.YEAR;

/**
 * General class containing utilities for the app.
 */
public class Utils {
    // Loader IDs.
    public static final int MOVIEDB_MOST_POPULAR_LOADER_ID = 1;
    public static final int MOVIEDB_HIGHEST_RATED_LOADER_ID = 2;
    public static final int MOVIEDB_VIDEOS_LOADER_ID = 3;
    public static final int MOVIEDB_REVIEWS_LOADER_ID = 4;

    // Movie DB API: common URL.
    private final static String MOVIESDB_COMMON_BASE_URL = "http://api.themoviedb.org/3";
    private final static String MOVIESDB_COMMON_API_KEY_PARAM = "api_key";
    private final static String MOVIESDB_COMMON_API_KEY = "REMOVED";
    private final static String MOVIESDB_COMMON_BASE_IMAGE_URL = "http://image.tmdb.org/t/p/w185/";

    // Movie DB API: discover/movie URL.
    private final static String MOVIESDB_DISCOVER_MOVIE_DISCOVER_URL = "discover";
    private final static String MOVIESDB_DISCOVER_MOVIE_MOVIE_URL = "movie";
    private final static String MOVIESDB_DISCOVER_MOVIE_SORT_PARAM = "sort_by";
    private final static String MOVIESDB_DISCOVER_MOVIE_SORT_POPULARITY = "popularity.desc";
    private final static String MOVIESDB_DISCOVER_MOVIE_SORT_RATING = "vote_average.desc";

    // Movie DB API: discover/movie data names.
    private final static String MOVIESDB_DISCOVER_MOVIE_RESULTS = "results";
    private final static String MOVIESDB_DISCOVER_MOVIE_POSTER_PATH = "poster_path";
    private final static String MOVIESDB_DISCOVER_MOVIE_ID = "id";
    private final static String MOVIESDB_DISCOVER_MOVIE_ORIGINAL_TITLE = "original_title";
    private final static String MOVIESDB_DISCOVER_MOVIE_VOTE_AVERAGE = "vote_average";
    private final static String MOVIESDB_DISCOVER_MOVIE_OVERVIEW = "overview";
    private final static String MOVIESDB_DISCOVER_MOVIE_RELEASE_DATE = "release_date";

    /**
     * Create URL to discover movies using Movie DB API.
     * <p>
     * For example:
     * http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=54b4cb8c55da282ebcfe11fa9a735a40
     * http://api.themoviedb.org/3/discover/movie?sort_by=vote_average.desc&api_key=54b4cb8c55da282ebcfe11fa9a735a40
     * </p>
     * @param mostPopular Whether to display most popular (true) or highest rated (false) movies.
     * @return The created URL or null if there was a problem.
     */
    public static URL CreateDiscoverMoviesURL(Boolean mostPopular)
    {
        // Build URI.
        Uri.Builder builder = Uri.parse(MOVIESDB_COMMON_BASE_URL).buildUpon();
        builder.appendPath(MOVIESDB_DISCOVER_MOVIE_DISCOVER_URL);
        builder.appendPath(MOVIESDB_DISCOVER_MOVIE_MOVIE_URL);
        if(mostPopular)
            builder.appendQueryParameter(MOVIESDB_DISCOVER_MOVIE_SORT_PARAM, MOVIESDB_DISCOVER_MOVIE_SORT_POPULARITY);
        else
            builder.appendQueryParameter(MOVIESDB_DISCOVER_MOVIE_SORT_PARAM, MOVIESDB_DISCOVER_MOVIE_SORT_RATING);
        builder.appendQueryParameter(MOVIESDB_COMMON_API_KEY_PARAM, MOVIESDB_COMMON_API_KEY);
        Uri uri = builder.build();

        // Build URL from URI.
        try {
            return new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Convert supplied JSON string to list of Movies.
     * @param moviesJSON JSON string to convert.
     * @return Created Movie list.
     */
    public static ArrayList<Movie> parseMoviesJson(String moviesJSON) {
        ArrayList<Movie> movies = new ArrayList<>();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = new GregorianCalendar();

        try {
            // Convert JSON of movies into individual movies, and their member variables.
            JSONObject moviesAsJSONObject = new JSONObject(moviesJSON);
            JSONArray resultsAsJSONArray = moviesAsJSONObject.getJSONArray(MOVIESDB_DISCOVER_MOVIE_RESULTS);

            for (int index = 0; index < resultsAsJSONArray.length(); index++) {
                JSONObject movieAsJSONObject = resultsAsJSONArray.getJSONObject(index);
                int identifier = movieAsJSONObject.getInt(MOVIESDB_DISCOVER_MOVIE_ID);
                String poster_path = MOVIESDB_COMMON_BASE_IMAGE_URL + movieAsJSONObject.getString(MOVIESDB_DISCOVER_MOVIE_POSTER_PATH);
                String original_title = movieAsJSONObject.getString(MOVIESDB_DISCOVER_MOVIE_ORIGINAL_TITLE);
                double vote_average = movieAsJSONObject.getDouble(MOVIESDB_DISCOVER_MOVIE_VOTE_AVERAGE);
                String overview = movieAsJSONObject.getString(MOVIESDB_DISCOVER_MOVIE_OVERVIEW);
                int releaseYear = Integer.MIN_VALUE; // Indicate release data not known.
                try {
                    // Although some movie entries provided by Movie DB are empty, some have no
                    // release date entry in the JSON at all! Handle by using optional way of
                    // retrieving string, which doesn't throw an exception.
                    Date release_date = dateFormatter.parse(movieAsJSONObject.optString(MOVIESDB_DISCOVER_MOVIE_RELEASE_DATE));
                    calendar.setTime(release_date);
                    releaseYear = calendar.get(YEAR);
                }
                catch(ParseException e) {
                    e.printStackTrace();
                }

                // Create movie.
                Movie movie = new Movie(identifier, original_title, poster_path, overview,  vote_average, releaseYear);

                // Add to list.
                movies.add(movie);
            }
        }
        catch(JSONException e) {
            e.printStackTrace();
        }

        return movies;
    }

    // Movie DB API: movie/<id>/videos URL.
    private final static String MOVIESDB_MOVIE_VIDEOS_MOVIE_URL = "movie";
    private final static String MOVIESDB_MOVIE_VIDEOS_VIDEOS_URL = "videos";

    // Movie DB API: movie/<id>/videos data names.
    private final static String MOVIESDB_MOVIE_VIDEOS_RESULTS = "results";
    private final static String MOVIESDB_MOVIE_VIDEOS_ID = "id";
    private final static String MOVIESDB_MOVIE_VIDEOS_KEY = "key";
    private final static String MOVIESDB_MOVIE_VIDEOS_NAME = "name";
    private final static String MOVIESDB_MOVIE_VIDEOS_SITE = "site";

    /**
     * Create URL to get videos of movie using Movie DB API, as identified by
     * specified id.
     * <p>
     * For example:
     * http://api.themoviedb.org/3/movie/419704/videos?api_key=54b4cb8c55da282ebcfe11fa9a735a40
     * </p>
     * @param id ID of movie.
     * @return The created URL or null if there was a problem.
     */
    public static URL CreateGetVideosURL(int id)
    {
        // Build URI.
        Uri.Builder builder = Uri.parse(MOVIESDB_COMMON_BASE_URL).buildUpon();
        builder.appendPath(MOVIESDB_MOVIE_VIDEOS_MOVIE_URL);
        builder.appendPath(String.valueOf(id));
        builder.appendPath(MOVIESDB_MOVIE_VIDEOS_VIDEOS_URL);
        builder.appendQueryParameter(MOVIESDB_COMMON_API_KEY_PARAM, MOVIESDB_COMMON_API_KEY);
        Uri uri = builder.build();

        // Build URL from URI.
        try {
            return new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Convert supplied JSON string to list of videos.
     * @param videosJSON JSON string to convert.
     * @return Created videos list.
     */
    public static ArrayList<Video> parseVideosJson(String videosJSON) {
        ArrayList<Video> videos = new ArrayList<>();

        try {
            // Convert JSON of videos into individual videos, and their member variables.
            JSONObject videosAsJSONObject = new JSONObject(videosJSON);
            JSONArray resultsAsJSONArray = videosAsJSONObject.getJSONArray(MOVIESDB_MOVIE_VIDEOS_RESULTS);

            for (int index = 0; index < resultsAsJSONArray.length(); index++) {
                JSONObject videoAsJSONObject = resultsAsJSONArray.getJSONObject(index);
                String identifier = videoAsJSONObject.getString(MOVIESDB_MOVIE_VIDEOS_ID);
                String key = videoAsJSONObject.getString(MOVIESDB_MOVIE_VIDEOS_KEY);
                String name = videoAsJSONObject.getString(MOVIESDB_MOVIE_VIDEOS_NAME);
                String site = videoAsJSONObject.getString(MOVIESDB_MOVIE_VIDEOS_SITE);

                // Create video.
                Video video = new Video(identifier, key, name, site);

                // Add to list.
                videos.add(video);
            }
        }
        catch(JSONException e) {
            e.printStackTrace();
        }

        return videos;
    }

    // Movie DB API: movie/<id>/reviews URL.
    private final static String MOVIESDB_MOVIE_REVIEWS_MOVIE_URL = "movie";
    private final static String MOVIESDB_MOVIE_REVIEWS_REVIEWS_URL = "reviews";

    // Movie DB API: movie/<id>/reviews data names.
    private final static String MOVIESDB_MOVIE_REVIEWS_RESULTS = "results";
    private final static String MOVIESDB_MOVIE_REVIEWS_ID = "id";
    private final static String MOVIESDB_MOVIE_REVIEWS_AUTHOR = "author";
    private final static String MOVIESDB_MOVIE_REVIEWS_CONTENT = "content";

    /**
     * Create URL to get reviews of movie using Movie DB API, as identified by
     * specified id.
     * <p>
     * For example:
     * http://api.themoviedb.org/3/movie/419704/reviews?api_key=54b4cb8c55da282ebcfe11fa9a735a40
     * </p>
     * @param id ID of movie.
     * @return The created URL or null if there was a problem.
     */
    public static URL CreateGetReviewsURL(int id)
    {
        // Build URI.
        Uri.Builder builder = Uri.parse(MOVIESDB_COMMON_BASE_URL).buildUpon();
        builder.appendPath(MOVIESDB_MOVIE_REVIEWS_MOVIE_URL);
        builder.appendPath(String.valueOf(id));
        builder.appendPath(MOVIESDB_MOVIE_REVIEWS_REVIEWS_URL);
        builder.appendQueryParameter(MOVIESDB_COMMON_API_KEY_PARAM, MOVIESDB_COMMON_API_KEY);
        Uri uri = builder.build();

        // Build URL from URI.
        try {
            return new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Convert supplied JSON string to list of reviews.
     * @param reviewsJSON JSON string to convert.
     * @return Created reviews list.
     */
    public static ArrayList<Review> parseReviewsJson(String reviewsJSON) {
        ArrayList<Review> reviews = new ArrayList<>();

        try {
            // Convert JSON of reviews into individual reviews, and their member variables.
            JSONObject reveiwsAsJSONObject = new JSONObject(reviewsJSON);
            JSONArray resultsAsJSONArray = reveiwsAsJSONObject.getJSONArray(MOVIESDB_MOVIE_REVIEWS_RESULTS);

            for (int index = 0; index < resultsAsJSONArray.length(); index++) {
                JSONObject reviewAsJSONObject = resultsAsJSONArray.getJSONObject(index);
                String identifier = reviewAsJSONObject.getString(MOVIESDB_MOVIE_REVIEWS_ID);
                String author = reviewAsJSONObject.getString(MOVIESDB_MOVIE_REVIEWS_AUTHOR);
                String content = reviewAsJSONObject.getString(MOVIESDB_MOVIE_REVIEWS_CONTENT);

                // Create review.
                Review review = new Review(identifier, author, content);

                // Add to list.
                reviews.add(review);
            }
        }
        catch(JSONException e) {
            e.printStackTrace();
        }

        return reviews;
    }

    // Youtube.
    private final static String YOUTUBE_BASE_URL = "https://www.youtube.com";
    private final static String YOUTUBE_WATCH_URL = "watch";
    private final static String YOUTUBE_KEY_PARAM = "v";

    /**
     * Create URI to play You Tube video identified by specified key in browser.
     * <p>
     * For example:
     * https://www.youtube.com/watch?v=P6AaSMfXHbA
     * </p>
     * @param youTubeVideoKey Key of video to play.
     * @return URI to play video.
     */
    public static Uri CreateYouTubeURI(String youTubeVideoKey) {
        // Build URI.
        Uri.Builder builder = Uri.parse(YOUTUBE_BASE_URL).buildUpon();
        builder.appendPath(YOUTUBE_WATCH_URL);
        builder.appendQueryParameter(YOUTUBE_KEY_PARAM, youTubeVideoKey);
        return builder.build();
    }

    /**
     * Check if internet is available.
     * <para>TCP/HTTP/DNS (depending on the port, 53=DNS, 80=HTTP, etc.).</para>
     * <para>Thanks to Levite of Stack Overflow for this solution:
     * https://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-times-out.</para>
     * @return True if available, otherwise false.
     */
    public static boolean isOnline() {
        try {
            int timeoutMs = 1500;
            Socket sock = new Socket();
            SocketAddress sockaddr = new InetSocketAddress("8.8.8.8", 53);

            sock.connect(sockaddr, timeoutMs);
            sock.close();

            return true;
        } catch (IOException e) { return false; }
    }
}
