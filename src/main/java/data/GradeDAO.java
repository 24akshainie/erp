package data;
import java.sql.*;
import java.util.*;
import domain.Grade;

public class GradeDAO {

    // Add a new grade record for a student in a specific section
    public boolean addGrade(Grade g) throws Exception {
        String sql = "INSERT INTO grades(student_id, section_id, component, score) VALUES(?,?,?,?)";
        try (Connection c = DBConnection.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, g.getStudentId());
            ps.setInt(2, g.getSectionId());
            ps.setString(3, g.getComponent()); // quiz, midsem,endsem
            ps.setDouble(4, g.getScore());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { 
            e.printStackTrace(); return false; 
        }
    }

    // Get all student's grade for a specific section
    public List<Grade> getGradesBySection(int sectionId) {
        List<Grade> list = new ArrayList<>();
        String sql = "SELECT * FROM grades WHERE section_id=?";
        try (Connection c = DBConnection.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {  // Convert each row into a Grade object
                list.add(new Grade(
                    rs.getInt("enrollment_id"),
                    rs.getInt("student_id"),
                    rs.getInt("section_id"),
                    rs.getString("component"),
                    rs.getDouble("score"),
                    rs.getString("final_grade")
                ));
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return list;
    }

    // Updates the final grade for a student in a section
    public boolean updateFinalGrade(int studentId, int sectionId, String finalGrade) {
        String sql = "UPDATE grades SET final_grade=? WHERE student_id=? AND section_id=?";
        try (Connection c = DBConnection.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, finalGrade);
            ps.setInt(2, studentId);
            ps.setInt(3, sectionId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { 
            e.printStackTrace(); return false; 
        }
    }
}
