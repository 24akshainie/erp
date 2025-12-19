package api.student;

import java.util.Scanner;
import auth.session.SessionManager;
import service.StudentService;

public class StudentDashboard {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Student Dashboard (Console Mode) ===");

        if (!SessionManager.isActive()) {
            System.out.println("⚠️ No active session. Please log in first.");
            return;
        }

        System.out.println("👋 Welcome, " + SessionManager.getCurrentUsername() + " (" + SessionManager.getCurrentRole() + ")");

        Scanner sc = new Scanner(System.in);
        int choice;

        while (true) {
            System.out.println("\n========= MENU =========");
            System.out.println("1. View Course Catalog");
            System.out.println("2. View My Registrations");
            System.out.println("3. Register for a Course");
            System.out.println("4. Drop a Course");
            System.out.println("5. View My Timetable");
            System.out.println("6. View My Grades");
            System.out.println("7. Download Transcript");
            System.out.println("0. Logout");
            System.out.print("Choose an option: ");

            choice = sc.nextInt();

            switch (choice) {
                case 1 -> StudentService.viewCatalog();
                case 2 -> StudentService.viewRegistrations(SessionManager.getCurrentUserId());
                case 3 -> {
                    System.out.print("Enter Section ID to register: ");
                    int sid = sc.nextInt();
                    StudentService.register(SessionManager.getCurrentUserId(), sid);
                }
                case 4 -> {
                    System.out.print("Enter Section ID to drop: ");
                    int sid = sc.nextInt();
                    StudentService.drop(SessionManager.getCurrentUserId(), sid);
                }
                case 5 -> StudentService.viewTimetable(SessionManager.getCurrentUserId());
                case 6 -> StudentService.viewGrades(SessionManager.getCurrentUserId());
                case 7 -> StudentService.downloadTranscript(SessionManager.getCurrentUserId());
                case 0 -> {
                    System.out.println("👋 Logging out...");
                    SessionManager.endSession();
                    return;
                }
                default -> System.out.println("❌ Invalid choice. Try again.");
            }
        }
    }
}
