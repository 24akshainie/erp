package data;

import java.sql.*;
import java.util.*;
import domain.Instructor;

public class InstructorDAO {

    // Add new instructor to db
    public boolean addInstructor(Instructor i) {
        String sql = "INSERT INTO instructors(user_id, department) VALUES(?,?)";

        try (Connection c = DBConnection.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, i.getUserId());
            ps.setString(2, i.getDepartment());
            ps.executeUpdate();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get an instructor by user_id 
    public Instructor getByUserId(int userId) {
        String sql = "SELECT * FROM instructors WHERE user_id=?";

        try (Connection c = DBConnection.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return constructInstructor(rs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Get list of all instructors from the db
    public List<Instructor> getAllInstructors() {
        List<Instructor> list = new ArrayList<>();
        String sql = "SELECT * FROM instructors";

        try (Connection c = DBConnection.getErpConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(constructInstructor(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Update department of an instructor 
    public boolean updateInstructor(int userId, String department) {
        String sql = "UPDATE instructors SET department=? WHERE user_id=?";

        try (Connection c = DBConnection.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, department);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete an instructor from the db
    public boolean deleteInstructor(int userId) {
        String sql = "DELETE FROM instructors WHERE user_id=?";

        try (Connection c = DBConnection.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Helper: Create Instructor Object
    private Instructor constructInstructor(ResultSet rs) throws SQLException {
        return new Instructor(
            rs.getInt("user_id"),
            rs.getString("department")
        );
    }
}
