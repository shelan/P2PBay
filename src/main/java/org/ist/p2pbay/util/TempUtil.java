
/*
 *
 *  * Copyright 2014
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.ist.p2pbay.util;

import org.ist.p2pbay.data.User;
import org.ist.p2pbay.exception.P2PBayException;
import org.ist.p2pbay.manager.UserManager;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * To be deleted once the project is at final stage. Only methods for testing
 */
public class TempUtil {

    public static void addUser(UserManager userManager) throws NoSuchAlgorithmException, UnsupportedEncodingException,
            P2PBayException {
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
