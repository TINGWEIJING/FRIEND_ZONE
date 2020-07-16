/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package friendzone;

import java.io.Serializable;
import java.util.Calendar;
/**
 * Adapter Class to allow users to sign up using Tinder Acc <br>
 * TinderAcc class extracts user details and aplies them to UserAcc Class <br>
 *
 * @Li Yan Yee
 */
public class TinderAcc implements Serializable {

    String phoneNumber, email, password, userName, location;
    char gender, genderPreference;
    int age;

    public TinderAcc(String phoneNumber,
            String email,
            String password,
            String name,
            int age,
            char gender,
            char genderPreference,
            String location) {
        this.phoneNumber = phoneNumber;
        this.userName = name;
        this.email = email;
        this.password = password;
        this.age = age;
        this.gender = gender;
        this.genderPreference = genderPreference;
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
        return "TinderAcc{"
               + "phoneNumber=" + phoneNumber
               + ", email=" + email
               + ", password=" + password
               + ", name=" + userName
               + ", gender=" + gender
               + ", genderPreference=" + genderPreference
               + ", location=" + location
               + ", age=" + age + '}';
    }

}
