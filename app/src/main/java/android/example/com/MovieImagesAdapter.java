package android.example.com;

import android.example.com.Database.Movie;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * RecyclerView adapter for providing movie images.
 */
public class MovieImagesAdapter extends RecyclerView.Adapter<MovieImagesAdapter.MovieImageViewHolder> {

    /**
     * Listener for notification of movie selection.
     */
    public interface MovieSelectionListener {
        /**
         * Handle selection of movie.
         * @param selectedMovie Selected movie.
         * @param imageWidth Width, to which image has been resized.
         * @param imageHeight Height, to which image has been resized.
         */
        void onMovieSelected(Movie selectedMovie, int imageWidth, int imageHeight);
    }

    private int mImageWidth;
    private int mImageHeight;
    private MovieSelectionListener mMovieSelectionListener;

    /**
     * Constructor.
     * @param imageWidth Width, to which image is to be resized.
     * @param imageHeight Height, to which image is to be resized.
     * @param movieSelectionListener Listener for notification of movie selection.
     */
    public MovieImagesAdapter(int imageWidth, int imageHeight, MovieSelectionListener movieSelectionListener)
    {
        mImageWidth = imageWidth;
        mImageHeight = imageHeight;
        mMovieSelectionListener = movieSelectionListener;
    }

    private ArrayList<Movie> mMovies = new ArrayList<>();
    /**
     * Get list of movies for display by RecyclerView.
     * @return List of movies.
     */
    public ArrayList<Movie> getMovies() {
        return mMovies;
    }

    /**
     * Create instance of MovieImagesAdapter class.
     * @param parent Parent ViewGroup to which view holder is to be added.
     * @param viewType Unused item type.
     * @return Instance of MovieImagesAdapter class.
     */
    @NonNull
    @Override
    public MovieImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Dynamically create layout for item.
        View movieImagesItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_images_item, parent, false);

        // Create MovieImageViewHolder.
        return new MovieImageViewHolder(movieImagesItem);
    }

    /**
     * Bind supplied view holder to movie at specified position in movies list.
     * @param holder View holder to bind.
     * @param position Position of movie.
     */
    @Override
    public void onBindViewHolder(@NonNull MovieImageViewHolder holder, int position) {
        holder.Bind(position);
    }

    /**
     * Get total number of movies.
     * @return Total number of movies.
     */
    @Override
    public int getItemCount() {
        return getMovies().size();
    }

    /**
     * A ViewHolder subclass suitable for displaying a movie image.
     */
    public class MovieImageViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener {

        private ImageView mMovieImage;

        /**
         * Constructor.
         * @param itemView View corresponding to movie images item, to be bound to view holder.
         */
        public MovieImageViewHolder(View itemView) {
            super(itemView);

            // Retrieve ImageView.
            mMovieImage = (ImageView)itemView.findViewById(R.id.movie_image);

            itemView.setOnClickListener(this);
        }

        /**
         * Bind view holder to movie at specified position in movies list.
         * <pare>This is where the images are resized to fit the width of the screen.</pare>
         * @param position Position of movie.
         */
        public void Bind(int position){

            Picasso.get().load(getMovies().get(position).getImagePath()).resize(mImageWidth, mImageHeight).into(mMovieImage);
        }

        /**
         * Handle selection of view holder to notify movie selection listener of adapter.
         */
        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            mMovieSelectionListener.onMovieSelected(mMovies.get(position), mImageWidth, mImageHeight);
        }
    }
}
