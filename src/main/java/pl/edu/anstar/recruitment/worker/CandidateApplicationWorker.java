package pl.edu.anstar.recruitment.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;
import pl.edu.anstar.recruitment.model.Faculty;
import pl.edu.anstar.recruitment.service.DatabaseService;
import pl.edu.anstar.recruitment.model.Mail;
import pl.edu.anstar.recruitment.model.CandidateApplication;
import pl.edu.anstar.recruitment.service.HttpClientService;
import pl.edu.anstar.recruitment.service.MailService;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class CandidateApplicationWorker {

    private static Logger LOGGER = LoggerFactory.getLogger(CandidateApplicationWorker.class);
/*
    @Value("${radon.nauka.gov.pl.apiKey:N/A}")
    private String API_KEY;
    @Value("${radon.nauka.gov.pl.url:N/A}")
    private String URL;
*/
    @Value("${database.postgresql.dburl:N/A}")
    private String dbUrl;

    @Value("${database.postgresql.dbuser:N/A}")
    private String dbUser;

    @Value("${database.postgresql.dbpassword:N/A}")
    private String dbPassword;

    @Value("${mail.anstar.edu.pl.mailsmtphost:N/A}")
    private String mailSmtpHost;

    @Value("${mail.anstar.edu.pl.mailuser:N/A}")
    private String mailUser;

    @Value("${mail.anstar.edu.pl.mailpassword:N/A}")
    private String mailPassword;

    @Value("${mail.anstar.edu.pl.mailsendermail:N/A}")
    private String mailSenderMail;

    @Value("${mail.anstar.edu.pl.mailsendername:N/A}")
    private String mailSenderName;

/*
    @Value("${spring.mail.host:N/A}")
    private String host;
    @Value("${spring.mail.port:N/A}")
    private String port;
    @Value("${spring.mail.username:N/A}")
    private String username;
    @Value("${spring.mail.password:N/A}")
    private String password;
*/
/*
    @JobWorker(type = "verifyInRadon1111")
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
*/
    @JobWorker(type = "registerApplication")
    public Map<String, Object> registerApplication(final JobClient client, final ActivatedJob job) {
        HashMap<String, Object> jobResultVariables = new HashMap<>();

        LOGGER.info("Job registerApplication is started.");

        final Map<String, Object> jobVariables = job.getVariablesAsMap();
        for (Map.Entry<String, Object> entry : jobVariables.entrySet()) {
            LOGGER.info("Job variable (process variable & inputted variable): " + entry.getKey() + " : " + entry.getValue());
        }

        int points = 0;
        try {
            points = Integer.parseInt((String) job.getVariablesAsMap().get("points"));
            LOGGER.info("Points. {}", points, "points.");
        } catch (NumberFormatException e) {
            LOGGER.info("Cannot convert String to int. {}", e);
        }

        CandidateApplication candidateApplication = new CandidateApplication(
                (String) job.getVariablesAsMap().get("firstName"),
                (String) job.getVariablesAsMap().get("lastName"),
                (String) job.getVariablesAsMap().get("email"),
                points,
                (String) job.getVariablesAsMap().get("faculty"),
                (boolean) job.getVariablesAsMap().get("olympic")
                //(String) job.getVariablesAsMap().get("decision")
        );

        int applicationId = DatabaseService.addApplication(candidateApplication, dbUrl, dbUser, dbPassword);
        if (applicationId > 0) {
            candidateApplication.setApplicationId(applicationId);
            LOGGER.info("Application registered. Application ID: {}", applicationId);
            jobResultVariables.put("applicationRegistered", true);
        } else {
            LOGGER.info("Application not registered.");
            jobResultVariables.put("applicationRegistered", false);
        }
        jobResultVariables.put("candidateApplication", candidateApplication);
        jobResultVariables.put("applicationId", applicationId);

        return jobResultVariables;
    }

    @JobWorker(type = "registerDecision")
    public Map<String, Object> registerDecision(final JobClient client, final ActivatedJob job) {
        HashMap<String, Object> jobResultVariables = new HashMap<>();

        LOGGER.info("Job registerDecision is started.");
        final Map<String, Object> jobVariables = job.getVariablesAsMap();
        for (Map.Entry<String, Object> entry : jobVariables.entrySet()) {
            LOGGER.info("Job variable (process variable & inputted variable): " + entry.getKey() + " : " + entry.getValue());
        }

        String decision = (String) job.getVariablesAsMap().get("decision");
        int applicationId = (Integer) job.getVariablesAsMap().get("applicationId");

        int countUpdatedRows = DatabaseService.updateApplicationDecision(applicationId, decision, dbUrl, dbUser, dbPassword);
        if (countUpdatedRows > 0) {
            LOGGER.info("Decision registered. Application ID: {}", applicationId + " / " + "Decision: " + decision);
            jobResultVariables.put("decisionRegistered", true);
        } else {
            LOGGER.info("Decision not registered.");
            jobResultVariables.put("decisionRegistered", false);
        }

        return jobResultVariables;
    }

    @JobWorker(type = "sendEmailWithANS")
    public Map<String, Object> sendEmailWithANS(final JobClient client, final ActivatedJob job) {
        HashMap<String, Object> jobResultVariables = new HashMap<>();

        LOGGER.info("Job sendEmail is started.");
        final Map<String, Object> jobVariables = job.getVariablesAsMap();
        for (Map.Entry<String, Object> entry : jobVariables.entrySet()) {
            LOGGER.info("Job variables (process & task input): {}", entry.getKey() + " : " + entry.getValue());
        }

        Mail mt = new Mail(0, (String) job.getVariablesAsMap().get("firstName") + " " + (String) job.getVariablesAsMap().get("lastName"), (String) job.getVariablesAsMap().get("email"), null, (Integer) job.getVariablesAsMap().get("applicationId"));
        try {
            mt.sendMail(mailSmtpHost, mailUser, mailPassword, mailSenderMail, mailSenderName);
            LOGGER.info("Sending mail succeeded.");
            jobResultVariables.put("mailSendingResult", true);
        } catch (Exception e) {
            LOGGER.error("Sending mail failed.");
            jobResultVariables.put("mailSendingResult", false);
        }

        return jobResultVariables;
    }
/*
    @JobWorker(type = "sendEmail")
    public Map<String, Object> sendGmail(final JobClient client, final ActivatedJob job) {
        HashMap<String, Object> jobResultVariables = new HashMap<>();

        LOGGER.info("Job sendEmail is started.");
        final Map<String, Object> jobVariables = job.getVariablesAsMap();
        for (Map.Entry<String, Object> entry : jobVariables.entrySet()) {
            LOGGER.info("Job variables (process & task input): {}", entry.getKey() + " : " + entry.getValue());
        }

        try {
            String from = "tpotempa@gmail.com";
            String fromName = "Akademia Tarnowska";
            String toAddress = (String) job.getVariablesAsMap().get("email");
            String toAddressName = job.getVariablesAsMap().get("firstName") + " " + (String) job.getVariablesAsMap().get("lastName");
            String ccAddresses = "";
            String bccAddresses = "";
            String subject = "Informacja o rejestracji aplikacji";
            String body = "Aplikacja na kierunek " + (String) job.getVariablesAsMap().get("radonFacultyName") +
                    ", studia " + (String) job.getVariablesAsMap().get("radonFacultyLevel") +
                    ", studia " + (String) job.getVariablesAsMap().get("radonFacultyForm") +
                    ", profil " + (String) job.getVariablesAsMap().get("radonFacultyProfile") +
                    " o czasie trwania " + (String) job.getVariablesAsMap().get("radonFacultyNumberOfSemesters") +
                    " semestrów" +
                    " z tytułem zawodowym " + (String) job.getVariablesAsMap().get("radonFacultyTitle") +
                    " została zarejestrowana. " +
                    "Identyfikator aplikacji: " + (Integer) job.getVariablesAsMap().get("applicationId");

            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(host);
            mailSender.setPort(Integer.parseInt(port));

            mailSender.setUsername(username);
            mailSender.setPassword(password);

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.debug", "true");

            MailService gs = new MailService(mailSender);
            gs.sendMail(from, fromName, subject, toAddress, ccAddresses, bccAddresses, body);
        } catch (Exception e) {
            LOGGER.error("Task exception.", e);
        }
        return jobResultVariables;
    }

    @JobWorker(type = "sendEnrollmentEmail")
    public Map<String, Object> sendGmailEnrollment(final JobClient client, final ActivatedJob job) {
        HashMap<String, Object> jobResultVariables = new HashMap<>();

        LOGGER.info("Job sendEmail is started.");
        final Map<String, Object> jobVariables = job.getVariablesAsMap();
        for (Map.Entry<String, Object> entry : jobVariables.entrySet()) {
            LOGGER.info("Job variables (process & task input): {}", entry.getKey() + " : " + entry.getValue());
        }

        try {
            String from = "tpotempa@gmail.com";
            String fromName = "Akademia Tarnowska";
            String toAddress = (String) job.getVariablesAsMap().get("email");
            String toAddressName = job.getVariablesAsMap().get("firstName") + " " + (String) job.getVariablesAsMap().get("lastName");
            String ccAddresses = "";
            String bccAddresses = "";
            String subject = "Informacja o wpisie na I rok studiów";
            String body = "Wpis I rok studiów na kierunek " + (String) job.getVariablesAsMap().get("radonFacultyName") +
                    ", studia " + (String) job.getVariablesAsMap().get("radonFacultyLevel") +
                    ", studia " + (String) job.getVariablesAsMap().get("radonFacultyForm") +
                    ", profil " + (String) job.getVariablesAsMap().get("radonFacultyProfile") +
                    " o czasie trwania " + (String) job.getVariablesAsMap().get("radonFacultyNumberOfSemesters") +
                    " semestrów" +
                    " z tytułem zawodowym " + (String) job.getVariablesAsMap().get("radonFacultyTitle") +
                    " została zarejestrowany. " +
                    "Identyfikator wpisu na studia: " + (Integer) job.getVariablesAsMap().get("applicationId");

            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(host);
            mailSender.setPort(Integer.parseInt(port));

            mailSender.setUsername(username);
            mailSender.setPassword(password);

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.debug", "true");

            MailService gs = new MailService(mailSender);
            gs.sendMail(from, fromName, subject, toAddress, ccAddresses, bccAddresses, body);
        } catch (Exception e) {
            LOGGER.error("Task exception.", e);
        }
        return jobResultVariables;
    }
*/
}