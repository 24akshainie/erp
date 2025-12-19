package data;

import domain.Student;
import java.sql.*;

public class StudentDAO {

    // Add new student to db
    public boolean add(Student s) throws Exception {
        String sql = "INSERT INTO students (user_id, roll_no, program, year) VALUES (?, ?, ?, ?)";
        try (Connection c = DBConnection.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, s.getUserId());
            ps.setString(2, s.getRollNo());
            ps.setString(3, s.getProgram());
            ps.setInt(4, s.getYear());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get student by user ID
    public Student getStudentByUserId(int id) throws Exception {
        String sql = "SELECT * FROM students WHERE user_id=?";
        try (Connection c = DBConnection.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Student(
                        rs.getInt("user_id"),
                        rs.getString("roll_no"),
                        rs.getString("program"),
                        rs.getInt("year"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // check if a student exists for given user ID
    public boolean exists(int userId) throws Exception {
        String sql = "SELECT user_id FROM students WHERE user_id=?";
        try (Connection c = DBConnection.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
