import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PasswordRemoteInterface extends Remote {
    CrackResult crackPassword(String md5Hash, int passwordLength, int numThreads) throws RemoteException;
    void notifyPasswordFound(String foundPassword, int foundThreadID) throws RemoteException;
}
