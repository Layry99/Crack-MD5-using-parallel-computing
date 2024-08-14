/*
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class PasswordCrackerClient {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            // Allow the user to input the MD5 hash, password length, number of threads, and number of servers
            System.out.print("Enter the MD5 hash: ");
            String md5Hash = scanner.nextLine();

            System.out.print("Enter password length: ");
            int passwordLength = scanner.nextInt();

            System.out.print("Enter the number of threads for each server: ");
            int numThreadsPerServer = scanner.nextInt();

            System.out.print("Enter the number of servers: ");
            int numServers = scanner.nextInt();

            // Create an array to hold the results from each server
            CrackResult[] results = new CrackResult[numServers];

                for (int i = 1; i <= numServers; i++) {
                    String serverIP;
                    int serverPort;

                    if (i == 1) {
                        serverIP = "localhost";
                        serverPort = 1099;
                    } else {
                        serverIP = "192.168.231.57"; // Adjust the IP address of your second server
                        serverPort = 1098; // Adjust the port number of your second server
                    }

                    Registry registry = LocateRegistry.getRegistry(serverIP, serverPort);
                    PasswordRemoteInterface stub = (PasswordRemoteInterface) registry.lookup("PasswordCracker");

                    // Pass the user input and number of threads per server to each server
                    results[i - 1] = stub.crackPassword(md5Hash, passwordLength, numThreadsPerServer);
                }


            // Combine and display the final result
            CrackResult finalResult = combineResults(results);
            System.out.println("Password cracked: " + finalResult.found);
            if (finalResult.found) {
                System.out.println("Password: " + finalResult.password);
                System.out.println("Time spent: " + finalResult.timeSpent + " milliseconds");
                System.out.println("Thread ID: " + finalResult.threadID);
            }
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

    // Combine results from multiple servers
    private static CrackResult combineResults(CrackResult[] results) {
        boolean found = false;
        String password = null;
        long timeSpent = 0;
        int threadID = -1;

        for (CrackResult result : results) {
            if (result.found) {
                found = true;
                password = result.password;
                timeSpent += result.timeSpent;
                threadID = result.threadID;
            }
        }

        return new CrackResult(found, password, timeSpent, threadID);
    }
}

*/

// PasswordCrackerClient.java
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PasswordCrackerClient {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            // Allow the user to input the MD5 hash, password length, number of threads, and number of servers
            System.out.print("Enter the MD5 hash: ");
            String md5Hash = scanner.nextLine();

            System.out.print("Enter password length: ");
            int passwordLength = scanner.nextInt();

            System.out.print("Enter the number of threads for each server: ");
            int numThreadsPerServer = scanner.nextInt();

            System.out.print("Enter the number of servers: ");
            int numServers = scanner.nextInt();

            // Create an array to hold the results from each server
            CrackResult[] results = new CrackResult[numServers];
            ExecutorService executorService = Executors.newFixedThreadPool(numServers);
            CountDownLatch latch = new CountDownLatch(numServers);

            // Declare and initialize the startTime variable
            long startTime = System.nanoTime();

            for (int i = 1; i <= numServers; i++) {
                String serverIP;
                int serverPort;

                if (i == 1) {
                    serverIP = "localhost";
                    serverPort = 1098;
                } else {
                    serverIP = "192.168.231.137"; // Adjust the IP address of your second server
                    serverPort = 1099; // Adjust the port number of your second server
                }

                final int serverIndex = i;
                executorService.submit(() -> {
                    try {
                        Registry registry = LocateRegistry.getRegistry(serverIP, serverPort);
                        PasswordRemoteInterface stub = (PasswordRemoteInterface) registry.lookup("PasswordCracker");

                        // Pass the user input and number of threads per server to each server
                        results[serverIndex - 1] = stub.crackPassword(md5Hash, passwordLength, numThreadsPerServer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown(); // Signal that this thread has completed
                    }
                });
            }

            // Wait for all threads to finish
            latch.await();
            executorService.shutdown();

            // Combine and display the final result
            CrackResult finalResult = combineResults(results);
            System.out.println("Password cracked: " + finalResult.found);
            if (finalResult.found) {
                System.out.println("Password: " + finalResult.password);
                long elapsedTime = System.nanoTime() - startTime;
                System.out.println("Time spent: " + elapsedTime / 1_000_000 + " milliseconds"); // Convert nanoseconds to milliseconds
                System.out.println("Thread ID: " + finalResult.threadID);
            }
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

    // Combine results from multiple servers
    private static CrackResult combineResults(CrackResult[] results) {
        boolean found = false;
        String password = null;
        long timeSpent = 0;
        int threadID = -1;

        for (CrackResult result : results) {
            if (result.found) {
                found = true;
                password = result.password;
                timeSpent += result.timeSpent;
                threadID = result.threadID;
            }
        }

        return new CrackResult(found, password, timeSpent, threadID);
    }
}
