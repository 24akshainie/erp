package data;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import domain.Enrollment;

public class EnrollmentDAO {

    // Register a student in a section
    public boolean register(Enrollment e) throws Exception {

        String insertSql = 
            "INSERT INTO enrollments(student_id, section_id, status) VALUES(?,?,?)";

        String updateSectionSql = 
            "UPDATE sections SET current_enrolled = current_enrolled + 1 " +
            "WHERE id = ? AND current_enrolled < capacity";

        try (Connection c = DBConnection.getErpConnection()) {

            // Insert enrollment record
            try (PreparedStatement ps = c.prepareStatement(insertSql)) {

                ps.setInt(1, e.getStudentId());
                ps.setInt(2, e.getSectionId());
                ps.setString(3, e.getStatus());

                int rows = ps.executeUpdate();

                if (rows == 0) {
                    return false; // insert failed
                }
            } 
            catch (SQLException ex) {

                if (ex.getMessage().contains("Duplicate entry")) {
                    return false;
                }

                if (ex.getMessage().contains("Cannot add or update a child row")) {
                    return false;
                }

                ex.printStackTrace();
                return false;
            }

            // Increase section enrolled count (only if capacity allows)
            try (PreparedStatement ps2 = c.prepareStatement(updateSectionSql)) {

                ps2.setInt(1, e.getSectionId());
                int updated = ps2.executeUpdate();

                if (updated == 0) {
                    return false;
                }
            }

            return true; // registration successful
        }
    }


    // Drop a student from a section
    public boolean drop(int studentId, int sectionId) {

        String checkStudent = "SELECT 1 FROM students WHERE user_id = ?";
        String checkSection = "SELECT 1 FROM sections WHERE id = ?";
        String deleteSQL = "DELETE FROM enrollments WHERE student_id=? AND section_id=?";
        String updateSectionSql =
                "UPDATE sections SET current_enrolled = current_enrolled - 1 " +
                "WHERE id = ? AND current_enrolled > 0";

        try (Connection c = DBConnection.getErpConnection()) {

            // Verify if student exsists
            try (PreparedStatement ps = c.prepareStatement(checkStudent)) {
                ps.setInt(1, studentId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    return false;
                }
            }

            // Verify if section exsists
            try (PreparedStatement ps = c.prepareStatement(checkSection)) {
                ps.setInt(1, sectionId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    return false;
                }
            }

            // Delete Enrollment Record
            int rows;
            try (PreparedStatement ps = c.prepareStatement(deleteSQL)) {

                ps.setInt(1, studentId);
                ps.setInt(2, sectionId);

                rows = ps.executeUpdate();
            }

            if (rows > 0) {
                // Decrease section enrolled count

                try (PreparedStatement ps = c.prepareStatement(updateSectionSql)) {
                    ps.setInt(1, sectionId);
                    ps.executeUpdate();
                }

                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // Check if a student is already enrolled in a section
    public boolean isEnrolled(int studentId, int sectionId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE student_id=? AND section_id=?";

        try (Connection c = DBConnection.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, sectionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    
    // Count total students enrolled in a section
    public int countEnrollments(int sectionId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE section_id=?";
        try (Connection c = DBConnection.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    
    // Get all enrollments for a student with section details
    public List<Enrollment> getEnrollmentsByStudent(int studentId) {
        List<Enrollment> list = new ArrayList<>();
        String sql = """
            SELECT e.student_id, e.section_id, e.status,
                s.course_id, s.instructor_id, s.day_time,
                s.room, s.capacity, s.semester, s.year
            FROM enrollments e
            JOIN sections s ON e.section_id = s.id
            WHERE e.student_id = ?
        """;

        try (Connection c = DBConnection.getErpConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Enrollment e = new Enrollment(
                        rs.getInt("student_id"),
                        rs.getInt("section_id"),
                        rs.getString("status")
                    );
                    e.setCourseId(rs.getString("course_id"));
                    e.setInstructorId(rs.getInt("instructor_id"));
                    e.setDayTime(rs.getString("day_time"));
                    e.setRoom(rs.getString("room"));
                    e.setCapacity(rs.getInt("capacity"));
                    e.setSemester(rs.getString("semester"));
                    e.setYear(rs.getInt("year"));
                    list.add(e);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return list;
    }

}
