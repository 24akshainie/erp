package service;

import auth.session.SessionManager;
import auth.store.AuthDAO;

public class AuthService {
    private final AuthDAO dao = new AuthDAO();

    //Attempts user login.
    //Returns 1 if successful, 0 if username not found, -1 if password is incorrect.
    
    public int login(String username, String password) throws Exception {
        int uid = dao.validateLogin(username, password);

        if (uid == 0) { // username not found
            return 0;
        } else if (uid == -1) { // wrong password
            return -1;
        } else {
            // login successful. start session
            String role = dao.getRoleByUserId(uid);
            SessionManager.startSession(uid, username, role);
            return 1;
        }
    }

    public void logout() {
        SessionManager.endSession();
    }
}
