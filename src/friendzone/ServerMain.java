package friendzone;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import friendzone.Coordination;
import friendzone.Debug;
import friendzone.Encryption;
import friendzone.ServerConnection;
import friendzone.ServerFileStoring;
import friendzone.TanTanAcc;
import friendzone.TinderAcc;
import friendzone.UserAcc;

/**
 * This class is used to set up a TCP server and connect all the user
 * together.<br>
 *
 * Knowledge used:<br>
 *
 * What is socket programming?<br>
 * https://www.javatpoint.com/socket-programming
 * https://www.baeldung.com/a-guide-to-java-sockets
 *
 * How to check public ip?<br>
 * cmd -> nslookup myip.opendns.com. resolver1.opendns.com <br>
 *
 * Why Runnable is used instead of Thread?<br>
 * https://www.tutorialspoint.com/java/java_multithreading.htm<br>
 * https://stackoverflow.com/questions/541487/implements-runnable-vs-extends-thread-in-java<br>
 * https://stackoverflow.com/questions/8579657/whats-the-difference-between-thread-start-and-runnable-run#8579695<br>
 *
 * Callable and ExecutorService may be used for future for improvement.<br>
 * TCP may be replace by UDP in future.<br>
 *
 * What is lambda expression?<br>
 * https://alvinalexander.com/java/java-8-lambda-thread-runnable-syntax-examples/<br>
 * https://beginnersbook.com/2017/10/java-lambda-expressions-tutorial-with-examples/<br>
 *
 * how to stop thread safely?<br>
 * https://www.geeksforgeeks.org/killing-threads-in-java/<br>
 *
 * Cocurrent collections:<br>
 * https://www.codejava.net/concurrency-tutorials<br>
 * https://www.codejava.net/java-core/concurrency/java-concurrent-collection-copyonwritearrayset-example<br>
 *
 * Current issue:<br>
 * The server only support for local network. User from different network cannot
 * connect to here.<br>
 * Have not implement a method to detect the client still online or not
 */
public class ServerMain {

    private ServerSocket server;
    private BufferedReader input;

    public static int port = 1414;
    public static int latestUserID = 0;

    private Thread receiveRequstService_thread;
    private Thread pairingBoyGirlService_thread;
    private Thread pairingHomoBoyService_thread;
    private Thread pairingHomoGirlService_thread;

    // store the pending request
    public static final ConcurrentHashMap<UserAcc, ServerConnection> onlineMap = new ConcurrentHashMap<>();
    public static CopyOnWriteArraySet<UserAcc> userList = new CopyOnWriteArraySet<>();
    public static CopyOnWriteArrayList<TinderAcc> TinderUserList = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<TanTanAcc> TanTanUserList = new CopyOnWriteArrayList<>();
    
    public static CopyOnWriteArrayList<ServerConnection> boyQueue = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<ServerConnection> girlQueue = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<ServerConnection> homoBoyQueue = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<ServerConnection> homoGirlQueue = new CopyOnWriteArrayList<>();

    public static CopyOnWriteArrayList<UserAcc> boyList = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<UserAcc> girlList = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<UserAcc> homoBoyList = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<UserAcc> homoGirlList = new CopyOnWriteArrayList<>();

    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");

    public static void main(String[] args) throws IOException {
        Encryption.key = Encryption.loadkey();
        printMsg("Starting the server");
        ServerMain s = new ServerMain(port);
        printMsg("Reading user account file");
        userList = ServerFileStoring.importUserListObjFile();
        ServerFileStoring.distributeUserList(userList);
        TinderUserList = ServerFileStoring.importTinderUserListObjFile();
        TanTanUserList = ServerFileStoring.importTanTanUserListObjFile();
        printMsg("Read success");
        s.receiveRequstService();
        printMsg("Requst Receiving Service is runnning");
        ServerFileStoring.exportUserDataFileScheduledService();
        printMsg("User File Exportion Scheduled Service is runnning");
//        ServerFileStoring.showOnlineUserScheduledService();
        s.pairingBoyGirlService();
        s.pairingHomoBoyService();
        s.pairingHomoGirllService();
        printMsg("Pairing Service is runnning");

    }

