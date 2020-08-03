package android.example.com;

/**
 * Class represents a Movie DB API review.
 */
public class Review {

    /**
     * Constructor.
     * @param identifier ID of review.
     * @param author Author of review.
     * @param content Content of review.
     */
    public Review(String identifier, String author, String content)
    {
        mIdentifier = identifier;
        mAuthor = author;
        mContent = content;
    }

    private String mIdentifier;
    /**
     * Get ID of review.
     * @return ID of review.
     */
    public String getIdentifier()
    {
        return mIdentifier;
    }

    private String mAuthor;
    /**
     * Get author of review.
     * @return Author of review.
     */
    public String getAuthor()
    {
        return mAuthor;
    }

    private String mContent;
    /**
     * Get content of review.
     * @return Content of review.
     */
    public String getContent()
    {
        return mContent;
    }
}
