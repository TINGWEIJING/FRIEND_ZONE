package friendzone;

import java.io.*;
import java.util.concurrent.*;
import friendzone.Encryption;
import friendzone.ServerMain;
import friendzone.TanTanAcc;
import friendzone.TinderAcc;
import friendzone.UserAcc;

/**
 * This class is use to manage file read write from local storage.<br>
 * https://www.java67.com/2015/08/how-to-load-data-from-csv-file-in-java.html<br>
 * https://mkyong.com/java/how-to-read-and-parse-csv-file-in-java/
 *
 * @author TING WEI JING
 */
public class ServerFileStoring {

    static FileOutputStream fos;
    static ObjectOutputStream oos;
    static FileInputStream fis;
    static ObjectInputStream ois;
    static BufferedReader input;
    static BufferedWriter output;

    private final static String USERLIST_FILENAME = "UserList.csv";
    private final static String USERLIST_OBJ_FILENAME = "data/UserList.dat";
    private final static String TINDERUSERLIST_FILENAME = "TinderUserList.csv";
    private final static String TINDERUSERLIST_OBJ_FILENAME = "data/TinderUserList.dat";
    private final static String TANTANUSERLIST_FILENAME = "TanTanUserList.csv";
    private final static String TANTANUSERLIST_OBJ_FILENAME = "data/TanTanUserList.dat";

    static BufferedReader br;

