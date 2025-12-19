package domain;

public class Section {
    private int id;
    private String courseId;
    private int instructorId;
    private String dayTime;
    private String room;
    private int capacity;
    private String semester;
    private int year;

    // Constructor
    public Section(int id, String courseId, int instructorId, String dayTime, String room,
                   int capacity, String semester, int year) {
        this.id = id;
        this.courseId = courseId;
        this.instructorId = instructorId;
        this.dayTime = dayTime;
        this.room = room;
        this.capacity = capacity;
        this.semester = semester;
        this.year = year;
    }

    // Getters
    public int getId() { return id; }
    public String getCourseId() { return courseId; }
    public int getInstructorId() { return instructorId; }
    public String getDayTime() { return dayTime; }
    public String getRoom() { return room; }
    public int getCapacity() { return capacity; }
    public String getSemester() { return semester; }
    public int getYear() { return year; }

    // Setters 
    public void setId(int id) { this.id = id; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public void setInstructorId(int instructorId) { this.instructorId = instructorId; }
    public void setDayTime(String dayTime) { this.dayTime = dayTime; }
    public void setRoom(String room) { this.room = room; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setSemester(String semester) { this.semester = semester; }
    public void setYear(int year) { this.year = year; }
}
