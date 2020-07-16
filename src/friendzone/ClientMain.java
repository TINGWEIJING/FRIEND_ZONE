package friendzone;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import friendzone.ClientConnection;
import friendzone.Encryption;
import friendzone.ServerConnection;
import friendzone.UserAcc;

/**
 * This class is used to handle CLI user interface.<br>
 * Only this class contains main() method to run the project.
 *
 */
public class ClientMain {

    private static int width = 52;
    private static int height = 26;
    private static int limitHeight = height - 3;

    private Scanner sc = new Scanner(System.in);
    private String input;
    public UserAcc userAcc;
    public UserAcc partnerUserAcc;
    public ClientMain clientMain;
    private ClientConnection connection;

    private Thread responding_thread;

    private AtomicBoolean confirmFromServer = new AtomicBoolean(false);
    private AtomicBoolean quitCondition = new AtomicBoolean(false);

    /**
     * Everything will run and call in main() method.
     *
     * @param args
     */
    public static void main(String[] args) {
        ClientMain clientMain = new ClientMain();
        clientMain.connectServerBox(clientMain);
        clientMain.mainMenuBox();
    }

    /**
     * Create a command box using unicode symbol.
     *
     */
    public void commandBox(String msg) {
        clearPage();
        StringBuilder output = new StringBuilder();
        int lineCount = 0;
        int count = 0;

        // roof of box
        for(int i = 0; i < width; i++) {
            output.append("*");
        }
        output.append("\n");    //new line

        // remember all start with 0 index
        for(int i = 0; i < msg.length(); i++) {
            // open line
            if(count == 0) {
                output.append("* ");
            }
            // check the letter count reaches end of line 
            if(count == width - 4) {
                // check is the next character of the end of line is 'space' or 'new line'
                if(msg.charAt(i) == ' ' || msg.charAt(i) == '\n') {
                    output.append(msg.substring(i - count, i));
                    output.append(" *\n");   // close line and new line
                    count = 0;
                    lineCount++;
                }
                // if not go to previous letter by looping until the j counter reach the last 'space' in the line
                else {
                    for(int j = i - 1; j >= i - count; j--) {
                        if(msg.charAt(j) == ' ') {
                            output.append(msg.substring(i - count, j));
                            for(int k = 0; k < i - j; k++) {
                                output.append(" ");
                            }
                            output.append(" *\n") // close line and new line
                                    .append("* "); // open line
                            count = i - j;
                            lineCount++;
                        }
                    }

                }
            }
            // if the line contains 'new line'
            else if(msg.charAt(i) == '\n') {
                output.append(msg.substring(i - count, i));
                for(int j = 0; j < (width - 4) - count; j++) {
                    output.append(" ");
                }
                output.append(" *\n"); // close line and new line
                count = 0;
                lineCount++;
            }
            // if i counter reach the last character of msg
            else if(i == msg.length() - 1) {
                output.append(msg.substring(i - count));
                for(int j = 0; j < (width - 4) - count - 1; j++) { // minus one because width is size while count is index
                    output.append(" ");
                }
                output.append(" *\n"); // close line and new line
                lineCount++;
            }
            else {
                count++;
            }
        }
        for(int i = 0; i < width; i++) {
            output.append("*");
        }
        for(int i = lineCount; i < height - 2; i++) {
            output.append("\n");
        }
        output.append("> ");
        System.out.print(output);
    }

    public int commandBox(String msg, String[] arr) {
        clearPage();
        StringBuilder output = new StringBuilder();
        int lineCount = 0;
        int count = 0;

        // roof of box
        for(int i = 0; i < width; i++) {
            output.append("*");
        }
        output.append("\n");    //new line

        // remember all start with 0 index
        for(int i = 0; i < msg.length(); i++) {
            // open line
            if(count == 0) {
                output.append("* ");
            }
            // check the letter count reaches end of line 
            if(count == width - 4) {
                // check is the next character of the end of line is 'space' or 'new line'
                if(msg.charAt(i) == ' ' || msg.charAt(i) == '\n') {
                    output.append(msg.substring(i - count, i));
                    output.append(" *\n");   // close line and new line
                    count = 0;
                    lineCount++;
                }
                // if not go to previous letter by looping until the j counter reach the last 'space' in the line
                else {
                    for(int j = i - 1; j >= i - count; j--) {
                        if(msg.charAt(j) == ' ') {
                            output.append(msg.substring(i - count, j));
                            for(int k = 0; k < i - j; k++) {
                                output.append(" ");
                            }
                            output.append(" *\n") // close line and new line
                                    .append("* "); // open line
                            count = i - j;
                            lineCount++;
                        }
                    }

                }
            }
            // if the line contains 'new line'
            else if(msg.charAt(i) == '\n') {
                output.append(msg.substring(i - count, i));
                for(int j = 0; j < (width - 4) - count; j++) {
                    output.append(" ");
                }
                output.append(" *\n"); // close line and new line
                count = 0;
                lineCount++;
            }
            // if i counter reach the last character of msg
            else if(i == msg.length() - 1) {
                output.append(msg.substring(i - count));
                for(int j = 0; j < (width - 4) - count - 1; j++) { // minus one because width is size while count is index
                    output.append(" ");
                }
                output.append(" *\n"); // close line and new line
                lineCount++;
            }
            else {
                count++;
            }
        }
        int numOfOption = 0;
        int letterCount = 0;
        StringBuilder tmp = new StringBuilder();
        for(int i = 0; lineCount < limitHeight && i < arr.length; lineCount++, i++) {
            if(arr[i] == null) {
                break;
            }
            else if(arr[i].equals("")) {
                break;
            }
            tmp.append("* ").append(i + 1).append(" - ").append(arr[i]);
            letterCount = tmp.length();
            for(int j = 0; j < width - letterCount - 2; j++) {
                tmp.append(" ");
            }
            tmp.append(" *\n");
            letterCount = 0;
            output.append(tmp);
            tmp.setLength(0);
            numOfOption++;
        }
        for(int i = 0; i < width; i++) {
            output.append("*");
        }
        for(int i = lineCount; i < height - 2; i++) {
            output.append("\n");
        }
        output.append("> ");
        System.out.print(output);
        return numOfOption;
    }

    /**
     * Display connecting message
     */
    public void connectServerBox(ClientMain clientMain) {
        String msg = "Connecting to FRIEND ZONE server."
                     + "\nPlease wait...";
        commandBox(msg);
        connection = new ClientConnection(clientMain);
        while(!connection.connectServer()) {
            // keep looping until connect to server
            // System.out.print('\b');
        }
        msg = "Connected to FRIEND ZONE server.";
        commandBox(msg);
    }

