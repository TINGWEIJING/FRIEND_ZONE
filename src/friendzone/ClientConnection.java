package friendzone;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import friendzone.ClientMain;
import friendzone.Debug;
import friendzone.Encryption;
import friendzone.ServerConnection;
import friendzone.ServerMain;
import friendzone.UserAcc;

/**
 * A class to connect to server
 *
 * @author TING WEI JING
 */
public class ClientConnection {

    private ClientMain clientMain;
    private Socket socket;
    private DataOutputStream sender;
    private DataInputStream receiver;
    private static BufferedReader input;

    static FileOutputStream fos;
    static ObjectOutputStream oos;
    static FileInputStream fis;
    static ObjectInputStream ois;
    static BufferedWriter output;

    private Thread sender_thread;
    private Thread receiver_thread;
    private AtomicBoolean isPartnerOnline = new AtomicBoolean(false);

    private final static String INTEREST_FILENAME = "Interest.csv";
    private final static String COUNTRY_FILENAME = "Country.csv";
    private final static String POSTCODE_FILENAME = "Postcode.csv";
    private final static String OPPOSITE_FILENAME = "Opposite.csv";

    private final static String INTERESET_OBJ_FILENAME = "data/Interest.dat";
    private final static String COUNTRY_OBJ_FILENAME = "data/Country.dat";
    private final static String POSTCODE_OBJ_FILENAME = "data/Postcode.dat";
    private final static String OPPOSITE_OBJ_FILENAME = "data/Opposite.dat";

    private final String hostName = "localhost";
    private static String choice;
    private final int portNumber = ServerMain.port;

    public static ArrayList<String> interestList = new ArrayList<>();
    public static ArrayList<String[]> countryList = new ArrayList<>();
    public static ArrayList<String[]> postcodeList = new ArrayList<>();
    public static HashMap<String, String> oppositeMap = new HashMap<>();

    public ClientConnection(ClientMain clientMain) {
        Encryption.key = Encryption.loadkey();
        this.clientMain = clientMain;
        input = new BufferedReader(new InputStreamReader(System.in));
        interestList = importInterestObjFile();
        countryList = importCountryObjFile();
        postcodeList = importPostcodeObjFile();
        oppositeMap = importOppositeObjFile();
    }

    /**
     * To connect to server.
     *
     * @return true if connected successfully
     */
    public boolean connectServer() {
        try {
            // create a socket to connect to server socket
            socket = new Socket(hostName, ServerMain.port);
            sender = new DataOutputStream(socket.getOutputStream());
            receiver = new DataInputStream(socket.getInputStream());
        }
        catch(UnknownHostException e) {
            Debug.println("Don't know about host " + hostName);
            return false;
        }
        catch(IOException e) {
            Debug.println("Couldn't get I/O for the connection to " + hostName);
            return false;
        }
        return true;
    }

    public boolean reconnectServer() {
        this.clientMain.userAcc = null;
        try {
            socket.close();
        }
        catch(IOException ex) {
            return false;
        }
        return connectServer();
    }

    public void sendToServer(String input) {
        try {
            sender.writeUTF(input);
        }
        catch(IOException e) {
            Debug.println("Send String error");
            Debug.println(e.toString());
        }
    }

    public void sendToServer(char input) {
        try {
            sender.writeChar(input);
        }
        catch(IOException e) {
            Debug.println("Send char error");
            Debug.println(e.toString());
        }
    }

    public void sendToServer(int input) {
        try {
            sender.writeInt(input);
        }
        catch(IOException e) {
            Debug.println("Send int error");
            Debug.println(e.toString());
        }
    }

    public void sendToServer(boolean input) {
        try {
            sender.writeBoolean(input);
        }
        catch(IOException e) {
            Debug.println("Send boolean error");
            Debug.println(e.toString());
        }
    }

    public void sendDisconnectQue() {
        try {
            sender.writeUTF(ServerConnection.DISCONNET_QUE);
        }
        catch(IOException e) {
        }
    }

