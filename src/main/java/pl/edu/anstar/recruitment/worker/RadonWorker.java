package pl.edu.anstar.recruitment.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.edu.anstar.recruitment.model.Faculty;
import pl.edu.anstar.recruitment.service.HttpClientService;

@Component
public class RadonWorker {

    private static Logger LOGGER = LoggerFactory.getLogger(RadonWorker.class);
    @Value("${radon.nauka.gov.pl.apiKey:N/A}")
    private String API_KEY;
    @Value("${radon.nauka.gov.pl.url:N/A}")
    private String URL;

    @JobWorker(type = "verifyInRadon")
    public void verifyInRadon(final JobClient client, final ActivatedJob job) {

        LOGGER.info("Job verifyInRadon is started.");

        String url =  URL + "&courseName=" + (String) job.getVariablesAsMap().get("faculty");
        LOGGER.info("URL: {}", url);

        HttpClientService radonClient = new HttpClientService(url, API_KEY);
        Faculty faculty = radonClient.facultyRequest();

        LOGGER.info("RADON : Faculty name: {}", faculty.getFacultyName());
        LOGGER.info("RADON : Faculty profile: {}", faculty.getFacultyProfile());
        LOGGER.info("RADON : Faculty level: {}", faculty.getFacultyLevel());
        LOGGER.info("RADON : Faculty title: {}", faculty.getFacultyTitle());
        LOGGER.info("RADON : Faculty form: {}", faculty.getFacultyForm());
        LOGGER.info("RADON : Faculty number of semesters: {}", faculty.getFacultyNumberOfSemesters());

        if(faculty.getFacultyName().equalsIgnoreCase("N/D")) {
            client.newThrowErrorCommand(job.getKey())
                    .errorCode("FACULTY_UNAVAILABLE")
                    .send()
                    .join();
            LOGGER.info("Error FACULTY_UNAVAILABLE thrown.");
        } else {
            // Another way of ending job.
            client.newCompleteCommand(job.getKey())
                    .variables("{\"radonFacultyName\": " + "\"" + faculty.getFacultyName() + "\"" + ", \"radonFacultyProfile\":" + "\"" + faculty.getFacultyProfile() + "\"" + ", \"radonFacultyLevel\":" + "\"" + faculty.getFacultyLevel() + "\"" + ", \"radonFacultyForm\":" + "\"" + faculty.getFacultyForm() + "\"" + ", \"radonFacultyTitle\":" + "\"" + faculty.getFacultyTitle() + "\"" + ", \"radonFacultyNumberOfSemesters\":" + "\"" + faculty.getFacultyNumberOfSemesters() + "\"" + "}")
                    .send()
                    .exceptionally(throwable -> {
                        throw new RuntimeException("Could not complete job " + job, throwable);
                    });
        }
        LOGGER.info("Job verifyInRadon is ended.");
    }
}