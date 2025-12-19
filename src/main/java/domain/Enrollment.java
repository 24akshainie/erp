package domain;

public class Enrollment {
    private int studentId;
    private int sectionId;
    private String status;
    private String courseId;

    // Section info
    private int instructorId;
    private String dayTime;
    private String room;
    private int capacity;
    private String semester;
    private int year;

    // Constructor
    public Enrollment(int studentId, int sectionId, String status) {
        this.studentId = studentId;
        this.sectionId = sectionId;
        this.status = status;
    }

    // Getters 
    public int getStudentId() { return studentId; }
    public int getSectionId() { return sectionId; }
    public String getStatus() { return status; }
    public String getCourseId() { return courseId; }
    public int getInstructorId() { return instructorId; }
    public String getDayTime() { return dayTime; }
    public String getRoom() { return room; }
    public int getCapacity() { return capacity; }
    public String getSemester() { return semester; }
    public int getYear() { return year; }

    // Setters
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }
    public void setStatus(String status) { this.status = status; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public void setInstructorId(int instructorId) { this.instructorId = instructorId; }
    public void setDayTime(String dayTime) { this.dayTime = dayTime; }
    public void setRoom(String room) { this.room = room; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setSemester(String semester) { this.semester = semester; }
    public void setYear(int year) { this.year = year; }

}
