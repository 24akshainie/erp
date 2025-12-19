package domain;

public class Grade {
    private int enrollmentId, studentId, sectionId;
    private String component, finalGrade;
    private double score;

    // Constructors
    public Grade(int studentId, int sectionId, String component, double score) {
        this.studentId = studentId;
        this.sectionId = sectionId;
        this.component = component;
        this.score = score;
    }

    public Grade(int enrollmentId, int studentId, int sectionId, String component, double score, String finalGrade) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.sectionId = sectionId;
        this.component = component;
        this.score = score;
        this.finalGrade = finalGrade;
    }

    // Getters
    public int getStudentId() { return studentId; }
    public int getSectionId() { return sectionId; }
    public String getComponent() { return component; }
    public double getScore() { return score; }
    public String getFinalGrade() { return finalGrade; }
}
