
// PasswordCrackerImplement.java
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PasswordCrackerImplement extends UnicastRemoteObject implements PasswordRemoteInterface {
    private static final Logger LOGGER = Logger.getLogger(PasswordCrackerImplement.class.getName());
    private static final AtomicBoolean isPasswordFound = new AtomicBoolean(false);
    private static String foundPassword = null;
    private static int foundThreadID = -1;
    private static final long startTime = 0L;
    private static final Object lock = new Object(); // Lock to synchronize access to shared variables

    private PasswordRemoteInterface otherServerStub;

    public PasswordCrackerImplement() throws RemoteException {
        super();
    }

    public void setOtherServerStub(PasswordRemoteInterface stub) {
        this.otherServerStub = stub;
    }

    @Override
    public CrackResult crackPassword(String md5Hash, int passwordLength, int numThreads) throws RemoteException {
        isPasswordFound.set(false);

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        long startTime = System.currentTimeMillis();

        int rangePerThread = (126 - 33 + 1) / numThreads; // Calculate the range of characters per thread

        for (int i = 0; i < numThreads; i++) {
            int threadID = i;
            int startChar = 33 + i * rangePerThread;
            int endChar = (i == numThreads - 1) ? 126 : startChar + rangePerThread - 1; // Last thread takes the remaining range

            executor.submit(() -> {
                generatePasswords(md5Hash, passwordLength, "", 33, 126, threadID, numThreads);
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long timeSpent = System.currentTimeMillis() - startTime;
        return new CrackResult(isPasswordFound.get(), foundPassword, timeSpent, foundThreadID);
    }

    @Override
    public void notifyPasswordFound(String foundPassword, int foundThreadID) throws RemoteException {
        synchronized (lock) {
            if (!isPasswordFound.get()) {
                LOGGER.log(Level.INFO, "Received notification from Thread {0} on the other server.", foundThreadID);
                isPasswordFound.set(true);
                PasswordCrackerImplement.foundPassword = foundPassword;
                PasswordCrackerImplement.foundThreadID = foundThreadID;
                // Optionally: Notify the other threads on this server to stop searching
            }
        }
    }

    private void generatePasswords(String md5Hash, int passwordLength, String currentPassword, int minChar, int maxChar, int threadID, int numThreads) {
        synchronized (lock) {
            if (!isPasswordFound.get()) {
                LOGGER.log(Level.INFO, "Thread {0} starting.", threadID);
            }
        }
    
        int totalCharRange = maxChar - minChar + 1;
        int charsPerThread = totalCharRange / numThreads;
    
        // Calculate the starting character for the thread
        int startChar = minChar + threadID * charsPerThread;
    
        // Calculate the ending character for the thread
        int endChar = (threadID == numThreads - 1) ? maxChar : startChar + charsPerThread - 1;
    
        if (currentPassword.length() == 0) {
            // First character - use the divided range
            for (int j = startChar; j <= endChar; j++) {
                generatePasswordsHelper(md5Hash, passwordLength - 1, currentPassword + (char) j, minChar, maxChar, threadID);
            }
        } else {
            // Subsequent characters - use the full range
            generatePasswordsHelper(md5Hash, passwordLength, currentPassword, minChar, maxChar, threadID);
        }
    
        synchronized (lock) {
            if (!isPasswordFound.get()) {
                LOGGER.log(Level.INFO, "Thread {0} completed.", threadID);
            }
        }
    }

    private void generatePasswordsHelper(String md5Hash, int remainingLength, String currentPassword, int minChar, int maxChar, int threadID) {
        if (remainingLength == 0) {
            if (getMd5(currentPassword).equals(md5Hash) && isPasswordFound.compareAndSet(false, true)) {
                synchronized (lock) {
                    foundPassword = currentPassword;
                    foundThreadID = threadID;
                    LOGGER.log(Level.INFO, "Thread {0} found the password: {1}", new Object[]{threadID, foundPassword});
                    // Notify the other server
                    try {
                        if (otherServerStub != null) {
                            otherServerStub.notifyPasswordFound(foundPassword, foundThreadID);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                return;
            }
        } else if (!isPasswordFound.get()) {
            for (int j = minChar; j <= maxChar; j++) {
                generatePasswordsHelper(md5Hash, remainingLength - 1, currentPassword + (char) j, minChar, maxChar, threadID);
            }
        }
    }

    private static String getMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
