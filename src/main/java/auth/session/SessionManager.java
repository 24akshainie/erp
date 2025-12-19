package auth.session;

public class SessionManager {

    // Stores current user's session data
    private static int userId = -1;
    private static String username = null;
    private static String role = null;
    private static boolean active = false;

    // Start a session
    public static void startSession(int uid, String uname, String r) {
        userId = uid;
        username = uname;
        role = r;
        active = true;
    }

    // End a session - clear the session data on logout
    public static void endSession() {
        userId = -1;
        username = null;
        role = null;
        active = false;
    }

    // Check whether a user is currently logged in
    public static boolean isActive() { 
        return active; 
    }

    // Return current user's ID
    public static int getCurrentUserId() {
        return userId;
    }

    // Return current user's username
    public static String getCurrentUsername() { 
        return username; 
    }

    // Return current user's role
    public static String getCurrentRole() {
        return role;
    }

    // Check if current user is 'Admin'
    public static boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    // Check if current user is 'Instructor'
    public static boolean isInstructor() {
        return "INSTRUCTOR".equalsIgnoreCase(role);
    }

    // Check if current user is 'Student'
    public static boolean isStudent() {
        return "STUDENT".equalsIgnoreCase(role);
    }
}
