package service;

import access.AccessControl;
import data.GradeDAO;
import domain.Grade;
import java.util.*;

public class InstructorService {

    // Allows instructor to enter a score for a specific student and component.
    public static void enterScore(int instructorId, int studentId, int sectionId, String component, double score) throws Exception {
        if (!AccessControl.isAllowed("Instructor", "enter_scores")) return;
        Grade g = new Grade(studentId, sectionId, component, score);
        new GradeDAO().addGrade(g);
    }

    // View gradebook for a particular section
    public static void viewGradebook(int sectionId) {
        if (!AccessControl.isAllowed("Instructor", "view_gradebook")) return;
        List<Grade> list = new GradeDAO().getGradesBySection(sectionId);
    }

    // Calculate and assign final grades for all students in a section
    public static void computeFinalGrades(int sectionId) {
        if (!AccessControl.isAllowed("Instructor", "compute_final")) return;
        GradeDAO dao = new GradeDAO();
        List<Grade> grades = dao.getGradesBySection(sectionId);
        for (Grade g : grades) {
            String grade = (g.getScore() >= 90) ? "A" :
                           (g.getScore() >= 80) ? "B" :
                           (g.getScore() >= 70) ? "C" :
                           (g.getScore() >= 60) ? "D" : "F";
            dao.updateFinalGrade(g.getStudentId(), sectionId, grade);
        }
    }
}