    /**
     * To construct Server class object.
     *
     * @param port port number for ServerSocket
     */
    public ServerMain(int port) {
        try {
            // create server socket with port
            server = new ServerSocket(port);
            input = new BufferedReader(new InputStreamReader(System.in));
            printMsg("Local address of this server: " + server.getInetAddress());
            printMsg("Channel: " + server.getChannel());
        }
        catch(IOException e) {
            Debug.println("Notice from ServerMain constructor:");
            Debug.println("Exception caught when trying to listen on port " + port + " or listening for a connection");
            Debug.println(e.getMessage());
        }
        printMsg("Server is running");
    }

    /**
     * To start a new thread to handle the connection request.
     */
    public void receiveRequstService() {
        Runnable runnable = () -> {
            while(!Thread.interrupted()) {
                try {
                    ServerConnection newConnection = new ServerConnection(server.accept());
                    printMsg("{" + newConnection.getUserName() + "} established a connection");
                    newConnection.connectionService();
                    Thread.sleep(500); // wait for 0.5sec for next request
                }
                catch(Exception e) {
                    Debug.println("Notice from ServerMain receiveRequstService():");
                    Debug.println(e.getStackTrace());
                    Debug.println(e.getCause().getMessage());
                }
            }
            Debug.println("Stop thread");
        };
        receiveRequstService_thread = new Thread(runnable);
        receiveRequstService_thread.start();
    }

    public void pairingBoyGirlService() {
        Runnable runnable = () -> {
            while(!Thread.interrupted()) {
                try {
                    if(boyQueue.size() > 0 && girlQueue.size() > 0) {
                        if(boyQueue.size() < girlQueue.size()) {
                            compareGirlList();
                        }
                        else {
                            compareBoyList();
                        }
                    }
                }
                catch(Exception e) {
                    Debug.println("Notice from ServerMain pairingBoyGirlService():");
                    Debug.println(e.toString());
                }
            }
        };
        pairingBoyGirlService_thread = new Thread(runnable);
        pairingBoyGirlService_thread.start();
    }

    public void compareGirlList() {
        ServerConnection sourceBoy = boyQueue.get(0);
        ServerConnection targetGirl = null;
        double minDist = Double.MAX_VALUE;
        int highScore = 0;
        int tempScore = 0;
        double tempDist = 0;
        for(ServerConnection x : girlQueue) {
            tempScore = 0;
            tempDist = Coordination.calDist(sourceBoy.userAcc, x.userAcc);
            for(String interest1 : sourceBoy.userAcc.interest) {
                for(String interest2 : x.userAcc.interest) {
                    if(interest1.equals(interest2)) {
                        tempScore++;
                    }
                }
            }
            if(tempDist <= minDist) {
                if(tempScore >= highScore) {
                    minDist = tempDist;
                    highScore = tempScore;
                    targetGirl = x;
                }
            }
        }
        if(targetGirl != null) {
            boyQueue.remove(sourceBoy);
            girlQueue.remove(targetGirl);
            sourceBoy.targetConRef.set(targetGirl);
            targetGirl.targetConRef.set(sourceBoy);
        }
    }

    public void compareBoyList() {
        ServerConnection sourceGirl = girlQueue.get(0);
        ServerConnection targetBoy = null;
        double minDist = Double.MAX_VALUE;
        int highScore = 0;
        int tempScore = 0;
        double tempDist = 0;
        for(ServerConnection x : boyQueue) {
            tempScore = 0;
            tempDist = Coordination.calDist(sourceGirl.userAcc, x.userAcc);
            for(String interest1 : sourceGirl.userAcc.interest) {
                for(String interest2 : x.userAcc.interest) {
                    if(interest1.equals(interest2)) {
                        tempScore++;
                    }
                }
            }
            if(tempDist <= minDist) {
                if(tempScore >= highScore) {
                    minDist = tempDist;
                    highScore = tempScore;
                    targetBoy = x;
                }
            }
        }
        if(targetBoy != null) {
            boyQueue.remove(targetBoy);
            girlQueue.remove(sourceGirl);
            sourceGirl.targetConRef.set(targetBoy);
            targetBoy.targetConRef.set(sourceGirl);
        }
    }

