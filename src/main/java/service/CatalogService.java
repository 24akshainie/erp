package service;

import data.CourseDAO;
import data.SectionDAO;
import data.InstructorDAO;
import domain.Course;
import domain.Section;
import domain.Instructor;

import java.util.ArrayList;
import java.util.List;

public class CatalogService {

    private final CourseDAO courseDAO;
    private final SectionDAO sectionDAO;
    private final InstructorDAO instructorDAO;

    public CatalogService() {
        this.courseDAO = new CourseDAO();
        this.sectionDAO = new SectionDAO();
        this.instructorDAO = new InstructorDAO();
    }

    public CatalogService(CourseDAO cDAO, SectionDAO sDAO, InstructorDAO iDAO) {
        this.courseDAO = cDAO;
        this.sectionDAO = sDAO;
        this.instructorDAO = iDAO;
    }

    // Get list of all courses from the db
    public List<Course> getAllCourses() {
        return courseDAO.getAllCourses();
    }

    // Get list of all sections for a specific course
    public List<Section> getSectionsForCourse(String courseCode) {
        return sectionDAO.getSectionsByCourse(courseCode);
    }

    // Search courses by keyword
    public List<Course> searchCourses(String query) {
        if (query == null || query.isBlank()) return getAllCourses();
        return courseDAO.searchCourses(query.trim());
    }

    public List<CourseCatalogRow> getFullCatalogForUI() {
        List<Course> courses = courseDAO.getAllCourses();
        List<CourseCatalogRow> rows = new ArrayList<>();

        for (Course c : courses) {
            List<Section> secs = sectionDAO.getSectionsByCourse(c.getCode());

            for (Section s : secs) {

                // Instructor information
                String instructorDisplay = "";

                Instructor inst = instructorDAO.getByUserId(s.getInstructorId());
                if (inst != null) {
                    instructorDisplay = inst.getDepartment(); 
                }

                rows.add(new CourseCatalogRow(
                        c.getCode(),
                        c.getTitle(),
                        c.getCredits(),
                        s.getId(),          // CORRECT
                        s.getDayTime(),
                        s.getRoom(),
                        s.getCapacity(),
                        instructorDisplay
                ));
            }
        }
        return rows;
    }

    public static class CourseCatalogRow {
        public String courseCode;
        public String title;
        public int credits;
        public int sectionId;
        public String dayTime;
        public String room;
        public int capacity;
        public String instructorName;

        public CourseCatalogRow(String courseCode, String title, int credits,
                                int sectionId, String dayTime, String room,
                                int capacity, String instructorName) {

            this.courseCode = courseCode;
            this.title = title;
            this.credits = credits;
            this.sectionId = sectionId;
            this.dayTime = dayTime;
            this.room = room;
            this.capacity = capacity;
            this.instructorName = instructorName;
        }
    }
}
