import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Server class for servicing all the intercommunication among the nodes
 */
public class FileServer implements Runnable {

    public int                  port;
    public int                  id;
    public Map<String, Integer> files = new HashMap<String, Integer>();

    private int                 leaderId;
    private int                 leaderPort;

    public FileServer(int port, int id) {
        super();
        this.port = port;
        this.id = id;
        this.leaderId = id;
        this.leaderPort = port;
    }

    @Override
    public void run() {
        try {
            // create a server socket and start listening infinitely
            ServerSocket ss = new ServerSocket(port);
            System.out.println("Server Waiting for client on port:" + port);
            while (true) {
                Socket s = ss.accept();
                // Get reader and printer to the client connection
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(s.getInputStream()));
                PrintStream outToClient = new PrintStream(s.getOutputStream());

                // Client sent a request
                String rcvd = inFromClient.readLine();

                // if the request is to register the client and decide the leader
                if (rcvd.startsWith("id:")) {
                    int receivedId = Integer.parseInt(rcvd.split(":")[1]);
                    if (receivedId < leaderId) {
                        leaderId = receivedId;
                        leaderPort = Integer.parseInt(rcvd.split(":")[2].trim());
                    }
                    // send back the new leader port
                    outToClient.println(leaderPort);
                }
                // if the request is to register the presence of a file on a particular node
                if (rcvd.startsWith("file:")) {
                    int port = Integer.parseInt(rcvd.split(":")[1].trim());
                    for (String fileName : rcvd.split(":")[2].split(",")) {
                        files.put(fileName, port);
                    }
                }
                // if the request is to enquire about the address of a file
                if (rcvd.startsWith("fileAsk:")) {
                    if (files.containsKey(rcvd.split(":")[1])) {
                        outToClient.println(files.get(rcvd.split(":")[1]));
                    } else {
                        outToClient.println("not found");
                    }
                }
                // send the file
                if (rcvd.startsWith("fileGive:")) {
                    String fileName = rcvd.split(":")[1];
                    FileInputStream fis = new FileInputStream(new File(Main.basePath + port + "/" + fileName));
                    byte[] b = new byte[1024];
                    int len = 0;
                    while ((len = fis.read(b, 0, 1024)) != -1) {
                        s.getOutputStream().write(b, 0, len);
                    }
                    s.getOutputStream().flush();
                    s.getOutputStream().close();

                }
                s.close();
            }
        } catch (Exception e) {
            System.out.println("error in the server on port:" + port);
            e.printStackTrace();
        }

    }

}
