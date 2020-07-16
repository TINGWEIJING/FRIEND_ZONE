package friendzone;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import friendzone.Debug;
import friendzone.Encryption;
import friendzone.ServerMain;
import friendzone.TanTanAcc;
import friendzone.TinderAcc;
import friendzone.UserAcc;
import friendzone.UserInterestComparator;

/**
 * To handle connections from clients trying to connect the server.<br>
 * https://www.codejava.net/concurrency-tutorials<br>
 *
 * @author TING WEI JING
 */
public class ServerConnection implements Runnable {

    // client socket
    private Socket clientSocket;
    public AtomicReference<ServerConnection> targetConRef = new AtomicReference<>();
    private AtomicBoolean confirmFromServer = new AtomicBoolean(false);
    private AtomicBoolean quitCondition = new AtomicBoolean(false);
    // target request paired up by server
    private DataOutputStream sender;
    private DataInputStream receiver;
    private String userName;
    public UserAcc userAcc;

    private Random rdm = new Random();
    public CopyOnWriteArrayList<ServerConnection> incomingUserList = new CopyOnWriteArrayList<>();
    public PriorityQueue<UserInterestComparator> nearbyUserQueue = new PriorityQueue<>();
    public ArrayList<UserInterestComparator> nearbyUserArr = new ArrayList<>();

    private Thread mainService_thread;
    private Thread forwardingMsg_thread;
    private Thread responding_thread;

    public static int PRIORQ_SIZE = 20;

    public static final String DISCONNET_QUE = "-quit";
    public static final String LOGIN_QUE = "1";
    public static final String REGISTER_QUE = "2";
    public static final String NEWACC_QUE = "3";
    public static final String SEARCHPARTNER_QUE = "4";
    public static final String FINDNEARBY_QUE = "5";
    public static final String INSTANTCHAT_QUE = "6";
    public static final String UPDATEINFO_QUE = "7";
    public static final String RETANTAN_QUE = "8";
    public static final String RETINDER_QUE = "9";
    public static final String SELFLEAVE_QUE = "-leave";
    public static final String PARTNERLEAVE_QUE = "-partneroffline";

    public ServerConnection(Socket client) {
        try {
            // create a receiving socket
            clientSocket = client;
            sender = new DataOutputStream(clientSocket.getOutputStream());
            receiver = new DataInputStream(clientSocket.getInputStream());
            userName = rdm.nextInt(1000) * 1000 + "";
        }
        catch(IOException e) {
            Debug.println("Notice from ServerConnection constructor:");
            ServerMain.printMsg("{" + userName + "} " + e.getMessage());
        }
    }

    /**
     * May implement using executer in future<br>
     */
    @Override
    public void run() {
        String inputFromClient;
        try {
            while(!Thread.interrupted()) {
                checkIncomingInstChatSubService();
                inputFromClient = readUserInput();
                Debug.println("Receive " + inputFromClient + " from {" + userName + "}");
                if(inputFromClient.equals(LOGIN_QUE)) {
                    authSubService();
                }
                else if(inputFromClient.equals(REGISTER_QUE)) {
                    registerNewUserSubService();
                }
                else if(inputFromClient.equals(RETANTAN_QUE)) {
                    registerWithTanTanSubService();
                }
                else if(inputFromClient.equals(RETINDER_QUE)) {
                    registerWithTinderSubService();
                }
                else if(inputFromClient.equals(SEARCHPARTNER_QUE)) {
                    searchPartnerSubService();
                    chattingService();
                    targetConRef.set(null);
                    ServerMain.onlineMap.put(userAcc, this);
                }
                else if(inputFromClient.equals(FINDNEARBY_QUE)) {
                    updateUserLocation();
                    findNearbyUserSubService();
                }
                else if(inputFromClient.equals(INSTANTCHAT_QUE)) {
                    instantChatService();
                    targetConRef.set(null);
                    ServerMain.onlineMap.put(userAcc, this);
                }
                else if(inputFromClient.equals(UPDATEINFO_QUE)) {
                    while(receiver.readBoolean()) {
                        updateUserAccData();
                    }
                }
            }
        }
        catch(Exception e) {
            Debug.println("Notice from ServerConnection run():");
            ServerMain.printMsg("{" + userName + "} " + e.toString());
        }
        finally {
            ServerMain.printMsg("{" + userName + "} logout");
            killConnection();
        }
    }

