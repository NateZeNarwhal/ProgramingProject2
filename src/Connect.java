//Nathan Diepenbrock
//NLD200000
//07/13/22
//CS 4348: Operating Systems
//Programming Assignment 2

//imports
import java.net.*;
import java.io.*;
import java.util.Objects;
import java.util.Scanner;
import static java.lang.System.exit;

public class Connect {
    //variables
    //array of socket connections 1-4 are used 5 is extra.
    public Socket[] servers = new Socket[5];
    public String[] hostnames = new String[5];
    public int me;
    //count the number of recognized stop messages
    public int stop = 0;
    //used to stop loops in the event of a stop command
    public boolean control = true;

    public Connect(String[] arg) {
        me = Integer.parseInt(arg[0]);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        //starts up the driving function of a created object
        Connect start = new Connect(args);
        start.startup();


    }

    //given the current servers' id, the method will then preform the proper connections and accept the proper conections.
    //after that it will await input from the user.
    public void startup() throws IOException, InterruptedException {
        //all hostnames used - I used the utd NET pcs net31 - net 34
        hostnames[1] = "10.176.69.158";
        hostnames[2] = "10.176.69.159";
        hostnames[3] = "10.176.69.160";
        hostnames[4] = "10.176.69.161";

        //first we unlock all the doors needed for our id
        //seperate thread so the process is able to accept and send at the same time. Dead lock is not possable this way as well
        //because number 1 opens all its doors first, before ever unlocking them
        Thread thread1 = new Thread(() -> {
            try {
                door();
                log("thread 1 end!!");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        //then we start trying connections
        Thread thread2 = new Thread(() -> {
            try {
                open();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        //Checks for incoming messages
        Thread thread3 = new Thread(() -> {
            try {
                listen();
                log("thread 1 end!!");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });


        log("thread 1 start");
        //thread one calls Door()
        thread1.start();

        log("PAUSE");
        //This is to account for the delay it can take to get all the programs running at
        //It is crucial they all meet here time wise!
        Thread.sleep(10000);
        log("thread 2 start");
        //thread 2 calls the open method
        thread2.start();
        Thread.sleep(10000);
        //thread 3 starts. This is the listening thread
        thread3.start();
        //we now have 4 threads
        //1 create doors
        //2 opens the doors
        //3 listens to messages
        //and 4 (original) is used for user input:

        //This takes in the users input, validates it. Then runs the appropriate command
        String msg = "";
        int destinationId = 0;
        //This never stops until the program is stopped by the stop command:
        while (true) {
            Scanner scan = new Scanner(System.in);
            String first = scan.next();

            if (Objects.equals(first, "send") || Objects.equals(first, "stop")) {
                //we need not worry about anything else if the command is stop, we just start the stopping process
                //by sending stop to all other processes
                if (Objects.equals(first, "stop")) {
                    speak(0,"stop");
                }
                destinationId = Integer.parseInt(scan.next());
                msg = scan.nextLine();
                //Normal command, send to desid
                speak(destinationId,msg);

            } else {
                System.out.println("Invalid");
            }
            //Message is printed out to the original console as well.
            System.out.println(msg);



        }
    }
    // Simple logging function
    public void log(String msg) {
        System.out.println("PROCESS: " + me + " says:  " + msg);
    }

    //this function was what took me the most time.
    public void door() throws IOException {
        //in logic
        int serverPort = 4020;
        ServerSocket serverSocket = new ServerSocket(serverPort);
        //same for all-
        log("Waiting for clients on port " + serverSocket.getLocalPort() + "...");
        //depending on the server we are, we open that many doors
        switch (me) {
            //server 1 only starts connections,
            case 1:
                break;
            //server 2 only accepts from server one, and sends to the rest
            case 2:
                log("Waiting for server 1");
                servers[1] = serverSocket.accept();
                log("connection received from server 1");
                break;
            //server 3 waits for 1 and 2
            case 3:
                log("Waiting for server 1");
                servers[1] = serverSocket.accept();
                log("connection received from server 1");
                log("Waiting for server 2");
                servers[2] = serverSocket.accept();
                log("connection received from server 2");
                break;
            case 4:
                //server 4 waits for all
                log("Waiting for server 1");
                servers[1] = serverSocket.accept();
                log("connection received from server 1");
                log("Waiting for server 2");
                servers[2] = serverSocket.accept();
                log("connection received from server 2");
                log("Waiting for server 3");
                servers[3] = serverSocket.accept();
                log("connection received from server 3");
                break;

        }

    }
    //this function is the inverse of the last
    //the ones that didn't wait, now open.
    public void open() throws IOException {
        switch (me) {
            case 1:
                servers[2] = conect(hostnames[2]);
                log("connected to server 2");
                servers[3] = conect(hostnames[3]);
                log("connected to server 3");
                servers[4] = conect(hostnames[4]);
                log("connected to server 4");
                break;

            case 2:
                servers[3] = conect(hostnames[3]);
                log("connected to server 3");
                servers[4] = conect(hostnames[4]);
                log("connected to server 4");
                break;

            case 3:
                servers[4] = conect(hostnames[4]);
                log("connected to server 4");
                break;
            //4 has already established its connections
            case 4:
                break;
        }

    }
    //This runs for every line that comes in as input from another server
    //It checks its contents and preforms the correct actions
    public void checkLine(String line) throws IOException {
        if (!line.isEmpty()) {
            System.out.println("Server received: " + line);

        }
        //if the command is to stop this runs-

        if (line.contains("stop")) {
            //This keeps track of how many servers are ready to stop. Currently one for the one who sent the command
            stop++;
            log("received stop command");

        }
        //if stop is one, we say that we have stopped to everyone
        if (stop == 1) {

            speak(0, "stop");

        }
        //then we wait for all 4 servers to say they are stoped, then we set control = 0. which stops the program
        if (stop == 4) {
            log("fully shutting down");
            control = false;
        }

    }

//This listens for input at every loop, only stopping when control = 0
    public void listen() throws IOException {
        String lime = "";
        BufferedReader fromClient1 = null, fromClient2 = null, fromClient3 = null, fromClient4 = null;
        if (me != 1) {
            fromClient1 =
                    new BufferedReader(
                            new InputStreamReader(servers[1].getInputStream()));
        }
        if (me != 2) {
            fromClient2 =
                    new BufferedReader(
                            new InputStreamReader(servers[2].getInputStream()));
        }
        if (me != 3) {
            fromClient3 =
                    new BufferedReader(
                            new InputStreamReader(servers[3].getInputStream()));
        }

        if (me != 4) {
            fromClient4 =
                    new BufferedReader(
                            new InputStreamReader(servers[4].getInputStream()));
        }

        //The never ending loop
        //checks for messages at every loop
        while (control) {


            if (fromClient1 != null && fromClient1.ready()) {
                checkLine(fromClient1.readLine());
            }


            if (fromClient2 != null && fromClient2.ready()) {
                checkLine(fromClient2.readLine());
            }


            if (fromClient3 != null && fromClient3.ready()) {
                checkLine(fromClient3.readLine());
            }


            if (fromClient4 != null && fromClient4.ready()) {
                checkLine(fromClient4.readLine());
            }


        }
        //when control = 0 we stop the program.
        exit(29);
    }

    //takes in the id and message and sends what it needs to
    public void speak(int id, String msg) throws IOException {
        PrintWriter toClient1 = null, toClient2 = null, toClient3 = null, toClient4 = null;

        if (me != 1) toClient1 = new PrintWriter(servers[1].getOutputStream(), true);

        if (me != 2) toClient2 = new PrintWriter(servers[2].getOutputStream(), true);

        if (me != 3) toClient3 = new PrintWriter(servers[3].getOutputStream(), true);

        if (me != 4) toClient4 = new PrintWriter(servers[4].getOutputStream(), true);

        if (msg.contains("stop")) {
            stop++;
        }




        switch (id){
            case 0:
                if (me != 1) toClient1.println(msg);
                if (me != 2) toClient2.println(msg);
                if (me != 3) toClient3.println(msg);
                if (me != 4) toClient4.println(msg);
                break;
            case 1:
                toClient1.println(msg);
                break;
            case 2:
                toClient2.println(msg);
                break;
            case 3:
                toClient3.println(msg);
                break;
            case 4:
                toClient4.println(msg);
                break;

        }

    }

    public Socket conect(String host) throws IOException {
        return new Socket(host, 4020);

    }
}
