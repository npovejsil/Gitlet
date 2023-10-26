package gitlet;
import java.text.SimpleDateFormat;
import java.util.TreeMap;
import java.util.Date;
import java.io.Serializable;

/**
 * Append new blobs to existing treemap
 * new additions/removals make up new commit (to store)
 * match a pointer to a blob in an arraylist??? I want to a
 * commit to reference any relevant blobs.
 * making an object with the id and the file to be committed.
 * @author Nora Povejsil
 */
public class Commit implements Serializable {
    /**
     * Commit message.
     */
    private final String message;
    /**
     * Commit time.
     */
    private final String timestamp;
    /**
     * Commit blopMap.
     */
    private final TreeMap<String, String> blobMap;
    /**
     * Commit parent.
     */
    private final String parent;
    /**
     * Commit second parent.
     */
    private final String secondParent;
    /**
     * Commit initial commit.
     */
    private final boolean isInitialCommit;

    /** append new blobs to existing treemap
     * new additions/removals make up new commit (to store)
     * match a pointer to a blob in an arraylist??? I want to a
     * commit
     * to reference any relevant blobs.
     * making an object with the id and the file to be committed.
     * @param blobs
     * @param initial
     * @param message1
     * @param parent1
     * @param secondParent1
     */
    public Commit(String message1, TreeMap<String, String> blobs,
                  String parent1, String secondParent1,
                  boolean initial) {
        message = message1;
        isInitialCommit = initial;
        if (isInitialCommit) {
            timestamp = "Wed Dec 31 16:00:00 1969 -0800";
        } else {
            Date dateHolder = new Date();
            SimpleDateFormat format =
                    new SimpleDateFormat("EEE MMM d HH:mm:ss "
                            + "yyyy Z");
            timestamp = format.format(dateHolder);
        }
        secondParent = secondParent1;
        blobMap = blobs;
        parent = parent1;


    }
    /** Get second parent.
     * @return string
    */
    public String getsecondParent() {
        return secondParent;
    }
    /** Get second parent.
     * @return treemap
     */
    public TreeMap<String, String> getBlobs() {
        return blobMap;
    }

    /** Get second parent.
     * @return string
     */
    public String getParent() {
        return parent;
    }

    /** Get second parent.
     * @return string
     */
    public String getMessage() {
        return message;
    }

    /** Get second parent.
     * @return string
     */
    public String getTimeStamp() {
        return timestamp;
    }

    /** Get second parent.
     * @return string
     */
    @Override
    public String toString() {
        return new String("=== \n ... \n ...");
    }
}
