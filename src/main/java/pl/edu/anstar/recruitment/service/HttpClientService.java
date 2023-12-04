package pl.edu.anstar.recruitment.service;

import pl.edu.anstar.recruitment.model.Faculty;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientService {

    private static final Logger LOG = LoggerFactory.getLogger(HttpClientService.class);
    private String API_KEY;
    private String URL;

    public HttpClientService(String url, String api_key){
        this.API_KEY = api_key;
        this.URL = url;
    }

    public Faculty facultyRequest() {
        String response="";
        try {
            URL url = new URL(URL + API_KEY);
            response = httpGET(url);
        } catch (Exception e){
            LOG.error("Exception: {}", e);
        }
        Faculty faculty = parseResponseToFaculty(response);

        return faculty;
    }

    public String httpGET(URL url){
        StringBuilder response = new StringBuilder();
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
            }
            bufferedReader.close();
        }catch(Exception e){
            LOG.error("Exception: {}", e);
        }

        return response.toString();
    }

    public Faculty parseResponseToFaculty(String response) {
        Faculty faculty = new Faculty("N/D", "N/D", "N/D", "N/D", "N/D", "N/D");
        JSONObject jsonObject = null;
        JSONObject facultyObject = null;
        JSONObject facultyDetailsObject = null;

        try {
            jsonObject = new JSONObject(response);

            if(jsonObject.getJSONArray("results").length() != 0) {
                facultyObject = jsonObject.getJSONArray("results").getJSONObject(0);
                faculty.setFacultyName(facultyObject.getString("courseName"));
                faculty.setFacultyProfile(facultyObject.getString("profileName"));
                faculty.setFacultyLevel(facultyObject.getString("levelName"));

                if(facultyObject.getJSONArray("courseInstances").length() != 0) {
                    facultyDetailsObject = facultyObject.getJSONArray("courseInstances").getJSONObject(0);
                    faculty.setFacultyTitle(facultyDetailsObject.getString("titleName"));
                    faculty.setFacultyForm(facultyDetailsObject.getString("formName"));
                    faculty.setFacultyNumberOfSemesters(facultyDetailsObject.getString("numberOfSemesters"));
                }

            } else {
                LOG.info("Response is empty.");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return faculty;
    }
}