package friendzone;

import java.io.*;
import java.security.KeyStore;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;


/**
 * This class is used to implement ENCRYPTION feature.<br>
 * the algorithm used is java Advanced Encryption Standard (AES).<br>
 * Class name may be prone to changing.<br>
 * The implemented method may be merged with other class in future.<br>
 *
 * What is AES?<br>
 * Explain in animation: https://www.youtube.com/watch?v=gP4PqVGudtg<br>
 *
 * Brief explanation: https://www.youtube.com/watch?v=WLm5_cxeywY<br>
 *
 * Document explanation:
 * https://www.tutorialspoint.com/cryptography/advanced_encryption_standard.htm<br>
 *
 *
 * @author WONG WEI LIANG
 */
public class Encryption {

    public static SecretKey key;
    private static final String KEY_FILENAME = "data/key.dat";
    private static char[] password = "password".toCharArray();
    
    /**
     * this method is used to generate a key.(the key size is 256 bits)
     *
     * @return
     */
    public static SecretKey keygen() {
        try {
            KeyGenerator genkey = KeyGenerator.getInstance("AES");
            genkey.init(256);
            SecretKey seckey = genkey.generateKey();
            return seckey;
        }
        catch(Exception e) {
            return null;
        }
    }

    public static SecretKey loadkey() {
        try {
            KeyStore ks = KeyStore.getInstance("JCEKS");
            ks.load(new FileInputStream(new File(KEY_FILENAME)), password);
            KeyStore.ProtectionParameter entrypassword = new KeyStore.PasswordProtection(password);
            KeyStore.SecretKeyEntry keyEntry = (KeyStore.SecretKeyEntry) ks.getEntry("secretkeyallias", entrypassword);
            SecretKey mysecretkey = keyEntry.getSecretKey();
            return mysecretkey;
        }
        catch(Exception e) {
            return null;
        }
    }

    public static void storekey(SecretKey seckey) {
        try {
            KeyStore ks = KeyStore.getInstance("JCEKS");
            ks.load(null, password);
            KeyStore.ProtectionParameter entrypassword = new KeyStore.PasswordProtection(password);
            KeyStore.SecretKeyEntry secretkeyentry = new KeyStore.SecretKeyEntry(seckey);
            ks.setEntry("secretkeyallias", secretkeyentry, entrypassword);
            ks.store(new FileOutputStream(KEY_FILENAME), password);
        }
        catch(Exception e) {
        }
    }

    /**
     * To encrypt the message.<br>
     * <p>
     * key and iv are needed in cbc mode. create an object of cipher class.
     * inside the getinstance is the algorithm used ,mode of operation and
     * padding scheme. initiate cipher object to encrypt Reference:
     *
     * @param plainText Plain text
     * @return Cipher text
     */
    public static String encrypt(String plainText, SecretKey seckey) {
        try {
            byte[] iv = new byte[16];
            IvParameterSpec ivsp = new IvParameterSpec(iv);
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, seckey, ivsp);
            byte[] plaintext = plainText.getBytes("UTF-8");
            byte[] ciphertext = c.doFinal(plaintext);

            return Base64.getEncoder().encodeToString(ciphertext);

        }
        catch(Exception e) {
            System.out.println("Error while encrypting:" + e.toString());
        }
        return null;
    }

    /**
     * To decrypt the message.<br>
     * <p>
     * same as above, just change the initiate method to decrypt. Reference:
     *
     * @param cipherText Cipher text
     * @return Plain text
     */
    public static String decrypt(String cipherText, SecretKey seckey) {
        try {
            byte[] iv = new byte[16];
            IvParameterSpec ivsp = new IvParameterSpec(iv);
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE, seckey, ivsp);
            byte[] ciphertext = Base64.getDecoder().decode(cipherText);
            byte[] plaintext = c.doFinal(ciphertext);

            return new String(plaintext);

        }
        catch(Exception e) {
            System.out.println("Error while decrypting:" + e.toString());
        }

        return null;
    }

}