    public boolean sendLocationToServer() {
        try {
            sender.writeUTF(this.clientMain.userAcc.country);
            sender.writeUTF(this.clientMain.userAcc.state);
            sender.writeUTF(this.clientMain.userAcc.postcode);
            sender.writeDouble(this.clientMain.userAcc.latitude);
            sender.writeDouble(this.clientMain.userAcc.longitude);
            return true;
        }
        catch(IOException e) {
            Debug.println("Send location error");
            return false;
        }
    }

    public boolean downloadUserDataFromServer() {
        try {
            this.clientMain.userAcc = new UserAcc(receiver.readUTF(), receiver.readUTF(), receiver.readUTF(), receiver.readUTF());
            this.clientMain.userAcc.gender = receiver.readChar();
            this.clientMain.userAcc.genderPreference = receiver.readChar();
            this.clientMain.userAcc.age = receiver.readInt();
            this.clientMain.userAcc.country = receiver.readUTF();
            this.clientMain.userAcc.state = receiver.readUTF();
            this.clientMain.userAcc.postcode = receiver.readUTF();
            this.clientMain.userAcc.latitude = receiver.readDouble();
            this.clientMain.userAcc.longitude = receiver.readDouble();
            for(int i = 0; i < UserAcc.INTEREST_SIZE; i++) {
                this.clientMain.userAcc.interest[i] = receiver.readUTF();
            }
            return true;
        }
        catch(IOException e) {
            Debug.println("Download data error");
            return false;
        }
    }

    public UserAcc downloadPartnerUserDataFromServer() {
        UserAcc partnerAcc = null;
        try {
            partnerAcc = new UserAcc(receiver.readUTF(), receiver.readUTF(), "", receiver.readUTF());
            partnerAcc.gender = receiver.readChar();
            partnerAcc.genderPreference = receiver.readChar();
            partnerAcc.age = receiver.readInt();
            partnerAcc.country = receiver.readUTF();
            partnerAcc.state = receiver.readUTF();
            partnerAcc.latitude = receiver.readDouble();
            partnerAcc.longitude = receiver.readDouble();
            for(int i = 0; i < UserAcc.INTEREST_SIZE; i++) {
                partnerAcc.interest[i] = receiver.readUTF();
            }
            return partnerAcc;
        }
        catch(IOException e) {
            Debug.println("Download partner data error");														 
            return partnerAcc;
        }
    }

    public UserAcc downloadTinderUserDataFromServer() {
        UserAcc newUserAcc = null;
        try {
            newUserAcc = new UserAcc("", receiver.readUTF(), receiver.readUTF(), receiver.readUTF());
            newUserAcc.age = receiver.readInt();
            newUserAcc.gender = receiver.readChar();
            newUserAcc.genderPreference = receiver.readChar();
            newUserAcc.country = "";
            newUserAcc.state = "";
            newUserAcc.postcode = "";
            for(int i = 0; i < UserAcc.INTEREST_SIZE; i++) {
                newUserAcc.interest[i] = "";
            }
            return newUserAcc;
        }
        catch(IOException e) {
			Debug.println("Download partner data error");											 
            return newUserAcc;
        }
    }

    public UserAcc downloadTanTanUserDataFromServer() {
        UserAcc newUserAcc = null;
        try {
            newUserAcc = new UserAcc("", receiver.readUTF(), receiver.readUTF(), "");
            newUserAcc.age = receiver.readInt();
            newUserAcc.gender = receiver.readChar();
            newUserAcc.genderPreference = receiver.readChar();
            newUserAcc.country = "";
            newUserAcc.state = "";
            newUserAcc.postcode = "";
            for(int i = 0; i < UserAcc.INTEREST_SIZE; i++) {
                newUserAcc.interest[i] = "";
            }
            return newUserAcc;
        }
        catch(IOException e) {
			Debug.println("Download partner data error");											 
            return newUserAcc;
        }
    }