    public void connectionService() {
        mainService_thread = new Thread(this);
        mainService_thread.start();
    }

    /**
     * To run the forwarding thread.
     *
     * @param target target request paired up by server
     */
    public void chattingService() {
        ServerConnection targetConnection = targetConRef.get();
        Runnable runForwarding = () -> {
            String receivedMsg = null;
            try {
                while(!Thread.interrupted() && targetConRef.get() != null) {
                    if(targetConRef.get().targetConRef.get() == null) {
                        break;
                    }
                    receivedMsg = receiver.readUTF();
                    Debug.println("{" + userName + "} " + receivedMsg);
                    if(receivedMsg.equals(ServerConnection.SELFLEAVE_QUE)) {
                        sender.writeUTF(Encryption.encrypt(ServerConnection.SELFLEAVE_QUE, Encryption.key));
                        targetConnection.sender.writeUTF(Encryption.encrypt(ServerConnection.PARTNERLEAVE_QUE, Encryption.key));
                        targetConnection.forwardingMsg_thread.interrupt();
                        targetConRef.set(null);
                        targetConnection.targetConRef.set(null);
                        forwardingMsg_thread.interrupt();
                        break;
                    }
                    else if(receivedMsg.equals(ServerConnection.PARTNERLEAVE_QUE)) {
                        targetConRef.set(null);
                        break;
                    }
                    else {
                        targetConnection.sender.writeUTF(receivedMsg);
                    }
                }
            }
            catch(IOException e) {
                Debug.println("Notice from ServerConnection chattingService():");
                ServerMain.printMsg("{" + userName + "} " + e.toString());
            }
            finally {
                ServerMain.printMsg("{" + this.userName + "} leave the chat");
            }
        };
        forwardingMsg_thread = new Thread(runForwarding);
        forwardingMsg_thread.start();
        while(targetConRef.get() != null) {
            try {
                Thread.sleep(100);
            }
            catch(InterruptedException e) {
                break;
            }
        }
    }

