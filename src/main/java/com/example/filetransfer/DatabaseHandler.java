package com.example.filetransfer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The DatabaseHandler class is responsible for handling interactions with the H2 database.
 * It provides methods for creating the necessary table, inserting file transfer entries into the database,
 * retrieving all file transfer entries, and closing the database connection.
 */
public class DatabaseHandler {

    // Database connection details.
    private static final String JDBC_URL = "jdbc:h2:file:./database";
    private static final String USER = "sa";
    private static final String PASSWORD = "";
    private static DatabaseHandler instance;

    // Load the H2 JDBC driver. This is required to connect to the H2 database.
    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Connection connection;

    /**
     * Private constructor used to prevent direct instantiation of the DatabaseHandler class.
     * Instead, the getInstance() method should be used to obtain an instance of DatabaseHandler.
     */
    private DatabaseHandler() {
        try {
            connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
            initializeDatabase(connection);
        } catch (SQLException e) {
            System.out.println("Connection to H2 database failed. " + e.getMessage());
        }
    }

    /**
     * Returns the instance of DatabaseHandler using the Singleton pattern, ensuring that only one instance is created.
     *
     * @return the instance of DatabaseHandler
     */
    public static DatabaseHandler getInstance() {
        if (instance == null) {
            instance = new DatabaseHandler();
        }
        return instance;
    }

    /**
     * Create the table if it does not exist. It doesn't exist the first time the application is run.
     * Simple but works for this application. transfer_direction is a string, but could be an enum for example.
     */
    private void initializeDatabase(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS file_transfers (" +
                "id INT AUTO_INCREMENT, " +
                "filename VARCHAR(255), " +
                "filesize LONG, " +
                "transfer_direction VARCHAR(50), " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP(), " +
                "server VARCHAR(255), " +
                "port VARCHAR(255), " +
                "PRIMARY KEY (id));";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
        }
    }

    /**
     * Close the connection to the H2 database.
     * This should be called when the application is closed.
     * To ensure the database is not corrupted, the connection should be closed before the application is closed.
     */
    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.out.println("Failed to close connection to H2 database. " + e.getMessage());
        }
    }

    /**
     * Get all file transfer entries from the database.
     */
    public List<FileTransferRecord> getAllFileTransfers() throws SQLException {
        List<FileTransferRecord> transfers = new ArrayList<>();
        String sql = "SELECT * FROM file_transfers";

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                transfers.add(new FileTransferRecord(
                        rs.getString("filename"),
                        rs.getLong("filesize"),
                        rs.getString("transfer_direction"),
                        rs.getTimestamp("timestamp").toInstant(),
                        rs.getString("server"),
                        rs.getString("port")
                ));
            }
        }
        return transfers;
    }

    /**
     * Inserts a file transfer entry into the database.
     */
    private void insertFileTransfer(String filename, long filesize, String transferDirection, String server, String port) {
        String sql = "INSERT INTO file_transfers (filename, filesize, transfer_direction, server, port) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, filename);
            pstmt.setLong(2, filesize);
            pstmt.setString(3, transferDirection);
            pstmt.setString(4, server);
            pstmt.setString(5, port);

            pstmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Inserts a sent file transfer entry into the database.
     */
    public void insertSentFileTransfer(String filename, long fileSize, String server, String port) {
        insertFileTransfer(filename, fileSize, "send", server, port);
    }

    /**
     * Insert a received file transfer entry into the database.
     */
    public void insertReceivedFileTransfer(String filename, long fileSize, String server, String port) {
        insertFileTransfer(filename, fileSize, "receive", server, port);
    }


}
