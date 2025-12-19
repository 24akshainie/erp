package domain;

public class Student {
    private int userId;
    private String rollNo;
    private String program;
    private int year;

    // Constructor
    public Student(int userId, String rollNo, String program, int year) {
        this.userId = userId;
        this.rollNo = rollNo;
        this.program = program;
        this.year = year;
    }

    // Getters
    public int getUserId() { return userId; }
    public String getRollNo() { return rollNo; }
    public String getProgram() { return program; }
    public int getYear() { return year; }

    @Override
    public String toString() {
        return rollNo + " (" + program + " Y" + year + ")";
    }
}