    public boolean uploadUserDataToServer() {
        try {
            sender.writeUTF(this.clientMain.userAcc.userID);
            sender.writeUTF(this.clientMain.userAcc.userName);
            sender.writeUTF(this.clientMain.userAcc.password);
            sender.writeUTF(this.clientMain.userAcc.email);
            sender.writeChar(this.clientMain.userAcc.gender);
            sender.writeChar(this.clientMain.userAcc.genderPreference);
            sender.writeInt(this.clientMain.userAcc.age);
            sender.writeUTF(this.clientMain.userAcc.country);
            sender.writeUTF(this.clientMain.userAcc.state);
            sender.writeUTF(this.clientMain.userAcc.postcode);
            sender.writeDouble(this.clientMain.userAcc.latitude);
            sender.writeDouble(this.clientMain.userAcc.longitude);
            for(int i = 0; i < UserAcc.INTEREST_SIZE; i++) {
                sender.writeUTF(this.clientMain.userAcc.interest[i]);
            }
            return true;
        }
        catch(IOException e) {
			Debug.println("Upload data error");								   
            return false;
        }
    }

    public boolean uploadNewUserDataToServer(UserAcc newUser) {
        try {
            sender.writeUTF(newUser.userName);
            sender.writeUTF(newUser.password);
            sender.writeUTF(newUser.email);
            sender.writeChar(newUser.gender);
            sender.writeChar(newUser.genderPreference);
            sender.writeInt(newUser.age);
            sender.writeUTF(newUser.country);
            sender.writeUTF(newUser.state);
            sender.writeUTF(newUser.postcode);
            sender.writeDouble(newUser.latitude);
            sender.writeDouble(newUser.longitude);
            for(int i = 0; i < UserAcc.INTEREST_SIZE; i++) {
                sender.writeUTF(newUser.interest[i]);
            }
            return true;
        }
        catch(IOException e) {
			Debug.println("Upload data error");								   
            return false;
        }
    }

    public boolean booleanFromServer() {
        try {
            return receiver.readBoolean();
        }
        catch(IOException e) {
			Debug.println("Read boolean error");									
            Debug.println(e.toString());
            return false;
        }
    }

    public String stringFromServer() {
        try {
            return receiver.readUTF();
        }
        catch(IOException e) {
			Debug.println("Read String error");								   
            Debug.println(e.toString());
            return null;
        }
    }

    public int intFromServer() {
        try {
            return receiver.readInt();
        }
        catch(IOException e) {
			Debug.println("Read int error");								
            Debug.println(e.toString());
            return -1;
        }
    }

