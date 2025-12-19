package api.instructor;

import java.util.Scanner;
import auth.session.SessionManager;
import service.InstructorService;

public class InstructorDashboard {
    public static void main(String[] args) throws Exception {
        if (!SessionManager.isActive()) {
            System.out.println("⚠️ No active session. Please log in first.");
            return;
        }

        System.out.println("=== Instructor Dashboard ===");
        System.out.println("Welcome, " + SessionManager.getCurrentUsername());

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n1. Enter Score\n2. View Gradebook\n3. Compute Final Grades\n4. Exit");
            System.out.print("Choose: ");
            int ch = sc.nextInt();

            if (ch == 1) {
                System.out.print("Student ID: "); int sid = sc.nextInt();
                System.out.print("Section ID: "); int sec = sc.nextInt();
                System.out.print("Component: "); String comp = sc.next();
                System.out.print("Score: "); double score = sc.nextDouble();
                InstructorService.enterScore(SessionManager.getCurrentUserId(), sid, sec, comp, score);
            } else if (ch == 2) {
                System.out.print("Section ID: "); int sec = sc.nextInt();
                InstructorService.viewGradebook(sec);
            } else if (ch == 3) {
                System.out.print("Section ID: "); int sec = sc.nextInt();
                InstructorService.computeFinalGrades(sec);
            } else {
                System.out.println("👋 Logging out...");
                SessionManager.endSession();
                break;
            }
        }
        sc.close();
    }
}
