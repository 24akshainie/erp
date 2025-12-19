import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestConn {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/erp_db";
        String user = "root";
        String password = ""; 

        String sql = """
            SELECT 
                s.id AS section_id,
                c.code AS course_code,
                s.instructor_id,
                s.day_time,
                s.room,
                s.capacity,
                s.semester,
                s.year
            FROM sections s
            JOIN courses c ON s.course_id = c.code
        """;

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("Sections in ERP DB:");
            while (rs.next()) {
                System.out.printf("ID: %d, Course: %s, Instructor: %s, Time: %s, Room: %s, Capacity: %d, %s %d%n",
                        rs.getInt("section_id"),
                        rs.getString("course_code"),
                        rs.getObject("instructor_id"), // could be null
                        rs.getString("day_time"),
                        rs.getString("room"),
                        rs.getInt("capacity"),
                        rs.getString("semester"),
                        rs.getInt("year"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
