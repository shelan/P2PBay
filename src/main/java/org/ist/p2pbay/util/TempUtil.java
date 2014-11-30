package org.ist.p2pbay.util;

import org.ist.p2pbay.data.User;
import org.ist.p2pbay.manager.UserManager;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * To be deleted once the project is at final stage. Only methods for testing
 */
public class TempUtil {

    public static void addUser(UserManager userManager) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        /////// this is a temp code to create a USER_DOMAIN for testing
        // username : USER_DOMAIN
        //password : 123
            System.out.println(" adding test USER_DOMAIN ......");
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update("123".getBytes("UTF-8"));
            // password to store
            byte[] passwordDigest = md.digest();
            User user = new User("user",passwordDigest);
            userManager.addUser("user", user);

    }

}