    /**
     * Create a main menu interface.
     *
     */
    public void mainMenuBox() {
        String msg = "Welcome to FRIEND ZONE app."
                     + "\n\nCommand:"
                     + "\n1 - Log in"
                     + "\n2 - Register"
                     + "\nq - Exit"
                     + "";
        while(true) {
            connection.booleanFromServer();
            commandBox(msg);
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case '1':
                        connection.sendToServer(ServerConnection.LOGIN_QUE);
                        loginBox();
                        connection.sendDisconnectQue();
                        connection.reconnectServer(); //reconnect to server
                        break;

                    case '2':
                        registerBox();
                        connection.sendDisconnectQue();
                        connection.reconnectServer();
                        break;

                    case 'q':
                        connection.sendDisconnectQue();
                        System.exit(0);
                        break;

                    default:
                        connection.sendToServer("");
                }
            }
            else {
                connection.sendToServer("");
            }
        }
    }

    /**
     * Create a login user interface.
     *
     */
    public void loginBox() {
        boolean isUserNameCorrect = false;
        String msg = "Please enter your username:"
                     + "\n\nCommand:"
                     + "\nEnter - Submit"
                     + "\nq - Back";
        userNameLoop:
        while(!isUserNameCorrect) {
            commandBox(msg);
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case 'q':
                        return;

                    default:
                        connection.sendToServer(input);
                }
            }
            else {
                connection.sendToServer(input);
            }
            if(!connection.booleanFromServer()) {
                msg = "Username not exist!"
                      + "\nPlease enter your username:"
                      + "\n\nCommand:"
                      + "\nEnter - Submit"
                      + "\nq - Back";
            }
            else {
                isUserNameCorrect = true;
                break;
            }
        }
        //password auth
        msg = "Please enter your password:"
              + "\n\nCommand:"
              + "\nEnter - Submit"
              + "\nq - Back";
        passLoop:
        while(isUserNameCorrect) {
            commandBox(msg);
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case 'q':
                        return;

                    default:
                        connection.sendToServer(Encryption.encrypt(input, Encryption.key));
                }
            }
            else {
                connection.sendToServer(Encryption.encrypt(input, Encryption.key));
            }
            if(!connection.booleanFromServer()) {
                msg = "Password incorrect!"
                      + "\nPlease enter your password:"
                      + "\n\nCommand:"
                      + "\nEnter - Submit"
                      + "\nq - Back";
                continue;
            }
            else { //login
                connection.downloadUserDataFromServer();
                functionBox();
            }
            break;
        }
    }

    /**
     * Create a new account registration user interface.
     *
     */
    public void registerBox() {
        String msg = "How do you want to register?:"
                     + "\n\nCommand:"
                     + "\n1 - A new Friendzone Account"
                     + "\n2 - With exiting Tinder Account"
                     + "\n3 - With exiting TanTan Account"
                     + "\nq - Back";
        while(true) {
            commandBox(msg);
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case '1':
                        connection.sendToServer(ServerConnection.REGISTER_QUE);
                        registerNewAccountBox();
                        return;

                    case '2':
                        connection.sendToServer(ServerConnection.RETINDER_QUE);
                        registerWithTinder();
                        return;

                    case '3':
                        connection.sendToServer(ServerConnection.RETANTAN_QUE);
                        registerWithTanTan();
                        return;

                    case 'q':
                        return;

                }
            }
        }
    }

    public void functionBox() {
        boolean updateLocation;
        String msg = "Please select the function you want:"
                     + "\n\nCommand:"
                     + "\n1 - Search partner"
                     + "\n2 - Find user near me"
                     + "\n3 - Saved contacts"
                     + "\n4 - Update personal info"
                     + "\nq - Log out";
        while(true) {
            confirmFromServer.set(false);
            handleIncomingMsgBox();
            commandBox(msg);
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case '1':
                        updateLocation = updateLocationBox();
                        if(updateLocation) {
                            connection.sendToServer(ServerConnection.SEARCHPARTNER_QUE);
                            connection.sendLocationToServer();
                            searchWaitingBox();
                            if(confirmFromServer.get()) {
                                displayPartnerInfoBox();
                                connection.chattingWithPartner();
                            }
                            confirmFromServer.set(false);
                        }
                        else {
                            connection.sendToServer(""); //server you quit
                        }
                        break;

                    case '2':
                        updateLocation = updateLocationBox();
                        if(updateLocation) {
                            connection.sendToServer(ServerConnection.FINDNEARBY_QUE);
                            connection.sendLocationToServer();
                            searchWaitingBox();
                            if(confirmFromServer.get()) {
                                displayNearbyUserInfoBox();
                            }
                            confirmFromServer.set(false);
                        }
                        else {
                            connection.sendToServer(""); //server you quit
                        }
                        break;

                    case '3':
                        if(displaySavedContactBox()) {
                            if(confirmFromServer.get()) {
                                displayPartnerInfoBox();
                                connection.chattingWithPartner();
                                confirmFromServer.set(false);
                            }
                        }
                        else {
                            connection.sendToServer(""); //server you quit
                        }
                        break;

                    case '4':
                        connection.sendToServer(ServerConnection.UPDATEINFO_QUE);
                        updateInfoBox();
                        break;

                    case 'q':
                        return;

                    default:
                        connection.sendToServer(""); //tell server you quit
                }
            }
            else {
                connection.sendToServer(""); //tell server you quit
            }
        }
    }

    public boolean updateInfoBox() {
        StringBuilder msgBuild;
        while(true) {
            msgBuild = new StringBuilder();
            msgBuild.append("Your personal infomation:")
                    .append("\n> UserID: ").append(this.userAcc.userID).append(" (Immutable)")
                    .append("\n> Username: ").append(this.userAcc.userName).append(" (Immutable)")
                    .append("\n> Email: ").append(this.userAcc.email).append(" (Immutable)")
                    .append("\n> Password: (hide)")
                    .append("\n> Gender: ").append(this.userAcc.gender)
                    .append("\n> Gender Preference: ").append(this.userAcc.genderPreference)
                    .append("\n> Age: ").append(this.userAcc.age)
                    .append("\n> Country: ").append(this.userAcc.country);
            if(this.userAcc.country.equals("Malaysia")) {
                msgBuild.append("\n> State: ").append(this.userAcc.state)
                        .append("\n> Postcode: ").append(this.userAcc.postcode);
            }
            msgBuild.append("\n> Interest: (Enter '5' to show the list)")
                    .append("\n\nCommand:")
                    .append("\n1 - Change password")
                    .append("\n2 - Change gender & gender preference setting")
                    .append("\n3 - Update age")
                    .append("\n4 - Update location")
                    .append("\n5 - Update interest list")
                    .append("\nq - Back");
            commandBox(msgBuild.toString());
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case '1':
                        if(updatePasswordBox(this.userAcc)) {
                            connection.sendToServer(true);
                            connection.uploadUserDataToServer();
                        }
                        break;

                    case '2':
                        if(updateGenderBox(this.userAcc)) {
                            connection.sendToServer(true);
                            connection.uploadUserDataToServer();
                        }
                        break;

                    case '3':
                        if(updateAgeBox(this.userAcc)) {
                            connection.sendToServer(true);
                            connection.uploadUserDataToServer();
                        }
                        break;

                    case '4':
                        if(updateLocationBox(this.userAcc)) {
                            connection.sendToServer(true);
                            connection.uploadUserDataToServer();
                        }
                        break;

                    case '5':
                        if(updateInterestBox(this.userAcc)) {
                            connection.sendToServer(true);
                            connection.uploadUserDataToServer();
                        }
                        break;

                    case 'q':
                        connection.sendToServer(false);
                        return false;
                }
            }
        }
    }

    public boolean updatePasswordBox(UserAcc user) {
        StringBuilder msgBuild;
        msgBuild = new StringBuilder();
        msgBuild.append("Please enter your current password:")
                .append("\n\nCommand:")
                .append("\nEnter - Submit")
                .append("\nq - Back");
        while(true) {
            commandBox(msgBuild.toString());
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case 'q':
                        return false;
                }
            }
            if(Encryption.encrypt(input, Encryption.key).equals(user.password)) {
                break;
            }
            else {
                msgBuild.setLength(0);
                msgBuild.append("Wrong password!:")
                        .append("\n\nCommand:")
                        .append("\nEnter - Submit")
                        .append("\nq - Back");
            }
        }
        msgBuild.setLength(0);
        msgBuild.append("Please enter new password:")
                .append("\n\nCommand:")
                .append("\nEnter - Submit")
                .append("\nq - Back");
        while(true) {
            commandBox(msgBuild.toString());
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case 'q':
                        return false;
                }
            }
            if(input.length() > 5) {
                user.password = Encryption.encrypt(input, Encryption.key);
                return true;
            }
            else {
                msgBuild.setLength(0);
                msgBuild.append("Password must more than 5 characters:")
                        .append("\n\nCommand:")
                        .append("\nEnter - Submit")
                        .append("\nq - Back");
            }
        }
    }

    public boolean updateGenderBox(UserAcc user) {
        char tmpGender = 'M';
        StringBuilder msgBuild;
        msgBuild = new StringBuilder();
        msgBuild.append("Gender? :")
                .append("\n\nCommand:")
                .append("\nM - Male")
                .append("\nF - Female")
                .append("\nq - Back");
        genderLoop:
        while(true) {
            commandBox(msgBuild.toString());
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case 'M':
                    case 'm':
                        tmpGender = 'M';
                        break genderLoop;

                    case 'F':
                    case 'f':
                        tmpGender = 'F';
                        break genderLoop;

                    case 'q':
                        return false;
                }
            }
        }
        msgBuild.setLength(0);
        msgBuild.append("Gender preference? :")
                .append("\n\nCommand:")
                .append("\nM - Male")
                .append("\nF - Female")
                .append("\nq - Back");
        while(true) {
            commandBox(msgBuild.toString());
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case 'M':
                    case 'm':
                        user.gender = tmpGender;
                        user.genderPreference = 'M';
                        return true;

                    case 'F':
                    case 'f':
                        user.gender = tmpGender;
                        user.genderPreference = 'F';
                        return true;

                    case 'q':
                        return false;
                }
            }
        }
    }

    public boolean updateAgeBox(UserAcc user) {
        StringBuilder msgBuild;
        msgBuild = new StringBuilder();
        msgBuild.append("Age? :")
                .append("\n\nCommand:")
                .append("\nEnter - Submit")
                .append("\nq - Back");
        genderLoop:
        while(true) {
            commandBox(msgBuild.toString());
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case 'q':
                        return false;
                }
            }
            try {
                int age = Integer.parseInt(input);
                if(user.age > 0) {
                    user.age = age;
                    return true;
                }
            }
            catch(Exception e) {
            }
        }
    }

    public boolean updateInterestBox(UserAcc user) {
        StringBuilder msgBuild = new StringBuilder();
        StringBuilder tmp = new StringBuilder();
        String[] interestArr = new String[0];
        ArrayList<String> tmpList;
        while(true) {
            msgBuild.setLength(0);
            msgBuild.append("Interest List:");
            for(int i = 0; i < user.interest.length / 2; i++) {
                if(user.interest[i].equals("")) {
                    break;
                }
                else {
                    tmp = new StringBuilder();
                    tmp.append("\n").append(i + 1).append(" - ").append(user.interest[i]);
                    if(!user.interest[i + user.interest.length / 2].equals("")) {
                        int len = width - 26 - tmp.length();
                        for(int j = 0; j < len; j++) {
                            tmp.append(" ");
                        }
                        tmp.append(i + 1 + 5).append(" - ").append(user.interest[i + user.interest.length / 2]);
                    }
                    msgBuild.append(tmp);
                }
            }
            msgBuild.append("\n\nCommand:")
                    .append("\nEnter - Submit or search")
                    .append("\nr <number> - remove interest")
                    .append("\nq - Back")
                    .append("\n\nSearch result:");
            commandBox(msgBuild.toString(), interestArr);
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case 'q':
                        return true;
                }
            }
            else if(input.startsWith("r ")) {
                int index;
                try {
                    index = Integer.parseInt(input.substring(2)) - 1;
                    if(index < user.interest.length) {
                        String[] tmpArr = user.interest.clone();
                        for(int i = index; i < user.interest.length; i++) {
                            user.interest[i] = "";
                        }
                        System.arraycopy(tmpArr, index + 1, user.interest, index, user.interest.length - 1 - index);
                        tmpArr = null;
                    }
                    continue;
                }
                catch(Exception e) {
                }
            }
            int index;
            index = ClientConnection.checkInterest(input);
            if(index != -1) {
                interestArr = new String[0];
                String tmpInterest = ClientConnection.interestList.get(index);
                for(int i = 0; i < user.interest.length; i++) {
                    if(user.interest[i].equals(tmpInterest)) {
                        break;
                    }
                    if(user.interest[i].equals("")) {
                        user.interest[i] = tmpInterest;
                        break;
                    }
                }
            }
            else {
                tmpList = ClientConnection.searchInterest(input);
                interestArr = tmpList.toArray(new String[tmpList.size()]);
            }
        }
    }

    public void handleIncomingMsgBox() {
        boolean gotIncoming = connection.booleanFromServer();
        if(gotIncoming) {
            this.partnerUserAcc = connection.downloadPartnerUserDataFromServer();
            handleLoop:
            while(true) {
                StringBuilder msg = new StringBuilder();
                msg.append("You have a invitation on instant chat from:")
                        .append("\nUsername - ").append(partnerUserAcc.userName)
                        .append("\nEmail - ").append(partnerUserAcc.email)
                        .append("\nGender - ").append(partnerUserAcc.gender)
                        .append("\nGender Preference - ").append(partnerUserAcc.genderPreference)
                        .append("\nAge - ").append(partnerUserAcc.age)
                        .append("\nCountry - ").append(partnerUserAcc.country);
                if(!partnerUserAcc.state.equals("")) {
                    msg.append("\nState - ").append(partnerUserAcc.state);
                }
                if(!partnerUserAcc.interest[0].equals("")) {
                    msg.append("\nInterest:");
                    for(int i = 0; i < partnerUserAcc.interest.length; i++) {
                        if(partnerUserAcc.interest[i].equals("")) {
                            break;
                        }
                        else {
                            msg.append("\n").append(i + 1).append(" - ").append(partnerUserAcc.interest[i]);
                        }
                    }
                }
                this.commandBox(msg.toString());
                System.out.println("<y - ACCEPT, n - REJECT>");
                input = sc.nextLine();
                if(input.length() == 1) {
                    switch(input.charAt(0)) {
                        case 'y':
                        case 'Y':
                            connection.sendToServer(true);
                            if(connection.booleanFromServer()) {
                                connection.chattingWithPartner();
                            }
                            else {
                                System.out.println("<INVITATION CANCLED, ENTER ANY KEY TO GO BACK TO FUNCTION MENU>");
                                sc.nextLine();
                            }
                            break handleLoop;

                        case 'n':
                        case 'N':
                            connection.sendToServer(false);
                            return;
                    }
                }
            }
        }
    }

    public boolean updateLocationBox() {
        boolean submitLocation = false;
        String msg;
        locationLoop:
        while(!submitLocation) {
            if(userAcc.country.equals("") || (userAcc.latitude < 1 && userAcc.latitude > -1)) {
                msg = "Updating your location."
                      + "\nPlease select your country."
                      + "\n\nCommand:"
                      + "\n1 - Malaysia"
                      + "\n2 - Search other country"
                      + "\nq - Back";
                commandBox(msg);
                input = sc.nextLine();
                if(input.length() == 1) {
                    switch(input.charAt(0)) {
                        case '1':
                            this.userAcc.country = "Malaysia";
                            submitLocation = updateStateBox();
                            break;

                        case '2':
                            submitLocation = updateCountryBox();
                            break;

                        case 'q':
                            return false;

                    }
                }
            }
            else {
                msg = "Would you like to update your location?"
                      + "\n\nCommand:"
                      + "\nY - Yes"
                      + "\nN - No and proceed"
                      + "\nq - Back";
                commandBox(msg);
                input = sc.nextLine();
                if(input.length() == 1) {
                    switch(input.charAt(0)) {
                        case 'Y':
                        case 'y':
                            this.userAcc.country = "";
                            this.userAcc.state = "";
                            this.userAcc.latitude = 0;
                            this.userAcc.longitude = 0;
                            break;

                        case 'N':
                        case 'n':
                            submitLocation = true;
                            break;

                        case 'q':
                            return false;
                    }
                }
            }
        }
        return submitLocation;
    }

    public boolean updateLocationBox(UserAcc newUser) {
        boolean submitLocation = false;
        String msg;
        locationLoop:
        while(!submitLocation) {
            msg = "Updating your location."
                  + "\nPlease select your country."
                  + "\n\nCommand:"
                  + "\n1 - Malaysia"
                  + "\n2 - Search other country"
                  + "\nq - Back";
            commandBox(msg);
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case '1':
                        newUser.country = "Malaysia";
                        submitLocation = updateStateBox(newUser);
                        break;

                    case '2':
                        submitLocation = updateCountryBox(newUser);
                        break;

                    case 'q':
                        return false;

                }
            }
        }
        return submitLocation;
    }

    public void searchWaitingBox() {
        Runnable respondingRun = () -> {
            try {
                while(!Thread.interrupted()) {
                    confirmFromServer.set(connection.booleanFromServer());
                    if(confirmFromServer.get()) {
                        clearPage();
                        System.out.println("<ENTER ANY KEY TO CONTINUE>");
                        connection.sendToServer("");
                        break;
                    }
                    else {
                        break;
                    }
                }
            }
            catch(Exception e) {
            }
        };
        responding_thread = new Thread(respondingRun);
        String msg;
        msg = "SEARCHING FOR PARTNER, PLEASE WAIT"
              + "\nYour information:"
              + "\nGender - " + this.userAcc.gender
              + "\nGender Preference - " + this.userAcc.genderPreference
              + "\nAge - " + this.userAcc.age
              + "\nCountry - " + this.userAcc.country
              + "\nInterest:";
        for(int i = 0; i < this.userAcc.interest.length; i++) {
            if(!this.userAcc.interest[i].equals("")) {
                msg += "\n" + (i + 1) + " - " + this.userAcc.interest[i];
            }
        }
        msg += "\nYou may update your interest in \"Update personal info\" function";
        commandBox(msg);
        System.out.println("<ENTER \"" + ServerConnection.DISCONNET_QUE + "\" TO QUIT SEARCHING>");
        responding_thread.start();
        while(!confirmFromServer.get()) {
            input = sc.nextLine();
            if(input.equals(ServerConnection.DISCONNET_QUE) && !confirmFromServer.get()) {
                connection.sendToServer(ServerConnection.DISCONNET_QUE);
                responding_thread.interrupt();
                break;
            }
        }
    }

    public void displayPartnerInfoBox() {
        StringBuilder msg = new StringBuilder();
        this.partnerUserAcc = connection.downloadPartnerUserDataFromServer();
        msg.append("Your partner information:")
                .append("\nUsername - ").append(partnerUserAcc.userName)
                .append("\nEmail - ").append(partnerUserAcc.email)
                .append("\nGender - ").append(partnerUserAcc.gender)
                .append("\nGender Preference - ").append(partnerUserAcc.genderPreference)
                .append("\nAge - ").append(partnerUserAcc.age)
                .append("\nCountry - ").append(partnerUserAcc.country);

        if(!partnerUserAcc.state.equals("")) {
            msg.append("\nState - ").append(partnerUserAcc.state);
        }
        if(!partnerUserAcc.interest[0].equals("")) {
            msg.append("\nInterest:");
            for(int i = 0; i < partnerUserAcc.interest.length; i++) {
                if(partnerUserAcc.interest[i].equals("")) {
                    break;
                }
                else {
                    msg.append("\n").append(i + 1).append(" - ").append(partnerUserAcc.interest[i]);
                }
            }
        }
        this.commandBox(msg.toString());
    }

    public void displayNearbyUserInfoBox() {
        int counter = 0;
        int max = connection.intFromServer();
        boolean move = true;
        StringBuilder msg;
        StringBuilder tmp;
        while(counter < max) {
            msg = new StringBuilder();
            if(move) {
                this.partnerUserAcc = connection.downloadPartnerUserDataFromServer();
            }
            msg.append("Nearby User information:")
                    .append("\nUsername - ").append(partnerUserAcc.userName)
                    .append("\nEmail - ").append(partnerUserAcc.email)
                    .append("\nGender - ").append(partnerUserAcc.gender)
                    .append("\nAge - ").append(partnerUserAcc.age)
                    .append("\nCountry - ").append(partnerUserAcc.country);
            if(!partnerUserAcc.state.equals("")) {
                msg.append("\nState - ").append(partnerUserAcc.state);
            }
            msg.append("\nDistance - ").append(Coordination.calDist(this.userAcc.latitude, this.userAcc.longitude,
                                                                    partnerUserAcc.latitude, partnerUserAcc.longitude));
            if(!partnerUserAcc.interest[0].equals("")) {
                msg.append("\nInterest:");
                for(int i = 0; i < partnerUserAcc.interest.length / 2; i++) {
                    if(partnerUserAcc.interest[i].equals("")) {
                        break;
                    }
                    else {
                        tmp = new StringBuilder();
                        tmp.append("\n").append(i + 1).append(" - ").append(partnerUserAcc.interest[i]);
                        if(!partnerUserAcc.interest[i + partnerUserAcc.interest.length / 2].equals("")) {
                            int len = width - 26 - tmp.length();
                            for(int j = 0; j < len; j++) {
                                tmp.append(" ");
                            }
                            tmp.append(i + 1 + 5).append(" - ").append(partnerUserAcc.interest[i + partnerUserAcc.interest.length / 2]);
                        }
                        msg.append(tmp);
                    }
                }
            }
            msg.append("\n\nCommand:")
                    .append("\n1 - Save contact and next")
                    .append("\n2 - Next user")
                    .append("\nq - Quit searching");
            this.commandBox(msg.toString());
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case '1':
                        this.userAcc.addContact(partnerUserAcc.userName);
                        connection.sendToServer("");
                        move = true;
                        counter++;
                        break;

                    case '2':
                        connection.sendToServer("");
                        move = true;
                        counter++;
                        break;

                    case 'q':
                        connection.sendToServer(ServerConnection.SELFLEAVE_QUE);
                        return;

                }
            }
            else {
                connection.sendToServer("");
                move = false;
            }
        }
    }

    public boolean displaySavedContactBox() {
        StringBuilder msg;
        String tempContact;
        int counter = 0;
        int tempValue;
        while(true) {
            msg = new StringBuilder();
            if(this.userAcc.contact.isEmpty()) {
                msg.append("You do not have any saved contact.")
                        .append("\n\nCommand:")
                        .append("\nq - Back");
                commandBox(msg.toString());
                input = sc.nextLine();
                if(input.length() == 1) {
                    switch(input.charAt(0)) {
                        case 'q':
                            return false;
                    }
                }
            }
            else {
                counter = 0;
                msg.append("Saved contacts:");
                for(ListIterator<String> i = this.userAcc.contact.listIterator(); i.hasNext();) {
                    tempContact = i.next();
                    tempValue = counter + 1;
                    msg.append("\n").append(tempValue).append(" - ").append(tempContact);
                    counter++;
                }
                msg.append("\n\nCommand:")
                        .append("\n<number> - Start chatting with him/her")
                        .append("\nremove <number> - remove contact <number>")
                        .append("\nq - Back");
                commandBox(msg.toString());
                input = sc.nextLine();
                if(input.length() == 1) {
                    switch(input.charAt(0)) {
                        case 'q':
                            return false;
                    }
                }
                if(input.startsWith("remove")) {
                    int index;
                    try {
                        index = Integer.parseInt(input.substring(7)) - 1;
                        if(index < counter) {
                            this.userAcc.contact.remove(index);
                        }
                    }
                    catch(Exception e) {
                    }
                }
                else {
                    int index;
                    try {
                        index = Integer.parseInt(input) - 1;
                        if(index < counter) {
                            connection.sendToServer(ServerConnection.INSTANTCHAT_QUE);
                            instantMsgBox(this.userAcc.contact.get(index));
                            return true;
                        }
                    }
                    catch(Exception e) {
                    }
                }
            }
        }
    }

    public void instantMsgBox(String username) {
        quitCondition.set(false);
        Runnable respondingRun = () -> {
            try {
                boolean tmpBool = connection.booleanFromServer();
                confirmFromServer.set(tmpBool);
                if(confirmFromServer.get()) {
                    clearPage();
                    System.out.println("<ENTER ANY KEY TO CONTINUE>");
                    connection.sendToServer("");
                    quitCondition.set(true);
                }
                else if(!Thread.interrupted()) {
                    clearPage();
                    System.out.println("<YOU HAVE BEEN REJECTED, PRESS ANY KEY TO GO BACK TO FUNCTION MENU>");
                    connection.sendToServer("");
                    confirmFromServer.set(false);
                    quitCondition.set(true);
                }
            }
            catch(Exception e) {
            }
            finally {
            }
        };
        responding_thread = new Thread(respondingRun);
        String msg;
        msg = "Sending instant chat invitation to " + username;
        commandBox(msg);
        System.out.println("<ENTER \"" + ServerConnection.DISCONNET_QUE + "\" TO QUIT>");
        connection.sendToServer(username);
        responding_thread.start();
        while(!quitCondition.get()) {
            input = sc.nextLine();
            if(input.equals(ServerConnection.DISCONNET_QUE) && !confirmFromServer.get()) {
                connection.sendToServer(ServerConnection.DISCONNET_QUE);
                responding_thread.interrupt();
                break;
            }
        }
        responding_thread.interrupt();
    }

    public boolean updateStateBox() {
        boolean isPostcodeValid = false;
        String msg;
        int index = -1;
        msg = "Please enter your postcode number (5 digits)."
              + "\n\nCommand:"
              + "\nEnter - Submit postcode"
              + "\nq - Back";
        while(!isPostcodeValid) {
            commandBox(msg);
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case 'q':
                        return false;
                }
            }
            else {
                index = ClientConnection.checkPostcode(input);
            }
            if(index != -1) {
                isPostcodeValid = true;
                this.userAcc.state = ClientConnection.postcodeList.get(index)[0];
                this.userAcc.postcode = input;
                this.userAcc.latitude = Double.parseDouble(ClientConnection.postcodeList.get(index)[3]);
                this.userAcc.longitude = Double.parseDouble(ClientConnection.postcodeList.get(index)[4]);
            }
        }
        return true;
    }

    public boolean updateStateBox(UserAcc newUser) {
        boolean isPostcodeValid = false;
        String msg;
        int index = -1;
        msg = "Please enter your postcode number (5 digits)."
              + "\n\nCommand:"
              + "\nEnter - Submit postcode"
              + "\nq - Back";
        while(!isPostcodeValid) {
            commandBox(msg);
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case 'q':
                        return false;
                }
            }
            else {
                index = ClientConnection.checkPostcode(input);
            }
            if(index != -1) {
                isPostcodeValid = true;
                newUser.state = ClientConnection.postcodeList.get(index)[0];
                newUser.postcode = input;
                newUser.latitude = Double.parseDouble(ClientConnection.postcodeList.get(index)[3]);
                newUser.longitude = Double.parseDouble(ClientConnection.postcodeList.get(index)[4]);
            }
        }
        return true;
    }

    public boolean updateCountryBox() {
        boolean isCountryValid = false;
        String msg;
        ArrayList<String> tmpList = new ArrayList<>();
        String[] countryArr = new String[0];
        int numOfOption = 0;
        int option = 0;
        int index = -1;
        msg = "Please enter your country."
              + "\n\nCommand:"
              + "\nEnter - Submit country"
              + "\nq - Back"
              + "\nSearch result:";
        while(!isCountryValid) {
            numOfOption = commandBox(msg, countryArr);
            input = sc.nextLine();
            if(countryArr.length > 1) {
                try {
                    option = Integer.parseInt(input);
                    if(option <= numOfOption) {
                        isCountryValid = true;
                        this.userAcc.country = countryArr[option - 1];
                        index = ClientConnection.checkCountry(this.userAcc.country);
                        this.userAcc.state = "";
                        this.userAcc.postcode = "";
                        this.userAcc.latitude = Double.parseDouble(ClientConnection.countryList.get(index)[1]);
                        this.userAcc.longitude = Double.parseDouble(ClientConnection.countryList.get(index)[2]);
                        return true;
                    }
                }
                catch(NumberFormatException e) {
                }
            }
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case 'q':
                        return false;
                }
            }
            index = ClientConnection.checkCountry(input);
            if(index != -1) {
                isCountryValid = true;
                this.userAcc.country = ClientConnection.countryList.get(index)[0];
                this.userAcc.state = "";
                this.userAcc.postcode = "";
                this.userAcc.latitude = Double.parseDouble(ClientConnection.countryList.get(index)[1]);
                this.userAcc.longitude = Double.parseDouble(ClientConnection.countryList.get(index)[2]);
            }
            else {
                tmpList = ClientConnection.searchCountry(input);
                countryArr = tmpList.toArray(new String[tmpList.size()]);
            }
        }
        return true;
    }

    public boolean updateCountryBox(UserAcc newUser) {
        boolean isCountryValid = false;
        String msg;
        ArrayList<String> tmpList = new ArrayList<>();
        String[] countryArr = new String[0];
        int numOfOption = 0;
        int option = 0;
        int index = -1;
        msg = "Please enter your country."
              + "\n\nCommand:"
              + "\nEnter - Submit country"
              + "\nq - Back"
              + "\nSearch result:";
        while(!isCountryValid) {
            numOfOption = commandBox(msg, countryArr);
            input = sc.nextLine();
            if(countryArr.length > 1) {
                try {
                    option = Integer.parseInt(input);
                    if(option <= numOfOption) {
                        isCountryValid = true;
                        newUser.country = countryArr[option - 1];
                        index = ClientConnection.checkCountry(this.userAcc.country);
                        newUser.state = "";
                        newUser.postcode = "";
                        newUser.latitude = Double.parseDouble(ClientConnection.countryList.get(index)[1]);
                        newUser.longitude = Double.parseDouble(ClientConnection.countryList.get(index)[2]);
                        return true;
                    }
                }
                catch(NumberFormatException e) {
                }
            }
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case 'q':
                        return false;
                }
            }
            index = ClientConnection.checkCountry(input);
            if(index != -1) {
                isCountryValid = true;
                newUser.country = ClientConnection.countryList.get(index)[0];
                newUser.state = "";
                newUser.postcode = "";
                newUser.latitude = Double.parseDouble(ClientConnection.countryList.get(index)[1]);
                newUser.longitude = Double.parseDouble(ClientConnection.countryList.get(index)[2]);
            }
            else {
                tmpList = ClientConnection.searchCountry(input);
                countryArr = tmpList.toArray(new String[tmpList.size()]);
            }
        }
        return true;
    }

    public void chattingBox() {
        String msg = "This is"
                     + "\nYou can start chatting now"
                     + "\n\nCommand:"
                     + "\n\\1 - Show his/her info"
                     + "\n\\q - Exit";
        while(true) {
            commandBox(msg);
            input = sc.nextLine();
        }
    }

    public boolean registerNewAccountBox() {
        UserAcc newUser = new UserAcc("", "", "", "");
        newUser.country = ""; // for update loaction
        for(int i = 0; i < UserAcc.INTEREST_SIZE; i++) {
            newUser.interest[i] = "";
        }
        boolean condCheck = false;
        String msg;
        msg = "Username? (Cannot be changed later):"
              + "\n\nCommand:"
              + "\nEnter - Submit"
              + "\nq - Cancel";
        while(!condCheck) {
            commandBox(msg);
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case 'q':
                        connection.sendToServer(false); //leave the register func
                        return false;
                }
            }
            connection.sendToServer(true); // ask server to check username
            connection.sendToServer(input); // send to check
            condCheck = connection.booleanFromServer();
            if(!condCheck) {  //receive checking
                msg = "Username not acceptable!"
                      + "\nUsername? :"
                      + "\n\nCommand:"
                      + "\nEnter - Submit"
                      + "\nq - Cancel";
            }
            else {
                condCheck = false;
                newUser.userName = input;
                break;
            }
        }
        msg = "Email? (Cannot be changed later):"
              + "\n\nCommand:"
              + "\nEnter - Submit"
              + "\nq - Cancel";
        emailLoop:
        while(!condCheck) {
            commandBox(msg);
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case 'q':
                        connection.sendToServer(false); //leave the register func
                        return false;
                }
            }
            if(input.endsWith(".com") && input.contains("@")) {
                newUser.email = input;
                break;
            }
            else {
                msg = "Email not acceptable!"
                      + "\nEmail? :"
                      + "\n\nCommand:"
                      + "\nEnter - Submit"
                      + "\nq - Cancel";
                condCheck = false;
            }
        }
        msg = "Password? :"
              + "\n\nCommand:"
              + "\nEnter - Submit"
              + "\nq - Cancel";
        passLoop:
        while(!condCheck) {
            commandBox(msg);
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case 'q':
                        connection.sendToServer(false); //leave the register func
                        return false;
                }
            }
            if(input.length() > 5) {
                newUser.password = Encryption.encrypt(input, Encryption.key);
                break;
            }
            else {
                msg = "Password must more than 5 characters"
                      + "\nPassword? :"
                      + "\n\nCommand:"
                      + "\nEnter - Submit"
                      + "\nq - Cancel";
                condCheck = false;
            }
        }
        msg = "Gender? :"
              + "\n\nCommand:"
              + "\nM - Male"
              + "\nF - Female"
              + "\nq - Cancel";
        genderLoop:
        while(!condCheck) {
            commandBox(msg);
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case 'M':
                    case 'm':
                        newUser.gender = 'M';
                        break genderLoop;

                    case 'F':
                    case 'f':
                        newUser.gender = 'F';
                        break genderLoop;

                    case 'q':
                        connection.sendToServer(false); //leave the register func
                        return false;
                }
            }
        }
        msg = "Gender preference? :"
              + "\n\nCommand:"
              + "\nM - Male"
              + "\nF - Female"
              + "\nq - Cancel";
        genderPreferLoop:
        while(!condCheck) {
            commandBox(msg);
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case 'M':
                    case 'm':
                        newUser.genderPreference = 'M';
                        break genderPreferLoop;

                    case 'F':
                    case 'f':
                        newUser.genderPreference = 'F';
                        break genderPreferLoop;

                    case 'q':
                        connection.sendToServer(false); //leave the register func
                        return false;
                }
            }
        }
        msg = "Age? :"
              + "\n\nCommand:"
              + "\nEnter - Submit"
              + "\nq - Cancel";
        ageLoop:
        while(!condCheck) {
            commandBox(msg);
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case 'q':
                        connection.sendToServer(false); //leave the register func
                        return false;
                }
            }
            try {
                newUser.age = Integer.parseInt(input);
                if(newUser.age > 0) {
                    break;
                }
            }
            catch(Exception e) {
            }
        }
        StringBuilder msgBuild;
        ArrayList<String> tmpList;
        String[] interestArr = new String[0];
        interestLoop:
        while(!condCheck) {
            msgBuild = new StringBuilder();
            msgBuild.append("Interest? :");
            for(int i = 0; i < newUser.interest.length; i++) {
                if(newUser.interest[i].equals("")) {
                    break;
                }
                else {
                    int c = i + 1;
                    msgBuild.append("\n").append(c).append(" - ").append(newUser.interest[i]);
                }
            }

            msgBuild.append("\n\nCommand:")
                    .append("\nEnter - Submit")
                    .append("\na - Skip and create account (must enter at least one to skip)")
                    .append("\nr <number> - remove interest")
                    .append("\nq - Cancel")
                    .append("\n\nSearch result:");
            commandBox(msgBuild.toString(), interestArr);
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case 'A':
                    case 'a':
                        if(newUser.interest[0] != null) {
                            if(!newUser.interest[0].equals("")) {
                                break interestLoop;
                            }
                        }
                        break;

                    case 'q':
                        connection.sendToServer(false); //leave the register func
                        return false;
                }
            }
            else if(input.startsWith("r ")) {
                int index;
                try {
                    index = Integer.parseInt(input.substring(2)) - 1;
                    if(index < newUser.interest.length) {
                        String[] tmpArr = newUser.interest.clone();
                        for(int i = index; i < newUser.interest.length; i++) {
                            newUser.interest[i] = "";
                        }
                        System.arraycopy(tmpArr, index + 1, newUser.interest, index, newUser.interest.length - 1 - index);
                        tmpArr = null;
                    }
                    continue;
                }
                catch(Exception e) {
                }
            }
            int index;
            index = ClientConnection.checkInterest(input);
            if(index != -1) {
                interestArr = new String[0];
                String tmpInterest = ClientConnection.interestList.get(index);
                for(int i = 0; i < newUser.interest.length; i++) {
                    if(newUser.interest[i].equals(tmpInterest)) {
                        break;
                    }
                    if(newUser.interest[i].equals("")) {
                        newUser.interest[i] = tmpInterest;
                        break;
                    }
                }
            }
            else {
                tmpList = ClientConnection.searchInterest(input);
                interestArr = tmpList.toArray(new String[tmpList.size()]);
            }
        }
        updateLocationBox(newUser);
        connection.sendToServer(true);
        connection.uploadNewUserDataToServer(newUser);
        msg = "Congratulation! You have successfulll registered your account!"
              + "\nPlease login from main menu."
              + "\n\nCommand:"
              + "\nAny key - Back to main menu";
        commandBox(msg);
        input = sc.nextLine();
        return true;
    }

    public void registerWithTinder() {
        UserAcc newUser = null;
        boolean isUserNameCorrect = false;
        String msg;
        msg = "Please enter your Tinder Account username or phone number with country code (e.g.+60123456789):"
              + "\n\nCommand:"
              + "\nEnter - Submit"
              + "\nq - Back";
        userNameLoop:
        while(!isUserNameCorrect) {
            commandBox(msg);
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case 'q':
                        return;

                    default:
                        connection.sendToServer(input);
                }
            }
            else {
                connection.sendToServer(input);
            }
            if(!connection.booleanFromServer()) {
                msg = "Tinder Account does not exist or this account has been used."
                      + "\nPlease enter again:"
                      + "\n\nCommand:"
                      + "\nEnter - Submit"
                      + "\nq - Back";
            }
            else {
                isUserNameCorrect = true;
                break;
            }
        }
        //password auth
        msg = "Please enter the password:"
              + "\n\nCommand:"
              + "\nEnter - Submit"
              + "\nq - Back";
        passLoop:
        while(isUserNameCorrect) {
            commandBox(msg);
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case 'q':
                        return;

                    default:
                        connection.sendToServer(Encryption.encrypt(input, Encryption.key));
                }
            }
            else {
                connection.sendToServer(Encryption.encrypt(input, Encryption.key));
            }
            if(!connection.booleanFromServer()) {
                msg = "Password incorrect!"
                      + "\nPlease enter the password:"
                      + "\n\nCommand:"
                      + "\nEnter - Submit"
                      + "\nq - Back";
            }
            else { //login
                newUser = connection.downloadTinderUserDataFromServer();
                break passLoop;
            }
        }
        msg = "Your information from Tinder Account: "
              + "\nUsername: " + newUser.userName
              + "\nEmail: " + newUser.email
              + "\nAge: " + newUser.age
              + "\nGender: " + newUser.gender
              + "\nGender Preference: " + newUser.genderPreference
              + "\n\nCommand:"
              + "\ny - Agree and proceed to update other information"
              + "\nn - Refuse to create account";
        confirmLoop:
        while(true) {
            commandBox(msg);
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case 'Y':
                    case 'y':
                        connection.sendToServer(true);
                        break confirmLoop;

                    case 'N':
                    case 'n':
                        connection.sendToServer(false);
                        return;
                }
            }
        }
        StringBuilder msgBuild;
        ArrayList<String> tmpList;
        String[] interestArr = new String[0];
        interestLoop:
        while(true) {
            msgBuild = new StringBuilder();
            msgBuild.append("Interest? :");
            for(int i = 0; i < newUser.interest.length; i++) {
                if(newUser.interest[i].equals("")) {
                    break;
                }
                else {
                    int c = i + 1;
                    msgBuild.append("\n").append(c).append(" - ").append(newUser.interest[i]);
                }
            }

            msgBuild.append("\n\nCommand:")
                    .append("\nEnter - Submit")
                    .append("\na - Skip and create account (must enter at least one to skip)")
                    .append("\nr <number> - remove interest")
                    .append("\nq - Cancel")
                    .append("\n\nSearch result:");
            commandBox(msgBuild.toString(), interestArr);
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case 'A':
                    case 'a':
                        if(newUser.interest[0] != null) {
                            if(!newUser.interest[0].equals("")) {
                                break interestLoop;
                            }
                        }
                        break;

                    case 'q':
                        connection.sendToServer(false); //leave the register func
                        return;
                }
            }
            else if(input.startsWith("r ")) {
                int index;
                try {
                    index = Integer.parseInt(input.substring(2)) - 1;
                    if(index < newUser.interest.length) {
                        String[] tmpArr = newUser.interest.clone();
                        for(int i = index; i < newUser.interest.length; i++) {
                            newUser.interest[i] = "";
                        }
                        System.arraycopy(tmpArr, index + 1, newUser.interest, index, newUser.interest.length - 1 - index);
                        tmpArr = null;
                    }
                    continue;
                }
                catch(Exception e) {
                }
            }
            int index;
            index = ClientConnection.checkInterest(input);
            if(index != -1) {
                interestArr = new String[0];
                String tmpInterest = ClientConnection.interestList.get(index);
                for(int i = 0; i < newUser.interest.length; i++) {
                    if(newUser.interest[i].equals(tmpInterest)) {
                        break;
                    }
                    if(newUser.interest[i].equals("")) {
                        newUser.interest[i] = tmpInterest;
                        break;
                    }
                }
            }
            else {
                tmpList = ClientConnection.searchInterest(input);
                interestArr = tmpList.toArray(new String[tmpList.size()]);
            }
        }
        updateLocationBox(newUser);
        connection.sendToServer(true);
        connection.uploadNewUserDataToServer(newUser);
        msg = "Congratulation! You have successfulll registered your account!"
              + "\nPlease login from main menu."
              + "\n\nCommand:"
              + "\nAny key - Back to main menu";
        commandBox(msg);
        input = sc.nextLine();
    }

    public void registerWithTanTan() {
        UserAcc newUser = null;
        boolean isUserNameCorrect = false;
        String msg;
        msg = "Please enter your TanTan Account username or phone number with country code (e.g.+60123456789):"
              + "\n\nCommand:"
              + "\nEnter - Submit"
              + "\nq - Back";
        userNameLoop:
        while(!isUserNameCorrect) {
            commandBox(msg);
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case 'q':
                        return;

                    default:
                        connection.sendToServer(input);
                }
            }
            else {
                connection.sendToServer(input);
            }
            if(!connection.booleanFromServer()) {
                msg = "TanTan Account does not exist or this account has been used."
                      + "\nPlease enter again:"
                      + "\n\nCommand:"
                      + "\nEnter - Submit"
                      + "\nq - Back";
            }
            else {
                isUserNameCorrect = true;
                break;
            }
        }
        //password auth
        msg = "Please enter the password:"
              + "\n\nCommand:"
              + "\nEnter - Submit"
              + "\nq - Back";
        passLoop:
        while(isUserNameCorrect) {
            commandBox(msg);
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case 'q':
                        return;

                    default:
                        connection.sendToServer(Encryption.encrypt(input, Encryption.key));
                }
            }
            else {
                connection.sendToServer(Encryption.encrypt(input, Encryption.key));
            }
            if(!connection.booleanFromServer()) {
                msg = "Password incorrect!"
                      + "\nPlease enter the password:"
                      + "\n\nCommand:"
                      + "\nEnter - Submit"
                      + "\nq - Back";
            }
            else { //login
                newUser = connection.downloadTanTanUserDataFromServer();
                break passLoop;
            }
        }
        msg = "Your information from TanTan Account: "
              + "\nUsername: " + newUser.userName
              + "\nEmail: (none)"
              + "\nAge: " + newUser.age
              + "\nGender: " + newUser.gender
              + "\nGender Preference: " + newUser.genderPreference
              + "\n\nCommand:"
              + "\ny - Agree and proceed to update other information"
              + "\nn - Refuse to create account";
        confirmLoop:
        while(true) {
            commandBox(msg);
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case 'Y':
                    case 'y':
                        connection.sendToServer(true);
                        break confirmLoop;

                    case 'N':
                    case 'n':
                        connection.sendToServer(false);
                        return;
                }
            }
        }
        msg = "Email? (Cannot be changed later):"
              + "\n\nCommand:"
              + "\nEnter - Submit"
              + "\nq - Cancel";
        emailLoop:
        while(true) {
            commandBox(msg);
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case 'q':
                        connection.sendToServer(false); //leave the register func
                        return;
                }
            }
            if(input.endsWith(".com") && input.contains("@")) {
                newUser.email = input;
                break;
            }
            else {
                msg = "Email not acceptable!"
                      + "\nEmail? :"
                      + "\n\nCommand:"
                      + "\nEnter - Submit"
                      + "\nq - Cancel";
            }
        }
        StringBuilder msgBuild;
        ArrayList<String> tmpList;
        String[] interestArr = new String[0];
        interestLoop:
        while(true) {
            msgBuild = new StringBuilder();
            msgBuild.append("Interest? :");
            for(int i = 0; i < newUser.interest.length; i++) {
                if(newUser.interest[i].equals("")) {
                    break;
                }
                else {
                    int c = i + 1;
                    msgBuild.append("\n").append(c).append(" - ").append(newUser.interest[i]);
                }
            }

            msgBuild.append("\n\nCommand:")
                    .append("\nEnter - Submit")
                    .append("\na - Skip and create account (must enter at least one to skip)")
                    .append("\nr <number> - remove interest")
                    .append("\nq - Cancel")
                    .append("\n\nSearch result:");
            commandBox(msgBuild.toString(), interestArr);
            input = sc.nextLine();
            if(input.length() == 1) {
                switch(input.charAt(0)) {
                    case 'A':
                    case 'a':
                        if(newUser.interest[0] != null) {
                            if(!newUser.interest[0].equals("")) {
                                break interestLoop;
                            }
                        }
                        break;

                    case 'q':
                        connection.sendToServer(false); //leave the register func
                        return;
                }
            }
            else if(input.startsWith("r ")) {
                int index;
                try {
                    index = Integer.parseInt(input.substring(2)) - 1;
                    if(index < newUser.interest.length) {
                        String[] tmpArr = newUser.interest.clone();
                        for(int i = index; i < newUser.interest.length; i++) {
                            newUser.interest[i] = "";
                        }
                        System.arraycopy(tmpArr, index + 1, newUser.interest, index, newUser.interest.length - 1 - index);
                        tmpArr = null;
                    }
                    continue;
                }
                catch(Exception e) {
                }
            }
            int index;
            index = ClientConnection.checkInterest(input);
            if(index != -1) {
                interestArr = new String[0];
                String tmpInterest = ClientConnection.interestList.get(index);
                for(int i = 0; i < newUser.interest.length; i++) {
                    if(newUser.interest[i].equals(tmpInterest)) {
                        break;
                    }
                    if(newUser.interest[i].equals("")) {
                        newUser.interest[i] = tmpInterest;
                        break;
                    }
                }
            }
            else {
                tmpList = ClientConnection.searchInterest(input);
                interestArr = tmpList.toArray(new String[tmpList.size()]);
            }
        }
        updateLocationBox(newUser);
        connection.sendToServer(true);
        connection.uploadNewUserDataToServer(newUser);
        msg = "Congratulation! You have successfulll registered your account!"
              + "\nPlease login from main menu."
              + "\n\nCommand:"
              + "\nAny key - Back to main menu";
        commandBox(msg);
        input = sc.nextLine();
    }

    static void clearPage() {
        StringBuilder out = new StringBuilder();
        for(int i = 0; i < 15; i++) {
            out.append('\n');
        }
        System.out.println(out);
    }

}
