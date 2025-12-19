package auth.hash;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    // convert password into Bcrypt hash form
    public static String hashPassword(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt());
    }

    // compares the plain password after hashing with stored hash
    public static boolean verify(String plain, String hashed) {
        return BCrypt.checkpw(plain, hashed);
    }
}
