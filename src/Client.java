//TCPClient.java

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

/*
 * Client thread to run and call the servers for different routines.
 */
public class Client implements Runnable {

    public int  port;
    public int  id;

    private int leaderPort;

    public Client(int port, int id) {
        super();
        this.port = port;
        this.id = id;
    }

    private void sendId() throws UnknownHostException, IOException, InterruptedException {
        // send the id 50 times to make sure, in any case, every client has got the leader with minimum id
        for (int i = 0; i < 50; i++) {
            for (int port : Main.ports) {
                Socket s = new Socket(Main.ip, port);
                PrintStream outToServer = new PrintStream(s.getOutputStream());
                BufferedReader inFromServer = new BufferedReader(new InputStreamReader(s.getInputStream()));
                outToServer.println("id:" + id + ":" + this.port);
                leaderPort = Integer.parseInt(inFromServer.readLine());
                s.close();
            }
            Thread.sleep(200);
        }
    }

    private void sendFileNames() throws UnknownHostException, IOException {
        Socket s = new Socket(Main.ip, leaderPort);
        PrintStream outToServer = new PrintStream(s.getOutputStream());

        StringBuilder sb = new StringBuilder();
        File[] files = new File(Main.basePath + port).listFiles();
        for (File f : files) {
            sb.append(f.getName() + ",");
            System.out.println("Client id:" + id + " registered the file:" + f.getName() + " with leader on port:" + leaderPort);
        }
        outToServer.println("file:" + port + ":" + sb.toString());
        s.close();
    }

    private void takeFile(String fileName) throws UnknownHostException, IOException {
        System.out.println("Trying to fetch the file:" + fileName + " for client id:" + id);
        System.out.println("querying the leader for the address of the file");
        Socket s = new Socket(Main.ip, leaderPort);
        PrintStream outToServer = new PrintStream(s.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(s.getInputStream()));
        outToServer.println("fileAsk:" + fileName);
        String resp = inFromServer.readLine();
        s.close();
        if ("not found".equals(resp)) {
            System.out.println("file not found");
        } else {
            System.out.println("file found on port:" + resp);
            System.out.println("making a connection...");
            Socket s1 = new Socket(Main.ip, Integer.parseInt(resp));
            PrintStream out = new PrintStream(s1.getOutputStream());
            DataInputStream in = new DataInputStream(s1.getInputStream());
            out.println("fileGive:" + fileName);
            File f = new File(Main.basePath + port + "/" + fileName);
            f.setWritable(true);
            FileOutputStream fos = new FileOutputStream(f);
            byte[] b = new byte[1024];
            int len = 0;
            while ((len = in.read(b, 0, 1024)) != -1) {
                fos.write(b, 0, len);
            }
            fos.flush();
            fos.close();
            s1.close();
            System.out.println("Received the file");
        }
    }

    @Override
    public void run() {
        // start with deciding the leader by sending individual ids and receiving the latest leader.
        try {
            sendId();
        } catch (Exception e) {
            // there are bound to be a few connection reset exceptions here, but they do not affect us so moving on.
        }
        System.out.println("Client id:" + id + " got the leader port as:" + leaderPort);
        // try to register the file names on the leader.
        try {
            sendFileNames();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // try to sleep this thread so that other threads can register their files too, before any one of them starts to fetch them.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
        }
        // try to take all the files.
        try {
            takeFile("f1.txt");
            takeFile("f2.txt");
            takeFile("f3.txt");
            takeFile("f4.txt");
            takeFile("f5.txt");
            takeFile("f6.txt");
            takeFile("f7.txt");
            takeFile("f8.txt");
            takeFile("f9.txt");
            // This file doesn't exist, so it will give the proof of file on found
            takeFile("f10.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Client id:" + id + "exiting.");
    }
}