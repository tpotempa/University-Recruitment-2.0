package pl.edu.anstar.recruitment.model;

public class Faculty {
    private String facultyName;
    private String facultyProfile;
    private String facultyLevel;
    private String facultyTitle;
    private String facultyForm;
    private String facultyNumberOfSemesters;

    public Faculty(String facultyName, String facultyProfile, String facultyLevel, String facultyTitle, String facultyForm, String facultyNumberOfSemesters) {
        this.facultyName = facultyName;
        this.facultyProfile = facultyProfile;
        this.facultyLevel = facultyLevel;
        this.facultyTitle = facultyTitle;
        this.facultyForm = facultyForm;
        this.facultyNumberOfSemesters = facultyNumberOfSemesters;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getFacultyProfile() {
        return facultyProfile;
    }

    public void setFacultyProfile(String facultyProfile) {
        this.facultyProfile = facultyProfile;
    }

    public String getFacultyLevel() { return facultyLevel; }

    public void setFacultyLevel(String facultyLevel) {
        this.facultyLevel = facultyLevel;
    }

    public String getFacultyTitle() {
        return facultyTitle;
    }

    public void setFacultyTitle(String facultyTitle) {
        this.facultyTitle = facultyTitle;
    }

    public String getFacultyForm() {
        return facultyForm;
    }

    public void setFacultyForm(String facultyForm) {
        this.facultyForm = facultyForm;
    }

    public void setFacultyNumberOfSemesters(String facultyNumberOfSemesters) { this.facultyNumberOfSemesters = facultyNumberOfSemesters; }

    public String getFacultyNumberOfSemesters() {
        return facultyNumberOfSemesters;
    }
}