import java.io.Serializable;

public class CrackResult implements Serializable {
    private static final long serialVersionUID = 1L;

    public final boolean found;
    public final String password;
    public final long timeSpent;  // in milliseconds
    public final int threadID;

    public CrackResult(boolean found, String password, long timeSpent, int threadID) {
        this.found = found;
        this.password = password;
        this.timeSpent = timeSpent;
        this.threadID = threadID;
    }
}
