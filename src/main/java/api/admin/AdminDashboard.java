package api.admin;

import java.util.Scanner;
import auth.session.SessionManager;
import service.AdminService;

public class AdminDashboard {
    public static void main(String[] args) throws Exception {
        if (!SessionManager.isActive()) {
            System.out.println("⚠️ No active session. Please log in first.");
            return;
        }

        System.out.println("=== Admin Dashboard ===");
        System.out.println("Welcome, " + SessionManager.getCurrentUsername());

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("""
                \n1. Add User
                2. Add Course
                3. Add Section
                4. Assign Instructor
                5. Toggle Maintenance
                6. Exit
                """);
            System.out.print("Choose: ");
            int ch = sc.nextInt();

            if (ch == 1) {
                System.out.print("User ID: "); int uid = sc.nextInt();
                System.out.print("Username: "); String uname = sc.next();
                System.out.print("Role: "); String role = sc.next();
                System.out.print("Password: "); String pwd = sc.next();
                AdminService.addUser(uid, uname, role, pwd);
            } else if (ch == 2) {
                System.out.print("Course code: "); String code = sc.next();
                System.out.print("Title: "); String title = sc.next();
                System.out.print("Credits: "); int cr = sc.nextInt();
                AdminService.addCourse(code, title, cr);
            } else if (ch == 3) {
                System.out.print("Course ID: "); String cid = sc.next();
                System.out.print("Instructor ID: "); int iid = sc.nextInt();
                System.out.print("Day/Time: "); String dt = sc.next();
                System.out.print("Room: "); String room = sc.next();
                System.out.print("Capacity: "); int cap = sc.nextInt();
                System.out.print("Semester: "); String sem = sc.next();
                System.out.print("Year: "); int year = sc.nextInt();
                AdminService.addSection(cid, iid, dt, room, cap, sem, year);
            } else if (ch == 4) {
                System.out.print("Section ID: "); int sid = sc.nextInt();
                System.out.print("Instructor ID: "); int iid = sc.nextInt();
                AdminService.assignInstructor(sid, iid);
            } else if (ch == 5) {
                System.out.print("Turn maintenance ON (true/false): ");
                boolean on = sc.nextBoolean();
                AdminService.toggleMaintenance(on);
            } else {
                System.out.println("👋 Logging out...");
                SessionManager.endSession();
                break;
            }
        }
        sc.close();
    }
}
