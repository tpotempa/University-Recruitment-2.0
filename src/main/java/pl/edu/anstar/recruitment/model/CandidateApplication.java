package pl.edu.anstar.recruitment.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CandidateApplication {

    private static Logger LOG = LoggerFactory.getLogger(CandidateApplication.class);
    private int applicationId;
    private String firstName;
    private String lastName;
    private String email;
    private int points;
    private String faculty;
    private boolean olympic;
    private String decision;

    public CandidateApplication() {
    }

    public CandidateApplication(String firstName, String lastName, String email, int points, String faculty, boolean olympic) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.points = points;
        this.faculty = faculty;
        this.olympic = olympic;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public boolean getOlympic() {
        return olympic;
    }

    public void setOlympic(boolean olympic) {
        this.olympic = olympic;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }
}