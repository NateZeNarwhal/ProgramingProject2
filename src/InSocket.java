
import java.net.*;
import java.io.*;

public class InSocket {
    int myid= 0;


    public void run() {
        try {
            int serverPort = 4020;
            ServerSocket serverSocket = new ServerSocket(serverPort);
            serverSocket.setSoTimeout(100000);
            while(true) {
                //same for all-
                System.out.println("Waiting for clients on port " + serverSocket.getLocalPort() + "...");

                Socket server1 = serverSocket.accept();
                System.out.println("Just connected to server 1");
                Socket server2 = serverSocket.accept();
                System.out.println("Just connected to server 2");
                Socket server3 = serverSocket.accept();
                System.out.println("Just connected to server 3");
                //delay ----
                wait(30000);


                PrintWriter toClient1 =
                        new PrintWriter(server1.getOutputStream(),true);
                PrintWriter toClient2 =
                        new PrintWriter(server2.getOutputStream(),true);
                PrintWriter toClient3 =
                        new PrintWriter(server3.getOutputStream(),true);


                BufferedReader fromClient1 =
                        new BufferedReader(
                                new InputStreamReader(server1.getInputStream()));
                String line = fromClient1.readLine();
                System.out.println("Server received: " + line);
                toClient1.println("Thank you for connecting to " + server1.getLocalSocketAddress() + "\nGoodbye!");
            }
        }
        catch(UnknownHostException ex) {
            ex.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        InSocket srv = new InSocket();
        srv.run();
    }
}