    /**
     * To start two new thread.<br>
     * senderThread - a thread to send message to server.<br>
     * receiverThread - a thread to receive message from server.<br>
     * <p>
     * A check "pairing is complete" method will be implemented in future
     */
    public void chattingWithPartner() {
        System.out.println("<YOU CAN START CHATTING NOW, ENTER \"" + ServerConnection.SELFLEAVE_QUE + "\" TO LEAVE CHAT>");
        isPartnerOnline.set(true);
        choice = "";
        Runnable runReceiving = () -> {
            String receivedMsg = null;
            try {
                while(!Thread.interrupted() && isPartnerOnline.get()) {
                    receivedMsg = realReadUTF();
                    if(receivedMsg.equals(ServerConnection.SELFLEAVE_QUE)) {
                        choice = input.readLine();
                        isPartnerOnline.set(false);
                        sender_thread.interrupt();
                        receiver_thread.interrupt();
                        break;
                    }
                    else if(receivedMsg.equals(ServerConnection.PARTNERLEAVE_QUE)) {
                        System.out.print("<" + clientMain.partnerUserAcc.userName
                                         + " LEFT THE CHAT, ");
                        if(clientMain.userAcc.contact.contains(clientMain.partnerUserAcc.userName)) {
                            System.out.println("ENTER ANY KEY TO GO BACK TO FUNCTION MENU>");
                        }
                        else {
                            System.out.println("DO YOU WANT TO SAVE THIS CONTACT? (y/n)>");
                        }
                        sender.writeUTF(ServerConnection.PARTNERLEAVE_QUE);
                        isPartnerOnline.set(false);
                        sender_thread.interrupt();
                        receiver_thread.interrupt();
                        break;
                    }
                    else {
                        System.out.println(clientMain.partnerUserAcc.userName + ": " + receivedMsg);
                    }
                }
            }
            catch(IOException e) {
            }
            finally {
            }
        };
        receiver_thread = new Thread(runReceiving);
        receiver_thread.start();

        Runnable runSending = () -> {
            String sendMsg = null;
            try {
                while(!Thread.interrupted() && isPartnerOnline.get()) {
                    sendMsg = input.readLine();
                    if(sendMsg.equals(ServerConnection.SELFLEAVE_QUE)) {
                        sender.writeUTF(sendMsg);
                        System.out.print("<YOU LEFT THE CHAT, ");
                        if(clientMain.userAcc.contact.contains(clientMain.partnerUserAcc.userName)) {
                            System.out.println("ENTER ANY KEY TO GO BACK TO FUNCTION MENU>");
                        }
                        else {
                            System.out.println("DO YOU WANT TO SAVE THIS CONTACT? (y/n)>");
                        }
                        isPartnerOnline.set(false);
                        receiver_thread.interrupt();
                        sender_thread.interrupt();
                        break;
                    }
                    else if(!Thread.interrupted()) {
                        realWriteUTF(sendMsg);
                    }
                    else {
                        choice = sendMsg;
                    }
                }
            }
            catch(IOException e) {
            }
            finally {
            }
        };
        sender_thread = new Thread(runSending);
        sender_thread.start();

        while(isPartnerOnline.get() || sender_thread.isAlive() || receiver_thread.isAlive()) {
            try {
                Thread.sleep(50);
            }
            catch(InterruptedException e) {
            }
        }
        if(choice.length() == 1) {
            switch(choice.charAt(0)) {
                case 'Y':
                case 'y':
                    clientMain.userAcc.addContact(clientMain.partnerUserAcc.userName);
                    break;
            }
        }
        sender_thread.interrupt();
        receiver_thread.interrupt();
        isPartnerOnline.set(false);
        clientMain.partnerUserAcc = null;
    }

    public String realReadUTF() {
        try {
            return Encryption.decrypt(receiver.readUTF(), Encryption.key);
        }
        catch(IOException ex) {
            return null;
        }
    }

    public void realWriteUTF(String msg) {
        try {
            sender.writeUTF(Encryption.encrypt(trollMsg(msg), Encryption.key));
        }
        catch(IOException ex) {
        }
    }

    public static String trollMsg(String oriMsg) {
        String processedMsg = null;
        String[] word = oriMsg.split("[ ]");

        String endstr = null;
        String startstr = null;
        String endstr1 = null;
        for(int i = 0; i < word.length; i++) {
            if(word[i].endsWith("!") || word[i].endsWith(".") || word[i].endsWith("?") || word[i].endsWith(",") || word[i].endsWith("*")) {
                endstr = word[i].substring(word[i].length() - 1);
                word[i] = word[i].substring(0, word[i].length() - 1);
            }
            if(word[i].startsWith("(") && word[i].endsWith(")")
               || word[i].startsWith("{") && word[i].endsWith("}")
               || word[i].startsWith("[") && word[i].endsWith("]")
               || word[i].startsWith("\"") && word[i].endsWith("\"")) {
                endstr1 = word[i].substring(word[i].length() - 1);
                startstr = word[i].substring(0, 1);
                word[i] = word[i].substring(1, word[i].length() - 1);
            }

            if(oppositeMap.containsKey(word[i].toLowerCase())) {
                if(Character.isUpperCase(word[i].charAt(0))) {
                    word[i] = oppositeMap.get(word[i].toLowerCase());
                    word[i] = word[i].substring(0, 1).toUpperCase() + word[i].substring(1);
                }
                else if(Character.isLowerCase(word[i].charAt(0))) {
                    word[i] = oppositeMap.get(word[i].toLowerCase());
                }
            }
            if(endstr != null) {
                word[i] += endstr;
                endstr = null;
            }
            if(endstr1 != null && startstr != null) {
                word[i] = startstr + word[i] + endstr1;
                startstr = null;
                endstr1 = null;
            }
        }

        StringBuilder str1 = new StringBuilder();
        for(int i = 0; i < word.length; i++) {
            str1.append(word[i]).append(" ");
        }
        processedMsg = str1.toString();

        return processedMsg;
    }

