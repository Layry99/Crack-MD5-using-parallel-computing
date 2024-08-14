# Crack-MD5-using-parallel-computing

Project overview


This project involves cracking an MD5 hash value into readable text using parallel computing. Multiple computers will work together to crack the password, with each computer assigned a different search area to eliminate redundancy. This approach makes the process of cracking the MD5 hash more efficient. If one computer successfully finds the password, it will notify the other computers to stop searching, and the computer that found the password will display the result.

To run the project.


Ensure that server-side is setup first. 
Server-side files should indlude.
1. CrackResult.java
2. PasswordCrackerImplement.java
3. PasswordCrackerServer.java
4. Password RemoteInterface.java

Compiled all files, and run the "PasswordCrackerServer"

After doing the all the steps above.
Ensure that client-side is setup.
Client-side files should include.
1. CrackResult.java
2. PasswordCrackerClient.java
3. PasswordRemoteInterface.java

Compiled all files, and run the "PasswordCrackerClient.java"