    public static void exportUserDataFileScheduledService() {
        
        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
        scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() 
            {
                distributeUserList(ServerMain.userList);
                exportUserListObjFile(ServerMain.userList);
                ServerMain.printMsg("User list exported");
            }

        }, 30, 60, TimeUnit.SECONDS);
    }

    public static void showOnlineUserScheduledService() {
        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
        scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("Online User:");
                for(UserAcc x : ServerMain.onlineMap.keySet()) {
                    System.out.println(x.toString());
                }
            }

        }, 1, 10, TimeUnit.SECONDS);
    }

    public static CopyOnWriteArraySet<UserAcc> importUserListCSV() {
        CopyOnWriteArraySet<UserAcc> list = new CopyOnWriteArraySet<>();
        UserAcc newUser;
        try {
            input = new BufferedReader(new FileReader(USERLIST_FILENAME));
            String hold;
            String[] data;
            input.readLine(); // ignore column title
            while((hold = input.readLine()) != null) {
                data = hold.split("[,]", 22);
                newUser = new UserAcc(data[0], data[1], Encryption.encrypt(data[2], Encryption.key), data[3]);
                if(data[4].equals("")) {
                    newUser.gender = 'M';
                }
                else {
                    newUser.gender = data[4].charAt(0);
                }
                if(data[5].equals("")) {
                    newUser.genderPreference = 'M';
                }
                else {
                    newUser.genderPreference = data[5].charAt(0);
                }
                if(data[6].equals("")) {
                    newUser.age = 0;
                }
                else {
                    newUser.age = Integer.parseInt(data[6]);
                }
                newUser.country = data[7];
                newUser.state = data[8];
                newUser.postcode = data[9];
                if(data[10].equals("")) {
                    newUser.latitude = 0;
                }
                else {
                    newUser.latitude = Double.parseDouble(data[10]);
                }
                if(data[11].equals("")) {
                    newUser.longitude = 0;
                }
                else {
                    newUser.longitude = Double.parseDouble(data[11]);
                }
                for(int i = 0; i < UserAcc.INTEREST_SIZE; i++) {
                    newUser.interest[i] = data[12 + i];
                }
                list.add(newUser);
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

    public static CopyOnWriteArraySet<UserAcc> importUserListObjFile() {
        CopyOnWriteArraySet<UserAcc> list = null;
        try {
            fis = new FileInputStream(USERLIST_OBJ_FILENAME);
            ois = new ObjectInputStream(new BufferedInputStream(fis));
            list = (CopyOnWriteArraySet<UserAcc>) ois.readObject();
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

    public static void distributeUserList(CopyOnWriteArraySet<UserAcc> userList) {
        CopyOnWriteArrayList<UserAcc> boy = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<UserAcc> girl = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<UserAcc> homoBoy = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<UserAcc> homoGirl = new CopyOnWriteArrayList<>();

        for(UserAcc x : userList) {
            if(x.gender == 'M' && x.genderPreference == 'F') {
                boy.add(x);
            }
            else if(x.gender == 'F' && x.genderPreference == 'M') {
                girl.add(x);
            }
            else if(x.gender == 'M' && x.genderPreference == 'M') {
                homoBoy.add(x);
            }
            else {
                homoGirl.add(x);
            }
        }

        ServerMain.boyList.clear();
        ServerMain.girlList.clear();
        ServerMain.homoBoyList.clear();
        ServerMain.homoGirlList.clear();
        ServerMain.boyList = boy;
        ServerMain.girlList = girl;
        ServerMain.homoBoyList = homoBoy;
        ServerMain.homoGirlList = homoGirl;

    }

    public static void exportUserListObjFile(CopyOnWriteArraySet<UserAcc> list) {
        try {
            fos = new FileOutputStream(USERLIST_OBJ_FILENAME);
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

    public static void convertUserListCSVtoObjFile() {
        CopyOnWriteArraySet<UserAcc> userList = ServerFileStoring.importUserListCSV();
        exportUserListObjFile(userList);
        for(UserAcc x : userList) {
            System.out.println(x.toString());
        }
    }

    public static CopyOnWriteArrayList<TinderAcc> importTinderUserListCSV() {
        CopyOnWriteArrayList<TinderAcc> list = new CopyOnWriteArrayList<>();
        TinderAcc newUser;
        try {
            input = new BufferedReader(new FileReader(TINDERUSERLIST_FILENAME));
            String hold;
            String[] data;
            input.readLine(); // ignore column title
            while((hold = input.readLine()) != null) {
                data = hold.split("[,]");
                newUser = new TinderAcc(data[0],
                                        data[1],
                                        Encryption.encrypt(data[2], Encryption.key),
                                        data[3],
                                        TinderAcc.birthdateToAge(data[4]),
                                        data[5].charAt(0),
                                        data[6].charAt(0),
                                        data[7]
                );
                list.add(newUser);
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

    public static CopyOnWriteArrayList<TinderAcc> importTinderUserListObjFile() {
        CopyOnWriteArrayList<TinderAcc> list = null;
        try {
            fis = new FileInputStream(TINDERUSERLIST_OBJ_FILENAME);
            ois = new ObjectInputStream(new BufferedInputStream(fis));
            list = (CopyOnWriteArrayList<TinderAcc>) ois.readObject();
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

    public static void exportTinderUserListObjFile(CopyOnWriteArrayList<TinderAcc> list) {
        try {
            fos = new FileOutputStream(TINDERUSERLIST_OBJ_FILENAME);
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

    public static void convertTinderUserListCSVtoObjFile() {
        CopyOnWriteArrayList<TinderAcc> userList = ServerFileStoring.importTinderUserListCSV();
        exportTinderUserListObjFile(userList);
        for(TinderAcc x : userList) {
            System.out.println(x.toString());
        }
    }

    public static CopyOnWriteArrayList<TanTanAcc> importTanTanUserListCSV() {
        CopyOnWriteArrayList<TanTanAcc> list = new CopyOnWriteArrayList<>();
        TanTanAcc newUser;
        try {
            input = new BufferedReader(new FileReader(TANTANUSERLIST_FILENAME));
            String hold;
            String[] data;
            input.readLine(); // ignore column title
            while((hold = input.readLine()) != null) {
                data = hold.split("[,]");
                newUser = new TanTanAcc(data[0],
                                        Encryption.encrypt(data[1], Encryption.key),
                                        data[2],
                                        data[3].charAt(0),
                                        data[4].charAt(0),
                                        TinderAcc.birthdateToAge(data[5]),
                                        data[6]
                );
                list.add(newUser);
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

    public static CopyOnWriteArrayList<TanTanAcc> importTanTanUserListObjFile() {
        CopyOnWriteArrayList<TanTanAcc> list = null;
        try {
            fis = new FileInputStream(TANTANUSERLIST_OBJ_FILENAME);
            ois = new ObjectInputStream(new BufferedInputStream(fis));
            list = (CopyOnWriteArrayList<TanTanAcc>) ois.readObject();
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

    public static void exportTanTanUserListObjFile(CopyOnWriteArrayList<TanTanAcc> list) {
        try {
            fos = new FileOutputStream(TANTANUSERLIST_OBJ_FILENAME);
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

    public static void convertTanTanUserListCSVtoObjFile() {
        CopyOnWriteArrayList<TanTanAcc> userList = ServerFileStoring.importTanTanUserListCSV();
        exportTanTanUserListObjFile(userList);
        for(TanTanAcc x : userList) {
            System.out.println(x.toString());
        }
    }

}
