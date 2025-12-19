package data;

import java.sql.*;
import java.util.*;
import domain.Course;

public class CourseDAO {

    // Get list of all courses from db
    public List<Course> getAllCourses() {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM courses";

        try (Connection c = DBConnection.getErpConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(constructCourse(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    // Get a single course using its unique course code
    public Course getCourseByCode(String code) {
        String sql = "SELECT * FROM courses WHERE code = ?";

        try (Connection c = DBConnection.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, code);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return constructCourse(rs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // course not found
    }


    // Search courses by keyword in code or title
    public List<Course> searchCourses(String query) {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM courses WHERE code LIKE ? OR title LIKE ?";

        try (Connection c = DBConnection.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            String pattern = "%" + query + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);

            // Add matching courses to list
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(constructCourse(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    // Add a new course
    public boolean addCourse(Course c) throws Exception {
        String sql = "INSERT INTO courses(code, title, credits) VALUES(?,?,?)";

        try (Connection con = DBConnection.getErpConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getCode());
            ps.setString(2, c.getTitle());
            ps.setInt(3, c.getCredits());
            ps.executeUpdate();

            return true; // added successfully

        } catch (SQLException e) {
            // handles duplicate course entry
            if (e.getMessage().contains("Duplicate entry")) {} 
            else {
                e.printStackTrace();
            }
            return false;
        }
    }


    // Update course title and credits
    public boolean updateCourse(String code, String title, int credits) {
        String sql = "UPDATE courses SET title=?, credits=? WHERE code=?";

        try (Connection c = DBConnection.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setInt(2, credits);
            ps.setString(3, code);

            return ps.executeUpdate() > 0; // returns true if row updated

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // Delete a course using course code
    public boolean deleteCourse(String code) {
        String sql = "DELETE FROM courses WHERE code=?";

        try (Connection c = DBConnection.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, code);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // Helper: Create Course object
    private Course constructCourse(ResultSet rs) throws SQLException {
        return new Course(
                rs.getString("code"),
                rs.getString("title"),
                rs.getInt("credits")
        );
    }
}