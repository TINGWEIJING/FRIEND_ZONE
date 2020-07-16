package friendzone;
//For country and state list

import java.io.*;
import java.util.LinkedList;

/**
 * This class is used to store user account data.<br>
 * Class name may be prone to changing.
 *
 * @author MAHESH
 */
public class UserAcc implements Serializable {

    public static int INTEREST_SIZE = 10;
    public static int CONTACT_SIZE = 10;

    //declare variables used to store user data
    String userID, userName, password, email;
    char gender, genderPreference;
    int age;
    String country, state, postcode;
    double latitude, longitude;
    String[] interest = new String[INTEREST_SIZE];
    LinkedList<String> contact = new LinkedList<>();

    /**
     * This constructor WITH PARAMETERS is used to create a new user with basic
     * data.
     *
     * @param userID
     * @param userName
     * @param password
     */
    public UserAcc(String userID, String userName, String password, String email) {
        this.userID = userID;
        this.userName = userName;
        this.password = password;
        this.email = email;
    }

    public boolean addContact(String username) {
        if(contact.contains(username)) {
            return false;
        }
        boolean isContactAdded = true;
        contact.addFirst(username);
        if(contact.size() > CONTACT_SIZE) {
            contact.removeLast();
            isContactAdded = false;
        }
        return isContactAdded;
    }

    public String toDisplay() {
        return userID
               + "," + userName
               + "," + password
               + "," + email
               + "," + gender
               + "," + genderPreference
               + "," + age
               + "," + country
               + "," + state
               + "," + postcode
               + "," + latitude
               + "," + longitude
               + "," + interest[0]
               + "," + interest[1]
               + "," + interest[2]
               + "," + interest[3]
               + "," + interest[4]
               + "," + interest[5]
               + "," + interest[6]
               + "," + interest[7]
               + "," + interest[8]
               + "," + interest[9];

    }

    public String toExcel() {
        return userID
               + "\t" + userName
               + "\t" + password
               + "\t" + email
               + "\t" + gender
               + "\t" + genderPreference
               + "\t" + age
               + "\t" + country
               + "\t" + state
               + "\t" + postcode
               + "\t" + latitude
               + "\t" + longitude
               + "\t" + interest[0]
               + "\t" + interest[1]
               + "\t" + interest[2]
               + "\t" + interest[3]
               + "\t" + interest[4]
               + "\t" + interest[5]
               + "\t" + interest[6]
               + "\t" + interest[7]
               + "\t" + interest[8]
               + "\t" + interest[9];

    }

    @Override
    public String toString() {
        return "UserAcc{"
               + "userID=" + userID
               + ", userName=" + userName
               + ", password=" + password
               + ", email=" + email
               + ", gender=" + gender
               + ", genderPreference=" + genderPreference
               + ", age=" + age
               + ", country=" + country
               + ", state=" + state
               + ", postcode=" + postcode
               + ", latitude=" + latitude
               + ", longitude=" + longitude
               + ", interest=" + interest[0]
               + ", interest=" + interest[1]
               + ", interest=" + interest[2]
               + ", interest=" + interest[3]
               + ", interest=" + interest[4]
               + ", interest=" + interest[5]
               + ", interest=" + interest[6]
               + ", interest=" + interest[7]
               + ", interest=" + interest[8]
               + ", interest=" + interest[9]
               + '}';
    }

}
