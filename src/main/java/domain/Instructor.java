package domain;

public class Instructor {
    private int userId;
    private String department;

    // Constructors
    public Instructor(int userId, String department) {
        this.userId = userId;
        this.department = department;
    }

    // Getters
    public int getUserId() { return userId; }
    public String getDepartment() { return department; }
}
