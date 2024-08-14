import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class PasswordCrackerServer {
    public static void main(String[] args) {
        try {
            // Set the externally reachable hostname or IP address of the server
            System.setProperty("java.rmi.server.hostname", "localhost");     

            PasswordRemoteInterface localStub = new PasswordCrackerImplement();

            int localPortNumber = 1098; // Adjust port number accordingly
            int otherPortNumber = 1099; // Adjust the port number of the other server

            Registry localRegistry = LocateRegistry.createRegistry(localPortNumber);
            localRegistry.rebind("PasswordCracker", localStub);

            System.out.println("Local server is ready on port " + localPortNumber);

            // Get the remote object from the other server
            Registry otherRegistry = LocateRegistry.getRegistry("192.168.204.180", otherPortNumber); // Adjust the IP and port
            PasswordRemoteInterface otherServerStub = (PasswordRemoteInterface) otherRegistry.lookup("PasswordCracker");

            // Set the other server's stub on the local server
            ((PasswordCrackerImplement) localStub).setOtherServerStub(otherServerStub);

        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

}
