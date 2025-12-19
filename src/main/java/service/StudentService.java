package service;

import access.AccessControl;
import data.*;
import domain.*;
import java.sql.*;
import java.util.List;
import auth.session.SessionManager;

public class StudentService {

    // View list of all available courses
    public static void viewCatalog() {
        if (!AccessControl.isAllowed("Student", "view_catalog")) return;
        List<Course> list = new CourseDAO().getAllCourses();
    }

    // Register a student into a specific section
    public static void register(int studentId, int sectionId) throws Exception {
        if (!AccessControl.isAllowed("Student", "register")) return;
        Enrollment e = new Enrollment(studentId, sectionId, "ENROLLED");
        new EnrollmentDAO().register(e);
    }

    // Drop a student from a registered section
    public static void drop(int studentId, int sectionId) {
        if (!AccessControl.isAllowed("Student", "drop")) return;
        new EnrollmentDAO().drop(studentId, sectionId);
    }

    // Display the timetable of sections the student is enrolled in
    public static void viewTimetable(int studentId) {
        if (!AccessControl.isAllowed("Student", "view_timetable")) return;

        String sql = """
            SELECT s.section_id, s.course_id, s.day_time, s.room, s.semester, s.year
            FROM enrollments e
            JOIN sections s ON e.section_id = s.section_id
            WHERE e.student_id = ?
        """;
        try (Connection c = DBConnection.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            System.out.println("\nYour Timetable:");
            while (rs.next()) {
                System.out.printf("Section %d | %s | %s | %s | %s %d%n",
                        rs.getInt("section_id"), rs.getString("course_id"),
                        rs.getString("day_time"), rs.getString("room"),
                        rs.getString("semester"), rs.getInt("year"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // View final grades
    public static void viewGrades(int studentId) {
        if (!AccessControl.isAllowed("Student", "view_grades")) return;

        String sql = """
            SELECT c.code, c.title, g.final_grade
            FROM grades g
            JOIN sections s ON g.section_id = s.section_id
            JOIN courses c ON s.course_id = c.code
            WHERE g.student_id = ?
        """;
        try (Connection c = DBConnection.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            System.out.println("\n🎯 Your Grades:");
            boolean any = false;
            while (rs.next()) {
                any = true;
                System.out.printf("%s - %s: %s%n",
                        rs.getString("code"), rs.getString("title"), rs.getString("final_grade"));
            }
            if (!any) System.out.println("No grades available yet.");
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Download transcript (CSV export)
    public static void downloadTranscript(int studentId) {
        if (!AccessControl.isAllowed("Student", "csv_transcript")) return;

        String fileName = "Transcript_" + SessionManager.getCurrentUsername() + ".csv";
        String sql = """
            SELECT DISTINCT c.code, c.title, s.semester, s.year, g.final_grade
            FROM grades g
            JOIN sections s ON g.section_id = s.section_id
            JOIN courses c ON s.course_id = c.code
            WHERE g.student_id = ?
            ORDER BY s.year, s.semester
        """;
        try (Connection conn = DBConnection.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             java.io.FileWriter fw = new java.io.FileWriter(fileName)) {

            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            fw.write("Course Code,Title,Semester,Year,Final Grade\n");
            while (rs.next()) {
                fw.write(String.format("%s,%s,%s,%d,%s\n",
                        rs.getString("code"),
                        rs.getString("title"),
                        rs.getString("semester"),
                        rs.getInt("year"),
                        rs.getString("final_grade")));
            }
            fw.close();
            System.out.println("Transcript saved as " + fileName);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Display all courses the student is currently registered in
    public static void viewRegistrations(int studentId) {
        if (!AccessControl.isAllowed("Student", "view_registrations")) return;
        String sql = """
            SELECT e.section_id, c.code, c.title, s.semester, s.year
            FROM enrollments e
            JOIN sections s ON e.section_id = s.section_id
            JOIN courses c ON s.course_id = c.code
            WHERE e.student_id = ?
        """;
        try (Connection conn = DBConnection.getErpConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n Current Registrations:");
            boolean any = false;
            while (rs.next()) {
                any = true;
                System.out.printf("Section %d | %s - %s | %s %d%n",
                        rs.getInt("section_id"),
                        rs.getString("code"),
                        rs.getString("title"),
                        rs.getString("semester"),
                        rs.getInt("year"));
            }
            if (!any) System.out.println("You are not registered in any sections.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
