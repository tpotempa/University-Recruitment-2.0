package pl.edu.anstar.recruitment.service;

import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import pl.edu.anstar.recruitment.model.CandidateApplication;

@Configuration
@ConfigurationProperties
public class DatabaseService {

    static final String JDBC_DRIVER = "org.postgresql.Driver";

    private static Logger LOGGER = LoggerFactory.getLogger(DatabaseService.class);

    public DatabaseService() {
    }

    private static void close(Connection connection, Statement stmt, ResultSet rs) {
        try {
            if (rs != null && !rs.isClosed()) {
                rs.close();
                LOGGER.info("Object {} closed.", rs.getClass().getName());
            }
        } catch (Exception e) {
            LOGGER.error("An exception occurred while closing a result set.", e);
        }
        try {
            if (stmt != null && !stmt.isClosed()) {
                stmt.close();
                LOGGER.info("Object {} closed.", stmt.getClass().getName());
            }
        } catch (NullPointerException e) {
            LOGGER.error("Null pointer exception occurred while closing Statement object.", e);
        } catch (Exception e) {
            LOGGER.error("An exception occurred while closing a {}.", stmt.getClass().getName() + e);
        }
        try {
            if (connection != null && !connection.isClosed()) {
                if (!connection.getAutoCommit()) {
                    try {
                        connection.setAutoCommit(true);
                        LOGGER.info("Connection AutoCommit mode set to {}.", connection.getAutoCommit());
                    } catch (SQLException e) {
                        LOGGER.error("An exception occurred while setting connection AutoCommit mode.", e);
                    }
                }
                connection.close();
                LOGGER.info("Object {} closed.", connection.getClass().getName());
            }
        } catch (Exception e) {
            LOGGER.error("An exception occurred while closing a connection.", e);
        }
    }

    public static int addApplication(CandidateApplication application, String url, String user, String password) {
        int applicationID = 0;
        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            Class.forName(JDBC_DRIVER);

            connection = DriverManager.getConnection(url, user, password);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            String sql = "INSERT INTO recruitment.application VALUES (DEFAULT,?,?,?,?,?,?,?,DEFAULT) RETURNING application_id";

            pstmt = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pstmt.clearParameters();

            pstmt.setString(1, application.getFirstName());
            pstmt.setString(2, application.getLastName());
            pstmt.setString(3, application.getEmail());
            pstmt.setInt(4, application.getPoints());
            pstmt.setString(5, application.getFaculty());
            pstmt.setBoolean(6, application.getOlympic());
            if(application.getDecision() == null) {
                pstmt.setNull(7, Types.VARCHAR);
            } else {
                pstmt.setString(7, application.getDecision());
            }

            rs = pstmt.executeQuery();
            rs.beforeFirst();
            if (rs.next()) {
                applicationID = rs.getInt("application_id");
                LOGGER.info("Row inserted. Application ID: {}.", applicationID);
            } else {
                LOGGER.info("No rows were inserted.");
            }

        } catch (ClassNotFoundException e) {
            LOGGER.error("An exception occurred while loading JDBC class.", e);
        } catch (Exception e) {
            LOGGER.error("A generic exception occurred.", e);
        } finally {
            close(connection, pstmt, rs);
        }
        return applicationID;
    }

    public static int updateApplicationDecision(int applicationId, String decision, String url, String user, String password) {
        Connection connection = null;
        PreparedStatement pstmt = null;
        int countUpdatedRows = 0;

        try {
            Class.forName(JDBC_DRIVER);

            connection = DriverManager.getConnection(url, user, password);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            String sql = "UPDATE recruitment.application SET decision = ? WHERE application_id = ?";

            pstmt = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pstmt.clearParameters();

            pstmt.setString(1, decision);
            pstmt.setInt(2, applicationId);

            countUpdatedRows = pstmt.executeUpdate();
            if (countUpdatedRows > 0) {
                LOGGER.info("Row updated: {}.", countUpdatedRows);
            } else {
                LOGGER.info("No rows were updated.");
            }

        } catch (ClassNotFoundException e) {
            LOGGER.error("An exception occurred while loading JDBC class.", e);
        } catch (Exception e) {
            LOGGER.error("A generic exception occurred.", e);
        } finally {
            close(connection, pstmt, null);
        }

        return countUpdatedRows;
    }

}