    public static int checkPostcode(String postcode) {
        int first = 0;
        int last = ClientConnection.postcodeList.size() - 1;
        int middle = (first + last) / 2;
        while(first <= last) {
            if(Integer.parseInt(ClientConnection.postcodeList.get(middle)[1]) < (Integer.parseInt(postcode))) {
                first = middle + 1;
            }
            else if(Integer.parseInt(ClientConnection.postcodeList.get(middle)[1]) > (Integer.parseInt(postcode))) {
                last = middle - 1;
            }
            else {
                return middle;
            }
            middle = (first + last) / 2;
        }
        return -1;
    }

    public static int checkCountry(String country) {
        int first = 0;
        int last = ClientConnection.countryList.size() - 1;
        int middle = (first + last) >> 1;
        while(first <= last) {
            if(ClientConnection.countryList.get(middle)[0].toLowerCase().compareTo(country.toLowerCase()) < 0) {
                first = middle + 1;
            }
            else if(ClientConnection.countryList.get(middle)[0].toLowerCase().compareTo(country.toLowerCase()) > 0) {
                last = middle - 1;
            }
            else {
                return middle;
            }
            middle = (first + last) >> 1;
        }
        return -1;
    }

    public static int checkInterest(String interest) {
        int first = 0;
        int last = ClientConnection.interestList.size() - 1;
        int middle = (first + last) >> 1;
        while(first <= last) {
            if(ClientConnection.interestList.get(middle).toLowerCase().compareTo(interest.toLowerCase()) < 0) {
                first = middle + 1;
            }
            else if(ClientConnection.interestList.get(middle).toLowerCase().compareTo(interest.toLowerCase()) > 0) {
                last = middle - 1;
            }
            else {
                return middle;
            }
            middle = (first + last) >> 1;
        }
        return -1;
    }

    public static ArrayList<String> searchCountry(String country) {
        ArrayList<String> targetCountry = new ArrayList<>();
        if(country.length() == 0) {
            return targetCountry;
        }
        int first = 0;
        int last = ClientConnection.countryList.size() - 1;
        int middle = (first + last) >> 1;
        int letterSize;
        if(country.length() >= 3) {
            letterSize = 3;
        }
        else if(country.length() >= 2) {
            letterSize = 2;
        }
        else {
            letterSize = 1;
        }
        while(first <= last) {
            if(ClientConnection.countryList.get(middle)[0].substring(0, letterSize).toLowerCase().compareTo(country.substring(0, letterSize).toLowerCase()) < 0) {
                first = middle + 1;
            }
            else if(ClientConnection.countryList.get(middle)[0].substring(0, letterSize).toLowerCase().compareTo(country.substring(0, letterSize).toLowerCase()) > 0) {
                last = middle - 1;
            }
            else {
                break;
            }
            middle = (first + last) >> 1;
        }
        first = middle - 10;
        last = middle + 10;
        if(first < 0) {
            first = 0;
        }
        for(int i = first; i <= last; i++) {
            if(i >= ClientConnection.countryList.size()) {
                break;
            }
            if(ClientConnection.countryList.get(i)[0].substring(0, letterSize).equalsIgnoreCase(country.substring(0, letterSize))) {
                targetCountry.add(ClientConnection.countryList.get(i)[0]);
            }
        }
        return targetCountry;
    }

