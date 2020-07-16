package friendzone;

import java.util.*;

/**
 * A tester class used to test the method. Free to use.
 *
 *
 */
public class test {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        Encryption.key = Encryption.loadkey();
//        ServerFileStoring.convertTanTanUserListCSVtoObjFile();
//        ServerFileStoring.convertTinderUserListCSVtoObjFile();
//        ServerFileStoring.convertUserListCSVtoObjFile();
//        ClientConnection.convertCountryCSVtoObjFile();
//        ClientConnection.convertInterestCSVtoObjFile();
        ClientConnection.convertOppositeCSVtoObjFile();
//        ClientConnection.convertPostcodeCSVtoObjFile();
    }

}
