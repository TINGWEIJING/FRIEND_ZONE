package friendzone;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Adapter Class to allow users to sign up using TanTan Acc <br>
 * TanTanAcc class extracts user details and aplies them to UserAcc Class <br>
 *
 * @Li Yan Yee
 */
public class TanTanAcc implements Serializable {

    String phoneNumber, password, userName, location;
    char gender, genderPreference;
    int age;

    public TanTanAcc(String phoneNumber,
            String password,
            String name,
            char gender,
            char genderPreference,
            int age,
            String location) {
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.userName = name;
        this.gender = gender;
        this.genderPreference = genderPreference;
        this.age = age;
        this.location = location;
    }

    /**
     * Convert Birthdate to Age
     *
     * @param birthdate
     * @return age
     */
    public static int birthdateToAge(String birthdate) {
        String[] str = birthdate.split("-");
        int age = 0;
        int year = Calendar.getInstance().get(Calendar.YEAR);
        if(str.length != 1) {
            age = year - Integer.parseInt(str[2]);
        }
        return age;
    }

    @Override
    public String toString() {
        return "TanTanAcc{"
               + "phoneNumber=" + phoneNumber
               + ", password=" + password
               + ", name=" + userName
               + ", gender=" + gender
               + ", genderPreference=" + genderPreference
               + ", location=" + location
               + ", age=" + age + '}';
    }

}
