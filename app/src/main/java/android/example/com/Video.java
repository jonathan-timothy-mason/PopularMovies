package android.example.com;

/**
 * Class represents a Movie DB API video.
 */
public class Video {

    /**
     * Constructor.
     * @param identifier ID if video.
     * @param key Key of video.
     * @param name Name of video.
     * @param site Site hosting video.
     */
    public Video(String identifier, String key, String name, String site)
    {
        mIdentifier = identifier;
        mKey = key;
        mName = name;
        mSite = site;
    }

    private String mIdentifier;
    /**
     * Get ID of video.
     * @return ID of video.
     */
    public String getIdentifier()
    {
        return mIdentifier;
    }

    private String mKey;
    /**
     * Get key of video.
     * @return Key of video.
     */
    public String getKey()
    {
        return mKey;
    }

    private String mName;
    /**
     * Get name of video.
     * @return Name of video.
     */
    public String getName()
    {
        return mName;
    }

    private String mSite;
    /**
     * Get site hosting video.
     * @return Site hosting video.
     */
    public String getSite()
    {
        return mSite;
    }
}