    public void pairingHomoBoyService() {
        Runnable runnable = () -> {
            while(!Thread.interrupted()) {
                try {
                    if(homoBoyQueue.size() > 1) {
                        ServerConnection sourceBoy = homoBoyQueue.get(0);
                        ServerConnection targetBoy = null;
                        double minDist = Double.MAX_VALUE;
                        int highScore = 0;
                        int tempScore = 0;
                        double tempDist = 0;
                        for(ServerConnection x : homoBoyQueue) {
                            if(x.equals(sourceBoy)) {
                                continue;
                            }
                            tempScore = 0;
                            tempDist = Coordination.calDist(sourceBoy.userAcc, x.userAcc);
                            for(String interest1 : sourceBoy.userAcc.interest) {
                                for(String interest2 : x.userAcc.interest) {
                                    if(interest1.equals(interest2)) {
                                        tempScore++;
                                    }
                                }
                            }
                            if(tempDist <= minDist) {
                                if(tempScore >= highScore) {
                                    minDist = tempDist;
                                    highScore = tempScore;
                                    targetBoy = x;
                                }
                            }
                        }
                        if(targetBoy != null) {
                            homoBoyQueue.remove(targetBoy);
                            homoBoyQueue.remove(sourceBoy);
                            sourceBoy.targetConRef.set(targetBoy);
                            targetBoy.targetConRef.set(sourceBoy);
                        }
                    }
                }
                catch(Exception e) {
                    Debug.println("Notice from ServerMain pairingHomoBoylService():");
                    Debug.println(e.toString());
                }
            }
        };
        pairingHomoBoyService_thread = new Thread(runnable);
        pairingHomoBoyService_thread.start();
    }

    public void pairingHomoGirllService() {
        Runnable runnable = () -> {
            while(!Thread.interrupted()) {
                try {
                    if(homoGirlQueue.size() > 1) {
                        ServerConnection sourceGirl = homoGirlQueue.get(0);
                        ServerConnection targetGirl = null;
                        double minDist = Double.MAX_VALUE;
                        int highScore = 0;
                        int tempScore = 0;
                        double tempDist = 0;
                        for(ServerConnection x : homoGirlQueue) {
                            if(x.equals(sourceGirl)) {
                                continue;
                            }
                            tempScore = 0;
                            tempDist = Coordination.calDist(sourceGirl.userAcc, x.userAcc);
                            for(String interest1 : sourceGirl.userAcc.interest) {
                                for(String interest2 : x.userAcc.interest) {
                                    if(interest1.equals(interest2)) {
                                        tempScore++;
                                    }
                                }
                            }
                            if(tempDist <= minDist) {
                                if(tempScore >= highScore) {
                                    minDist = tempDist;
                                    highScore = tempScore;
                                    targetGirl = x;
                                }
                            }
                        }
                        if(targetGirl != null) {
                            homoGirlQueue.remove(targetGirl);
                            homoGirlQueue.remove(sourceGirl);
                            sourceGirl.targetConRef.set(targetGirl);
                            targetGirl.targetConRef.set(sourceGirl);
                        }
                    }
                }
                catch(Exception e) {
                    Debug.println("Notice from ServerMain pairingHomoBoylService():");
                    Debug.println(e.toString());
                }
            }
        };
        pairingHomoGirlService_thread = new Thread(runnable);
        pairingHomoGirlService_thread.start();
    }

    public static synchronized String getNewUserID() {
        if(!userList.isEmpty()) {
            try {
                String lastID = "0";
                for(Iterator<UserAcc> i = userList.iterator(); i.hasNext();) {
                    lastID = i.next().userID;
                }
                latestUserID = Integer.parseInt(lastID);
                latestUserID++;
                return String.format("%05d", latestUserID);

            }
            catch(Exception e) {
                Debug.println("Notice from ServerMain getLatestUserID():");
                Debug.print(e.toString());
            }
        }
        return null;
    }

    /**
     * To shut down the server.
     */
    public void shutdown() {
        try {
            receiveRequstService_thread.interrupt();
            pairingBoyGirlService_thread.interrupt();
            pairingHomoBoyService_thread.interrupt();
            pairingHomoGirlService_thread.interrupt();
            ServerFileStoring.exportUserListObjFile(userList);
            input.close();
            server.close();
        }
        catch(IOException e) {
            Debug.println("Notice from ServerMain shutdown():");
            Debug.println(e.getMessage());
        }
    }

    /**
     * To print message from server with date and timestamp
     *
     * @param msg
     */
    public static void printMsg(Object msg) {
        String stamp = dateTimeFormat.format(new Date());
        System.out.println("[" + stamp + "] " + msg);
    }

}
