package android.example.com;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

/**
 * RecyclerView adapter for providing movie videos.
 */
public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideoViewHolder> {

    /**
     * Listener for notification of video selection.
     */
    public interface VideoSelectionListener {
        /**
         * Handle selection of video.
         * @param selectedVideo Selected video.
         */
        void onVideoSelected(Video selectedVideo);
    }

    private VideoSelectionListener mVideoSelectionListener;

    /**
     * Constructor.
     * @param videoSelectionListener Listener for notification of video selection.
     */
    public VideosAdapter(VideoSelectionListener videoSelectionListener)
    {
        mVideoSelectionListener = videoSelectionListener;
    }

    private ArrayList<Video> mVideos = new ArrayList<>();
    /**
     * Get list of video for display by RecyclerView.
     * @return List of videos.
     */
    public ArrayList<Video> getVideos() {
        return mVideos;
    }

    /**
     * Create instance of VideoAdapter class.
     * @param parent Parent ViewGroup to which view holder is to be added.
     * @param viewType Unused item type.
     * @return Instance of VideoAdapter class.
     */
    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Dynamically create layout for item.
        View videoItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item, parent, false);

        // Create VideoViewHolder.
        return new VideoViewHolder(videoItem);
    }

    /**
     * Bind supplied view holder to video at specified position in videos list.
     * @param holder View holder to bind.
     * @param position Position of video.
     */
    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        holder.Bind(position);
    }

    /**
     * Get total number of videos.
     * @return Total number of videos.
     */
    @Override
    public int getItemCount() {
        return getVideos().size();
    }

    /**
     * A ViewHolder subclass suitable for displaying a video, i.e. its icon and name.
     */
    public class VideoViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener {

        private ImageView mIcon;
        private TextView mName;

        /**
         * Constructor.
         * @param itemView View corresponding to video, to be bound to view holder.
         */
        public VideoViewHolder(View itemView) {
            super(itemView);

            // Retrieve ImageView and TextView.
            mIcon = (ImageView)itemView.findViewById(R.id.video_icon);
            mName = (TextView)itemView.findViewById(R.id.video_name);

            itemView.setOnClickListener(this);
        }

        /**
         * Bind view holder to video at specified position in videos list.
         * @param position Position of video.
         */
        public void Bind(int position){

            mName.setText(getVideos().get(position).getName());
        }

        /**
         * Handle selection of view holder to notify video selection listener of adapter.
         */
        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            mVideoSelectionListener.onVideoSelected(mVideos.get(position));
        }
    }
}
