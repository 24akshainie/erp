package domain;

public class Course {
    private String code, title;
    private int credits;

    // Constructor
    public Course(String code, String title, int credits) {
        this.code = code;
        this.title = title;
        this.credits = credits;
    }

    // Getters
    public String getCode() { return code; }
    public String getTitle() { return title; }
    public int getCredits() { return credits; }

    @Override
    public String toString() {
        return code + " - " + title + " (" + credits + " cr)";
    }
}