    public static ArrayList<String> searchInterest(String interest) {
        ArrayList<String> targetInterest = new ArrayList<>();
        if(interest.length() == 0) {
            return targetInterest;
        }
        int first = 0;
        int last = ClientConnection.interestList.size() - 1;
        int middle = (first + last) >> 1;
        int letterSize;
        if(interest.length() >= 3) {
            letterSize = 3;
        }
        else if(interest.length() >= 2) {
            letterSize = 2;
        }
        else {
            letterSize = 1;
        }
        while(first <= last) {
            if(ClientConnection.interestList.get(middle).substring(0, letterSize).toLowerCase().compareTo(interest.substring(0, letterSize).toLowerCase()) < 0) {
                first = middle + 1;
            }
            else if(ClientConnection.interestList.get(middle).substring(0, letterSize).toLowerCase().compareTo(interest.substring(0, letterSize).toLowerCase()) > 0) {
                last = middle - 1;
            }
            else {
                break;
            }
            middle = (first + last) >> 1;
        }
        first = middle - 10;
        last = middle + 10;
        if(first < 0) {
            first = 0;
        }
        for(int i = first; i <= last; i++) {
            if(i >= ClientConnection.interestList.size()) {
                break;
            }
            if(ClientConnection.interestList.get(i).substring(0, letterSize).equalsIgnoreCase(interest.substring(0, letterSize))) {
                targetInterest.add(ClientConnection.interestList.get(i));
            }
        }
        return targetInterest;
    }

    public static ArrayList<String> importInterestCSV() {
        ArrayList<String> list = new ArrayList<>();
        try {
            input = new BufferedReader(new FileReader(INTEREST_FILENAME));
            String hold;
            while((hold = input.readLine()) != null) {
                list.add(hold);
            }
            input.close();
        }
        catch(FileNotFoundException e) {
            System.out.println("File was not found");
        }
        catch(IOException e) {
            System.out.println("Error reading from file");
        }
        return list;
    }

