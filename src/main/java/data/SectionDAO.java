package data;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import domain.Section;
import data.EnrollmentDAO;

public class SectionDAO {

    // Add a new section to the db
    public boolean addSection(String courseId, int instructorId, String dayTime, String room,
                              int capacity, String semester, int year) {

        String sql = "INSERT INTO sections(course_id, instructor_id, day_time, room, capacity, semester, year) "
                   + "VALUES(?,?,?,?,?,?,?)";

        try (Connection c = DBConnection.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, courseId);
            ps.setInt(2, instructorId);
            ps.setString(3, dayTime);
            ps.setString(4, room);
            ps.setInt(5, capacity);
            ps.setString(6, semester);
            ps.setInt(7, year);

            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Assign instructor to an exsisting section
    public boolean assignInstructor(int sectionId, int instructorId) {

        String sql = "UPDATE sections SET instructor_id=? WHERE id=?";

        try (Connection c = DBConnection.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, instructorId);
            ps.setInt(2, sectionId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all sections from the db
    public List<Section> getAllSections() {

        List<Section> list = new ArrayList<>();
        String sql = "SELECT * FROM sections";

        try (Connection c = DBConnection.getErpConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Section s = constructSection(rs);
                list.add(s);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Get all sections for a specific course
    public List<Section> getSectionsByCourse(String courseId) {

        List<Section> list = new ArrayList<>();
        String sql = "SELECT * FROM sections WHERE course_id = ?";

        try (Connection c = DBConnection.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, courseId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(constructSection(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Get section by it's ID
    public Section getById(int sectionId) {

        String sql = "SELECT * FROM sections WHERE id = ?";

        try (Connection c = DBConnection.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, sectionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return constructSection(rs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Get sections available for a student to enroll
    // (not already enrolled and with seats available)
    public List<Section> getAvailableSectionsForStudent(int studentId) {
        List<Section> list = new ArrayList<>();
        String sql = """
        SELECT s.*
        FROM sections s
        LEFT JOIN registrations r ON s.id = r.section_id AND r.student_id=?
        WHERE r.id IS NULL AND
            (SELECT COUNT(*) FROM registrations r2 WHERE r2.section_id = s.id) < s.capacity
    """;


        try (Connection c = DBConnection.getErpConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Section(
                        rs.getInt("id"),
                        rs.getString("course_id"),
                        rs.getInt("instructor_id"),
                        rs.getString("day_time"),
                        rs.getString("room"),
                        rs.getInt("capacity"),
                        rs.getString("semester"),
                        rs.getInt("year")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Get total number of students enrolled in a section.
    public int getEnrolledCount(int sectionId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE section_id = ?";
        try (Connection conn = DBConnection.getErpConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Helper: Create Section object
    private Section constructSection(ResultSet rs) throws SQLException {
        Section s = new Section(0, null, 0, null, null, 0, null, 0);
        s.setId(rs.getInt("id"));
        s.setCourseId(rs.getString("course_id"));
        s.setInstructorId(rs.getInt("instructor_id"));
        s.setDayTime(rs.getString("day_time"));
        s.setRoom(rs.getString("room"));
        s.setCapacity(rs.getInt("capacity"));
        s.setSemester(rs.getString("semester"));
        s.setYear(rs.getInt("year"));
        return s;
    }

}
