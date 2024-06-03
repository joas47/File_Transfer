package com.example.filetransfer;

import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

/**
 * A class representing a tab for displaying log information.
 * The LogTab extends the Tab class and provides methods for logging sent and received file transfers.
 */
public class LogTab extends Tab {

    private final TextArea logTextArea = new TextArea();

    public LogTab(Stage stage) {
        setText("Log");
        logTextArea.setEditable(false);
        setContent(logTextArea);

        // Load old transfers from the database when the program starts.
        try {
            List<FileTransferRecord> transfers = DatabaseHandler.getInstance().getAllFileTransfers();
            for (FileTransferRecord record : transfers) {
                if (record.getTransferDirection().equals("send")) {
                    logOldSent(record.getFilename(), record.getServer(), record.getPort(), record.getTimestamp());
                } else if (record.getTransferDirection().equals("receive")) {
                    logOldReceived(record.getFilename(), record.getServer(), record.getPort(), record.getTimestamp());
                }
            }
        } catch (SQLException e) {
            logTextArea.appendText("Error loading transfer history from database: " + e.getMessage() + "\n");
        }
    }

    // Below are methods for logging sent and received file transfers.
    // The logOldSent() and logOldReceived() methods are used to load old transfers from the database when the program starts.
    // The logSent() and logReceived() methods are used to log new transfers when they occur.

    private void logOldSent(String filename, String server, String port, Instant timestamp) {
        String ts = java.time.LocalDateTime.ofInstant(timestamp,
                java.time.ZoneId.systemDefault()).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        logTextArea.appendText("[" + ts + "]" + " Sent " + filename + " to " + server + ":" + port + "\n");
    }

    private void logOldReceived(String filename, String server, String port, Instant timestamp) {
        String ts = java.time.LocalDateTime.ofInstant(timestamp,
                java.time.ZoneId.systemDefault()).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        logTextArea.appendText("[" + ts + "]" + " Received " + filename + " from " + server + ":" + port + "\n");
    }

    private String timeDate() {
        return java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public void logSent(String filename, String server, int port) {
        logTextArea.appendText("[" + timeDate() + "]" + " Sent " + filename + " to " + server + ":" + port + "\n");
    }

    public void logReceived(String filename, String server, int port) {
        logTextArea.appendText("[" + timeDate() + "]" + " Received " + filename + " from " + server + ":" + port + "\n");
    }

}