    public static ArrayList<String> importInterestObjFile() {
        ArrayList<String> list = new ArrayList<>();
        try {
            fis = new FileInputStream(INTERESET_OBJ_FILENAME);
            ois = new ObjectInputStream(new BufferedInputStream(fis));
            list = (ArrayList<String>) ois.readObject();
            ois.close();
            fis.close();
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
        catch(ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        return list;
    }

    public static void exportInterestObjFile(ArrayList<String> list) {
        try {
            fos = new FileOutputStream(INTERESET_OBJ_FILENAME);
            oos = new ObjectOutputStream(new BufferedOutputStream(fos));
            oos.writeObject(list);
            // closing resources
            oos.close();
            fos.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void convertInterestCSVtoObjFile() {
        ArrayList<String> list = importInterestCSV();
        exportInterestObjFile(list);
        for(String x : list) {
            System.out.println(x);
        }
    }

    public static ArrayList<String[]> importCountryCSV() {
        ArrayList<String[]> list = new ArrayList<>();
        try {
            input = new BufferedReader(new FileReader(COUNTRY_FILENAME));
            String hold;
            String[] data;
            input.readLine(); //ignore title
            while((hold = input.readLine()) != null) {
                data = hold.split("[,]", 3);
                list.add(data);
            }
            input.close();
        }
        catch(FileNotFoundException e) {
            System.out.println("File was not found");
        }
        catch(IOException e) {
            System.out.println("Error reading from file");
        }
        return list;
    }

    public static ArrayList<String[]> importCountryObjFile() {
        ArrayList<String[]> list = new ArrayList<>();
        try {
            fis = new FileInputStream(COUNTRY_OBJ_FILENAME);
            ois = new ObjectInputStream(new BufferedInputStream(fis));
            list = (ArrayList<String[]>) ois.readObject();
            ois.close();
            fis.close();
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
        catch(ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        return list;
    }

    public static void exportCountryObjFile(ArrayList<String[]> list) {
        try {
            fos = new FileOutputStream(COUNTRY_OBJ_FILENAME);
            oos = new ObjectOutputStream(new BufferedOutputStream(fos));
            oos.writeObject(list);
            // closing resources
            oos.close();
            fos.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void convertCountryCSVtoObjFile() {
        ArrayList<String[]> list = importCountryCSV();
        exportCountryObjFile(list);
        for(String[] x : list) {
            System.out.println(Arrays.toString(x));
        }
    }

    public static ArrayList<String[]> importPostcodeCSV() {
        ArrayList<String[]> list = new ArrayList<>();
        try {
            input = new BufferedReader(new FileReader(POSTCODE_FILENAME));
            String hold;
            String[] data;
            input.readLine(); //ignore title
            while((hold = input.readLine()) != null) {
                data = hold.split("[,]", 5);
                list.add(data);
            }
            input.close();
        }
        catch(FileNotFoundException e) {
            System.out.println("File was not found");
        }
        catch(IOException e) {
            System.out.println("Error reading from file");
        }
        return list;
    }

    public static ArrayList<String[]> importPostcodeObjFile() {
        ArrayList<String[]> list = new ArrayList<>();
        try {
            fis = new FileInputStream(POSTCODE_OBJ_FILENAME);
            ois = new ObjectInputStream(new BufferedInputStream(fis));
            list = (ArrayList<String[]>) ois.readObject();
            ois.close();
            fis.close();
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
        catch(ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        return list;
    }

    public static void exportPostcodeObjFile(ArrayList<String[]> list) {
        try {
            fos = new FileOutputStream(POSTCODE_OBJ_FILENAME);
            oos = new ObjectOutputStream(new BufferedOutputStream(fos));
            oos.writeObject(list);
            // closing resources
            oos.close();
            fos.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void convertPostcodeCSVtoObjFile() {
        ArrayList<String[]> list = importPostcodeCSV();
        exportPostcodeObjFile(list);
        for(String[] x : list) {
            System.out.println(Arrays.toString(x));
        }
    }

    public static HashMap<String, String> importOppositeCSV() {
        HashMap<String, String> oppositeWord = new HashMap<>();
        try {
            input = new BufferedReader(new FileReader(OPPOSITE_FILENAME));
            String hold;
            String[] data;
            while((hold = input.readLine()) != null) {
                data = hold.split("[,]",2);
                oppositeWord.put(data[0], data[1]);
            }
            input.close();
        }
        catch(FileNotFoundException e) {
            System.out.println("File was not found");
        }
        catch(IOException e) {
            System.out.println("Error reading from file");
        }
        return oppositeWord;
    }

    public static HashMap<String, String> importOppositeObjFile() {
        HashMap<String, String> oppositeWord = new HashMap<>();
        try {
            fis = new FileInputStream(OPPOSITE_OBJ_FILENAME);
            ois = new ObjectInputStream(new BufferedInputStream(fis));
            oppositeWord = (HashMap<String, String>) ois.readObject();
            ois.close();
            fis.close();
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
        catch(ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        return oppositeWord;
    }

    public static void exportOppositeObjFile(HashMap<String, String> map) {
        try {
            fos = new FileOutputStream(OPPOSITE_OBJ_FILENAME);
            oos = new ObjectOutputStream(new BufferedOutputStream(fos));
            oos.writeObject(map);
            // closing resources
            oos.close();
            fos.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }

    }

    public static void convertOppositeCSVtoObjFile() {
        HashMap<String, String> map = importOppositeCSV();
        exportOppositeObjFile(map);
        for(Map.Entry<String, String> e : map.entrySet()) {
            System.out.println(e.getKey() + " : " + e.getValue());
        }
    }

}
