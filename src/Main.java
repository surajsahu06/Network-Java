import java.util.ArrayList;
import java.util.List;

public class Main {

    //static list of ports where the three nodes are going to be run
    public static List<Integer> ports    = new ArrayList<Integer>();
    static {
        ports.add(6001);
        ports.add(6002);
        ports.add(6003);
    }

    // This program assumes that all the servers will be running on the same machine on different ports
    public static final String  ip       = "127.0.0.1";

    public static final String  basePath = "files/";

    public static void main(String args[]) {

        // create three servers corresponding to each node
        Thread fs1 = new Thread(new FileServer(ports.get(0), 1));
        Thread fs2 = new Thread(new FileServer(ports.get(1), 2));
        Thread fs3 = new Thread(new FileServer(ports.get(2), 3));

        // create three clients corresponding to each node
        Thread c1 = new Thread(new Client(ports.get(0), 1));
        Thread c2 = new Thread(new Client(ports.get(1), 2));
        Thread c3 = new Thread(new Client(ports.get(2), 3));
        fs1.start();
        fs2.start();
        fs3.start();

        c1.start();
        c2.start();
        c3.start();
    }
}