    public void instantChatService() {
        confirmFromServer.set(false);
        quitCondition.set(false);
        Runnable respondingRun = () -> {
            try {
                String rec = receiver.readUTF();
                if(rec.equals(DISCONNET_QUE)) {
                    if(!confirmFromServer.get()) {
                        quitCondition.set(true);
                        confirmFromServer.set(false);
                        responding_thread.interrupt();
                    }
                }
            }
            catch(Exception e) {
                Debug.println("Notice from ServerConnection instantChatService():");
                ServerMain.printMsg("{" + userName + "} " + e.toString());
            }
            finally {
            }
        };
        responding_thread = new Thread(respondingRun);
        try {
            String targetUsername = receiver.readUTF();
            UserAcc tempUserAcc;
            responding_thread.start();
            ServerMain.printMsg("{" + userName + "} sends instant chat invitation to {" + targetUsername + "}");
            while(!quitCondition.get()) {
                for(Iterator<UserAcc> i = ServerMain.userList.iterator(); i.hasNext();) {
                    tempUserAcc = i.next();
                    if(targetUsername.equals(tempUserAcc.userName)) {
                        targetConRef.set(ServerMain.onlineMap.get(tempUserAcc));
                        break;
                    }
                }
                if(targetConRef.get() != null) {
                    targetConRef.get().incomingUserList.add(this);
                    break;
                }
            }

            while(!quitCondition.get()) {
                if(targetConRef.get() != null && ServerMain.onlineMap.contains(targetConRef.get())) {
                    if(!targetConRef.get().incomingUserList.contains(this)) {
                        confirmFromServer.set(false);
                        ServerMain.printMsg("{" + userName + "} instant chat partner reject");
                        break;
                    }
                    else if(targetConRef.get().targetConRef.get() != null) {
                        if(targetConRef.get().targetConRef.get().equals(this)) {
                            sender.writeBoolean(true);
                            confirmFromServer.set(true);
                            ServerMain.printMsg("{" + userName + "} instant chat partner accept");
                            break;
                        }
                    }
                }
                else {
                    confirmFromServer.set(false);
                    ServerMain.printMsg("{" + userName + "} instant chat partner not online");
                    break;
                }
            }
            if(confirmFromServer.get() && targetConRef.get() != null) {
                responding_thread.interrupt();
                while(responding_thread.isAlive()) {
                    //make sure thread is kill
                }
                ServerMain.printMsg("{" + userName + "} is instant chat with {" + targetUsername + "}");
                sendTargetUserAccData();
                chattingService();
            }
            else {
                responding_thread.interrupt();
                sender.writeBoolean(false); //important for quit case
                while(responding_thread.isAlive()) {
                    //make sure thread is kill
                }
                if(targetConRef.get() != null) {
                    if(targetConRef.get().incomingUserList.remove(this)) {

                    }
                }
                ServerMain.printMsg("{" + userName + "}'s invitation rejected");
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            ServerMain.printMsg("{" + userName + "} " + e.toString());
            ServerMain.printMsg("{" + userName + "} disconnect");
            killConnection();
        }
    }

    public void checkIncomingInstChatSubService() {
        try {
            if(!incomingUserList.isEmpty()) {
                sender.writeBoolean(true);
                Debug.println("Send true to {" + userName + "}");
                ServerConnection tmpCon = incomingUserList.get(0);
                sendIncomingUserAccData(tmpCon.userAcc);
                if(receiver.readBoolean()) {
                    Debug.println("Receive true from {" + userName + "}");
                    if(!incomingUserList.isEmpty()) {
                        if(incomingUserList.contains(tmpCon)) {
                            sender.writeBoolean(true);
                            Debug.println("Send true to {" + userName + "}");
                            this.targetConRef.set(incomingUserList.get(0));
                            chattingService();
                            incomingUserList.remove(0);
                        }
                        else {
                            sender.writeBoolean(false);
                            Debug.println("Send false to {" + userName + "}");
                        }
                    }
                    else {
                        sender.writeBoolean(false);
                        Debug.println("Send false to {" + userName + "}");
                    }
                }
                else {
                    Debug.println("Receive false from {" + userName + "}");
                    incomingUserList.remove(0);
                    ServerMain.printMsg("{" + userName + "} reject a invitation");
                }
            }
            else {
                sender.writeBoolean(false);
                Debug.println("Send false to {" + userName + "}");
            }
        }
        catch(IOException e) {
            Debug.println("Notice from ServerConnection checkIncomingInstChatSubService():");
            ServerMain.printMsg("{" + userName + "} " + e.toString());
        }
        this.targetConRef.set(null);
    }

    public void authSubService() {
        String inputFromUser;
        UserAcc userAcc = null;
        boolean hasUserAcc = false;
        boolean isPassCorrect = false;

        try {
            userNameLoop:
            while(!Thread.interrupted() && !hasUserAcc) {
                hasUserAcc = false;
                inputFromUser = readUserInput();
                for(Iterator<UserAcc> i = ServerMain.userList.iterator(); i.hasNext();) {
                    userAcc = i.next();
                    if(userAcc.userName.equals(inputFromUser)) {
                        hasUserAcc = true;
                        sender.writeBoolean(hasUserAcc);
                        break userNameLoop;
                    }
                    else if(userAcc.email.equals(inputFromUser)) {
                        hasUserAcc = true;
                        sender.writeBoolean(hasUserAcc);
                        break userNameLoop;
                    }
                }
                sender.writeBoolean(hasUserAcc);
            }
            while(!Thread.interrupted() && hasUserAcc && userAcc != null) {
                isPassCorrect = false;
                inputFromUser = readUserInput();
                if(userAcc.password.equals(inputFromUser)) {
                    isPassCorrect = true;
                    sender.writeBoolean(isPassCorrect);
                    this.userAcc = userAcc;
                    ServerMain.onlineMap.put(userAcc, this);
                    ServerMain.printMsg("{" + this.userName + "} has changed to {" + this.userAcc.userName + "}");
                    this.userName = this.userAcc.userName;
                    transferUserAccData();
                    ServerMain.printMsg("{" + this.userName + "} logined");
                    break;
                }
                else {
                    sender.writeBoolean(isPassCorrect);
                }
            }
        }
        catch(Exception e) {
            Debug.println("Notice from ServerConnection authSubService():");
            ServerMain.printMsg("{" + userName + "} " + e.toString());
            ServerMain.printMsg("{" + userName + "} disconnect");
            killConnection();
        }
    }

    public void searchPartnerSubService() {
        confirmFromServer.set(false);
        Runnable respondingRun = () -> {
            try {
                while(!Thread.interrupted()) {
                    if(targetConRef.get() != null || confirmFromServer.get()) {
                        confirmFromServer.set(true);
                        sender.writeBoolean(true);
                        break;
                    }
                }
            }
            catch(Exception e) {
            }
        };
        responding_thread = new Thread(respondingRun);
        try {
            updateUserLocation();
            ServerMain.onlineMap.remove(this.userAcc);
            if(this.userAcc.gender == 'M' && this.userAcc.genderPreference == 'F') {
                ServerMain.boyQueue.add(this);
                ServerMain.printMsg("{" + userName + "} waiting in boy queue");
            }
            else if(this.userAcc.gender == 'F' && this.userAcc.genderPreference == 'M') {
                ServerMain.girlQueue.add(this);
                ServerMain.printMsg("{" + userName + "} waiting in girl queue");
            }
            else if(this.userAcc.gender == 'M' && this.userAcc.genderPreference == 'M') {
                ServerMain.homoBoyQueue.add(this);
                ServerMain.printMsg("{" + userName + "} waiting in homo boy queue");
            }
            else {
                ServerMain.homoGirlQueue.add(this);
                ServerMain.printMsg("{" + userName + "} waiting in homo girl queue");
            }
            ServerMain.printMsg("{" + userName + "} pending for partner");
            responding_thread.start();
            while(!confirmFromServer.get()) {
                if(receiver.readUTF().equals(DISCONNET_QUE)) {
                    if(!confirmFromServer.get()) {
                        sender.writeBoolean(false);
                        targetConRef.set(null);
                        ServerMain.boyQueue.remove(this);
                        ServerMain.girlQueue.remove(this);
                        ServerMain.homoBoyQueue.remove(this);
                        ServerMain.homoGirlQueue.remove(this);
                        responding_thread.interrupt();
                        return;
                    }
                }
            }
            ServerMain.boyQueue.remove(this);
            ServerMain.girlQueue.remove(this);
            ServerMain.homoBoyQueue.remove(this);
            ServerMain.homoGirlQueue.remove(this);
            ServerMain.printMsg("{" + userName + "} pairs with {" + targetConRef.get().userName + "}");
            sendTargetUserAccData();
        }
        catch(IOException e) {
            Debug.println("Notice from ServerConnection searchPartnerSubService():");
            ServerMain.printMsg("{" + userName + "} " + e.toString());
            ServerMain.printMsg("{" + userName + "} disconnect");
            killConnection();
        }
        finally {
            ServerMain.boyQueue.remove(this);
            ServerMain.girlQueue.remove(this);
            ServerMain.homoBoyQueue.remove(this);
            ServerMain.homoGirlQueue.remove(this);
        }
    }

    public void findNearbyUserSubService() {
        UserInterestComparator tempUser;
        ServerMain.printMsg("{" + userName + "} is searching nearby user");
        if(this.userAcc.gender == 'M' && this.userAcc.genderPreference == 'F') {
            for(UserAcc x : ServerMain.girlList) {
                tempUser = new UserInterestComparator(this.userAcc, x);
                if(!x.equals(this.userAcc) && tempUser.score >= UserInterestComparator.penalty) {
                    this.nearbyUserQueue.add(tempUser);
                }
                if(nearbyUserQueue.size() > PRIORQ_SIZE) {
                    this.nearbyUserQueue.remove();
                }
            }
        }
        else if(this.userAcc.gender == 'F' && this.userAcc.genderPreference == 'M') {
            for(UserAcc x : ServerMain.boyList) {
                tempUser = new UserInterestComparator(this.userAcc, x);
                if(!x.equals(this.userAcc) && tempUser.score >= UserInterestComparator.penalty) {
                    this.nearbyUserQueue.add(tempUser);
                }
                if(nearbyUserQueue.size() > PRIORQ_SIZE) {
                    this.nearbyUserQueue.remove();
                }
            }
        }
        else if(this.userAcc.gender == 'M' && this.userAcc.genderPreference == 'M') {
            for(UserAcc x : ServerMain.homoBoyList) {
                tempUser = new UserInterestComparator(this.userAcc, x);
                if(!x.equals(this.userAcc) && tempUser.score >= UserInterestComparator.penalty) {
                    this.nearbyUserQueue.add(new UserInterestComparator(this.userAcc, x));
                }
                if(nearbyUserQueue.size() > PRIORQ_SIZE) {
                    this.nearbyUserQueue.remove();
                }
            }
        }
        else {
            for(UserAcc x : ServerMain.homoGirlList) {
                tempUser = new UserInterestComparator(this.userAcc, x);
                if(!x.equals(this.userAcc) && tempUser.score >= UserInterestComparator.penalty) {
                    this.nearbyUserQueue.add(new UserInterestComparator(this.userAcc, x));
                }
                if(nearbyUserQueue.size() > PRIORQ_SIZE) {
                    this.nearbyUserQueue.remove();
                }
            }
        }
        while(!nearbyUserQueue.isEmpty()) {
            nearbyUserArr.add(nearbyUserQueue.remove());
        }
        Collections.sort(nearbyUserArr, Collections.reverseOrder());
        try {
            sender.writeBoolean(true);
            sender.writeInt(nearbyUserArr.size());
            receiver.readUTF();
        }
        catch(IOException e) {
            killConnection();
        }
        for(int i = 0; i < nearbyUserArr.size(); i++) {
            sendTargetUserAccData(nearbyUserArr.get(i).partnerUserAcc);
            try {
                if(receiver.readUTF().equals(ServerConnection.SELFLEAVE_QUE)) {
                    break;
                }
            }
            catch(IOException ex) {
                killConnection();
            }
        }
        nearbyUserArr.clear();
        nearbyUserQueue.clear();
        ServerMain.printMsg("{" + userName + "} leaves searching nearby user");
    }

    public boolean transferUserAccData() {
        try {
            sender.writeUTF(this.userAcc.userID);
            sender.writeUTF(this.userAcc.userName);
            sender.writeUTF(this.userAcc.password);
            sender.writeUTF(this.userAcc.email);
            sender.writeChar(this.userAcc.gender);
            sender.writeChar(this.userAcc.genderPreference);
            sender.writeInt(this.userAcc.age);
            sender.writeUTF(this.userAcc.country);
            sender.writeUTF(this.userAcc.state);
            sender.writeUTF(this.userAcc.postcode);
            sender.writeDouble(this.userAcc.latitude);
            sender.writeDouble(this.userAcc.longitude);
            for(int i = 0; i < UserAcc.INTEREST_SIZE; i++) {
                sender.writeUTF(this.userAcc.interest[i]);
            }
            return true;
        }
        catch(IOException e) {
            Debug.println("Notice from ServerConnection transferUserAccData():");
            ServerMain.printMsg("{" + userName + "} " + e.toString());
            ServerMain.printMsg("{" + userName + "} download data error");
            ServerMain.printMsg("{" + userName + "} disconnect");
            killConnection();
            return false;
        }
    }

    public boolean transferTinderUserAccData(TinderAcc TinderUser) {
        try {
            sender.writeUTF(TinderUser.userName);
            sender.writeUTF(TinderUser.password);
            sender.writeUTF(TinderUser.email);
            sender.writeInt(TinderUser.age);
            sender.writeChar(TinderUser.gender);
            sender.writeChar(TinderUser.genderPreference);
            return true;
        }
        catch(IOException e) {
            Debug.println("Notice from ServerConnection transferTinderUserAccData():");
            ServerMain.printMsg("{" + userName + "} " + e.toString());
            ServerMain.printMsg("{" + userName + "} download data error");
            ServerMain.printMsg("{" + userName + "} disconnect");
            killConnection();
            return false;
        }
    }

    public boolean transferTanTanUserAccData(TanTanAcc TinderUser) {
        try {
            sender.writeUTF(TinderUser.userName);
            sender.writeUTF(TinderUser.password);
            sender.writeInt(TinderUser.age);
            sender.writeChar(TinderUser.gender);
            sender.writeChar(TinderUser.genderPreference);
            return true;
        }
        catch(IOException e) {
            Debug.println("Notice from ServerConnection transferTinderUserAccData():");
            ServerMain.printMsg("{" + userName + "} " + e.toString());
            ServerMain.printMsg("{" + userName + "} download data error");
            ServerMain.printMsg("{" + userName + "} disconnect");
            killConnection();
            return false;
        }
    }

    public boolean sendTargetUserAccData() {
        UserAcc targetUser = this.targetConRef.get().userAcc;
        try {
            sender.writeUTF(targetUser.userID);
            sender.writeUTF(targetUser.userName);
            sender.writeUTF(targetUser.email);
            sender.writeChar(targetUser.gender);
            sender.writeChar(targetUser.genderPreference);
            sender.writeInt(targetUser.age);
            sender.writeUTF(targetUser.country);
            sender.writeUTF(targetUser.state);
            sender.writeDouble(targetUser.latitude);
            sender.writeDouble(targetUser.longitude);
            for(int i = 0; i < UserAcc.INTEREST_SIZE; i++) {
                sender.writeUTF(targetUser.interest[i]);
            }
            return true;
        }
        catch(IOException e) {
            Debug.println("Notice from ServerConnection sendTargetUserAccData():");
            ServerMain.printMsg("{" + userName + "} " + e.toString());
            ServerMain.printMsg("{" + userName + "} download data error");
            ServerMain.printMsg("{" + userName + "} disconnect");
            killConnection();
            return false;
        }
    }

    public boolean sendIncomingUserAccData(UserAcc userAcc) {
        UserAcc targetUser = userAcc;
        try {
            sender.writeUTF(targetUser.userID);
            sender.writeUTF(targetUser.userName);
            sender.writeUTF(targetUser.email);
            sender.writeChar(targetUser.gender);
            sender.writeChar(targetUser.genderPreference);
            sender.writeInt(targetUser.age);
            sender.writeUTF(targetUser.country);
            sender.writeUTF(targetUser.state);
            sender.writeDouble(targetUser.latitude);
            sender.writeDouble(targetUser.longitude);
            for(int i = 0; i < UserAcc.INTEREST_SIZE; i++) {
                sender.writeUTF(targetUser.interest[i]);
            }
            return true;
        }
        catch(IOException e) {
            Debug.println("Notice from ServerConnection sendIncomingUserAccData():");
            ServerMain.printMsg("{" + userName + "} " + e.toString());
            ServerMain.printMsg("{" + userName + "} download data error");
            ServerMain.printMsg("{" + userName + "} disconnect");
            killConnection();
            return false;
        }
    }

    public boolean sendTargetUserAccData(UserAcc targetUser) {
        try {
            sender.writeUTF(targetUser.userID);
            sender.writeUTF(targetUser.userName);
            sender.writeUTF(targetUser.email);
            sender.writeChar(targetUser.gender);
            sender.writeChar(targetUser.genderPreference);
            sender.writeInt(targetUser.age);
            sender.writeUTF(targetUser.country);
            sender.writeUTF(targetUser.state);
            sender.writeDouble(targetUser.latitude);
            sender.writeDouble(targetUser.longitude);
            for(int i = 0; i < UserAcc.INTEREST_SIZE; i++) {
                sender.writeUTF(targetUser.interest[i]);
            }
            return true;
        }
        catch(IOException e) {
            Debug.println("Notice from ServerConnection sendTargetUserAccData():");
            ServerMain.printMsg("{" + userName + "} " + e.toString());
            ServerMain.printMsg("{" + userName + "} download data error");
            ServerMain.printMsg("{" + userName + "} disconnect");
            killConnection();
            return false;
        }
    }

    public boolean updateUserAccData() {
        try {
            this.userAcc.userID = receiver.readUTF();
            this.userAcc.userName = receiver.readUTF();
            this.userAcc.password = receiver.readUTF();
            this.userAcc.email = receiver.readUTF();
            this.userAcc.gender = receiver.readChar();
            this.userAcc.genderPreference = receiver.readChar();
            this.userAcc.age = receiver.readInt();
            this.userAcc.country = receiver.readUTF();
            this.userAcc.state = receiver.readUTF();
            this.userAcc.postcode = receiver.readUTF();
            this.userAcc.latitude = receiver.readDouble();
            this.userAcc.longitude = receiver.readDouble();
            for(int i = 0; i < UserAcc.INTEREST_SIZE; i++) {
                this.userAcc.interest[i] = receiver.readUTF();
            }
            ServerMain.printMsg("{" + this.userName + "} data updated");
            return true;
        }
        catch(IOException e) {
            Debug.println("Notice from ServerConnection updateUserAccData():");
            ServerMain.printMsg("{" + userName + "} " + e.toString());
            ServerMain.printMsg("{" + userName + "} disconnect");
            killConnection();
            return false;
        }
    }

    public void registerNewUserSubService() {
        quitCondition.set(false);
        boolean gotUsername = false;
        try {
            while(!quitCondition.get()) {
                if(receiver.readBoolean()) {
                    String name = receiver.readUTF();
                    gotUsername = false;
                    for(Iterator<UserAcc> i = ServerMain.userList.iterator(); i.hasNext();) {
                        String tmp = i.next().userName;
                        if(tmp.equals(name)) {
                            gotUsername = true;
                            break;
                        }
                    }
                    sender.writeBoolean(!gotUsername);
                    if(!gotUsername) {
                        quitCondition.set(false);
                        break;
                    }
                }
                else {
                    quitCondition.set(true);
                    break;
                }
            }
            if(!quitCondition.get()) {
                if(receiver.readBoolean()) {
                    createNewUserAccData();
                }
            }
        }
        catch(Exception e) {
            Debug.println("Notice from ServerConnection registerNewUserSubService():");
            ServerMain.printMsg("{" + userName + "} " + e.toString());
            ServerMain.printMsg("{" + userName + "} disconnect");
            killConnection();
        }
        finally {
            quitCondition.set(false);
        }
    }

    public void registerWithTinderSubService() {
        String inputFromUser;
        TinderAcc currTinderUser = null;
        boolean hasUserAcc = false; // has this user in Tinder database
        boolean isPassCorrect = false;
        boolean userRepeat = true;
        try {
            userNameLoop:
            while(!Thread.interrupted() && userRepeat) {
                hasUserAcc = false;
                userRepeat = true;
                inputFromUser = readUserInput();
                for(Iterator<TinderAcc> i = ServerMain.TinderUserList.iterator(); i.hasNext();) {
                    currTinderUser = i.next();
                    if(currTinderUser.phoneNumber.equals(inputFromUser)) {
                        hasUserAcc = true;
                        break;
                    }
                    else if(currTinderUser.email.equals(inputFromUser)) {
                        hasUserAcc = true;
                        break;
                    }
                    else if(currTinderUser.userName.equals(inputFromUser)) {
                        hasUserAcc = true;
                        break;
                    }
                }
                if(!hasUserAcc) {
                    sender.writeBoolean(false);
                    continue;
                }
                else {
                    userRepeat = false;
                }
                for(Iterator<UserAcc> i = ServerMain.userList.iterator(); i.hasNext();) {
                    String tmp = i.next().userName;
                    if(tmp.equals(currTinderUser.userName)) {
                        userRepeat = true;
                        sender.writeBoolean(false);
                        break;
                    }
                }
                if(!userRepeat) {
                    sender.writeBoolean(true);
                    break;
                }
            }
            while(!Thread.interrupted() && hasUserAcc && currTinderUser != null) {
                isPassCorrect = false;
                inputFromUser = readUserInput();
                if(currTinderUser.password.equals(inputFromUser)) {
                    isPassCorrect = true;
                    sender.writeBoolean(isPassCorrect);
                    transferTinderUserAccData(currTinderUser);
                    break;
                }
                else {
                    sender.writeBoolean(isPassCorrect);
                }
            }
            if(receiver.readBoolean()) { // agree to create a new acc ?
                if(receiver.readBoolean()) {
                    createNewUserAccData();
                }
            }
        }
        catch(Exception e) {
            Debug.println("Notice from ServerConnection registerWithTinderSubService():");
            ServerMain.printMsg("{" + userName + "} " + e.toString());
            ServerMain.printMsg("{" + userName + "} disconnect");
            killConnection();
        }
    }

    public void registerWithTanTanSubService() {
        String inputFromUser;
        TanTanAcc currTanTanAcc = null;
        boolean hasUserAcc = false; // has this user in Tinder database
        boolean isPassCorrect = false;
        boolean userRepeat = true;
        try {
            userNameLoop:
            while(!Thread.interrupted() && userRepeat) {
                hasUserAcc = false;
                userRepeat = true;
                inputFromUser = readUserInput();
                for(Iterator<TanTanAcc> i = ServerMain.TanTanUserList.iterator(); i.hasNext();) {
                    currTanTanAcc = i.next();
                    if(currTanTanAcc.phoneNumber.equals(inputFromUser)) {
                        hasUserAcc = true;
                        break;
                    }
                    else if(currTanTanAcc.userName.equals(inputFromUser)) {
                        hasUserAcc = true;
                        break;
                    }
                }
                if(!hasUserAcc) {
                    sender.writeBoolean(false);
                    continue;
                }
                else {
                    userRepeat = false;
                }
                for(Iterator<UserAcc> i = ServerMain.userList.iterator(); i.hasNext();) {
                    String tmp = i.next().userName;
                    if(tmp.equals(currTanTanAcc.userName)) {
                        userRepeat = true;
                        sender.writeBoolean(false);
                        break;
                    }
                }
                if(!userRepeat) {
                    sender.writeBoolean(true);
                    break;
                }
            }
            while(!Thread.interrupted() && hasUserAcc && currTanTanAcc != null) {
                isPassCorrect = false;
                inputFromUser = readUserInput();
                if(currTanTanAcc.password.equals(inputFromUser)) {
                    isPassCorrect = true;
                    sender.writeBoolean(isPassCorrect);
                    transferTanTanUserAccData(currTanTanAcc);
                    break;
                }
                else {
                    sender.writeBoolean(isPassCorrect);
                }
            }
            if(receiver.readBoolean()) { // agree to create a new acc ?
                if(receiver.readBoolean()) {
                    createNewUserAccData();
                }
            }
        }
        catch(Exception e) {
            Debug.println("Notice from ServerConnection registerWithTanTanSubService():");
            ServerMain.printMsg("{" + userName + "} " + e.toString());
            ServerMain.printMsg("{" + userName + "} disconnect");
            killConnection();
        }
    }

    public boolean createNewUserAccData() {
        try {
            this.userAcc = new UserAcc(ServerMain.getNewUserID(), receiver.readUTF(), receiver.readUTF(), receiver.readUTF());
            this.userAcc.gender = receiver.readChar();
            this.userAcc.genderPreference = receiver.readChar();
            this.userAcc.age = receiver.readInt();
            this.userAcc.country = receiver.readUTF();
            this.userAcc.state = receiver.readUTF();
            this.userAcc.postcode = receiver.readUTF();
            this.userAcc.latitude = receiver.readDouble();
            this.userAcc.longitude = receiver.readDouble();
            for(int i = 0; i < UserAcc.INTEREST_SIZE; i++) {
                this.userAcc.interest[i] = receiver.readUTF();
            }
            ServerMain.userList.add(this.userAcc);
            ServerMain.printMsg("{" + this.userAcc.userName + "} new user created");
            if(this.userAcc.gender == 'M' && this.userAcc.genderPreference == 'F') {
                ServerMain.boyList.add(this.userAcc);
            }
            else if(this.userAcc.gender == 'F' && this.userAcc.genderPreference == 'M') {
                ServerMain.girlList.add(this.userAcc);
            }
            else if(this.userAcc.gender == 'M' && this.userAcc.genderPreference == 'M') {
                ServerMain.homoBoyList.add(this.userAcc);
            }
            else {
                ServerMain.homoGirlList.add(this.userAcc);
            }
            return true;
        }
        catch(IOException e) {
            Debug.println("Notice from ServerConnection updateUserAccData():");
            ServerMain.printMsg("{" + userName + "} " + e.toString());
            ServerMain.printMsg("{" + userName + "} disconnect");
            killConnection();
            return false;
        }
    }

    public boolean updateUserLocation() {
        try {
            this.userAcc.country = receiver.readUTF();
            this.userAcc.state = receiver.readUTF();
            this.userAcc.postcode = receiver.readUTF();
            this.userAcc.latitude = receiver.readDouble();
            this.userAcc.longitude = receiver.readDouble();
            ServerMain.printMsg("{" + this.userName + "} location updated");
            return true;
        }
        catch(IOException e) {
            Debug.println("Notice from ServerConnection updateUserLocation():");
            ServerMain.printMsg("{" + userName + "} " + e.toString());
            ServerMain.printMsg("{" + userName + "} update location error");
            ServerMain.printMsg("{" + userName + "} disconnect");
            killConnection();
            return false;
        }
    }

    private void killConnection() {
        try {
            this.clientSocket.close();
        }
        catch(IOException e) {

        }
        finally {
            ServerMain.boyQueue.remove(this);
            ServerMain.girlQueue.remove(this);
            ServerMain.homoBoyQueue.remove(this);
            ServerMain.homoGirlQueue.remove(this);
            if(this.userAcc != null) {
                ServerMain.onlineMap.remove(this.userAcc);
            }
            mainService_thread.interrupt();
            this.targetConRef = null;
            this.userAcc = null;
        }
    }

    public String readUserInput() {
        String input = null;
        try {
            input = receiver.readUTF();
            if(DISCONNET_QUE.equals(input)) {
                killConnection();
            }
        }
        catch(IOException ex) {
        }
        return input;
    }

    public String getUserName() {
        return userName;
    }

}
