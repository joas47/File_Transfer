package com.example.filetransfer;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

/**
 * Represents a tab for sending a file.
 * Extends the Tab class.
 */
public class SendTab extends Tab {

    private final LogTab logTab;
    private final DatabaseHandler db = FileTransfer.getDatabaseHandler();
    private TextField serverTextField = new TextField("localhost");
    private TextField portTextField = new TextField("8080");
    private ProgressBar sendProgressBar;

    public SendTab(Stage stage, LogTab logTab) {
        this.logTab = logTab;
        setText("Send File");
        initComponents(stage);
    }

    /**
     * Creates and returns a VBox layout with specified components.
     *
     * @param sendFileButton   the button used to send a file
     * @param sendPathField    the text field used to display the path of the file to send
     * @param chooseFileToSend the button used to choose a file to send
     * @return the VBox layout with the specified components
     */
    private VBox getvBox(Button sendFileButton, TextField sendPathField, Button chooseFileToSend) {
        TextField serverText = new TextField("Server: ");
        TextField portText = new TextField("Port: ");
        serverText.setEditable(false);
        portText.setEditable(false);
        HBox hBox = new HBox(serverText, serverTextField);
        HBox hBox1 = new HBox(portText, portTextField);
        hBox.setAlignment(Pos.CENTER);
        hBox1.setAlignment(Pos.CENTER);
        VBox vBox = new VBox(hBox, hBox1, sendFileButton, sendPathField, chooseFileToSend, sendProgressBar);
        vBox.setAlignment(Pos.CENTER);
        return vBox;
    }

    /**
     * Initializes the components of the application window.
     *
     * @param stage the main application stage
     */
    private void initComponents(Stage stage) {
        sendProgressBar = createProgressBar();
        TextField sendPathField = createSendPathField();
        Button chooseFileToSend = createChooseFileButton(stage, sendPathField);
        Button sendFileButton = createSendFileButton(sendPathField);
        VBox vBox = getvBox(chooseFileToSend, sendPathField, sendFileButton);
        setContent(vBox);
    }

    /**
     * Creates and initializes a new ProgressBar.
     *
     * @return the newly created ProgressBar
     */
    private ProgressBar createProgressBar() {
        ProgressBar progressBar = new ProgressBar();
        progressBar.setVisible(false);
        return progressBar;
    }

    /**
     * Displays an error message in a dialog box.
     *
     * @param message the error message to be displayed
     */
    private void showErrorMessage(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Displays an information message in a dialog box.
     *
     * @param message the information message to be displayed
     */
    private void showInformation(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Success");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * This method is used to send a file to a specified server on a specific port.
     * It retrieves filename, server and port values from the GUI, validates that they are not empty,
     * and then initializes a file sender with these parameters.
     * A progress bar is shown in the GUI while the file is being sent.
     * If sending the file is successful, it logs the transfer and inserts it into a database.
     * If the file transfer fails, it shows an error message.
     *
     * @param sendPathField TextField GUI component that contains the full path to the file to send.
     */
    private void sendFile(TextField sendPathField) {
        if (sendPathField.getText().isEmpty() || serverTextField.getText().isEmpty() || portTextField.getText().isEmpty()) {
            showErrorMessage("Please fill in all fields and select a file.");
            return;
        }

        String filename = sendPathField.getText();
        String server = serverTextField.getText();
        int port = Integer.parseInt(portTextField.getText());

        FileSender fileSender = new FileSender(filename, server, port);

        sendProgressBar.setVisible(true);

        new Thread(() -> {
            try {
                fileSender.send();
                // TODO: never reaches this point it seems. Investigate.
                waitForFileToSend(fileSender);

                Platform.runLater(() -> {
                    sendProgressBar.setVisible(false);
                    logTab.logSent(filename, server, port);
                    db.insertSentFileTransfer(filename, fileSender.getTotalBytes(), server, String.valueOf(port));
                    showInformation("File sent successfully.");
                });
            } catch (IOException ex) {
                ex.printStackTrace();
                showErrorMessage("An error occurred while sending the file.");
            }
        }).start();
    }


    /**
     * This method waits for the file to be sent by continuously checking the status of the FileSender.
     * It updates the progress bar in the GUI based on the progress of the file transfer.
     * Sleeps for 100  milliseconds to avoid using too much CPU while waiting for the file to be sent.
     *
     * @param fileSender The FileSender object responsible for sending the file.
     */
    private void waitForFileToSend(FileSender fileSender) {
        while (fileSender.isSending()) {
            double progress = fileSender.getProgress();
            Platform.runLater(() -> sendProgressBar.setProgress(progress));

            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Creates a button for sending a file.
     * When the button is clicked, it executes the sendFile() method with the provided sendPathField parameter.
     *
     * @param sendPathField The text field containing the path of the file to be sent.
     * @return A Button object representing the "Send File" button.
     */
    private Button createSendFileButton(TextField sendPathField) {
        Button sendFileButton = new Button("Send File");
        sendFileButton.setOnAction(e -> sendFile(sendPathField));
        return sendFileButton;
    }

    /**
     * Creates a text field for entering the path of a file to be sent.
     * The field is initially set to non-editable to prevent the user from entering a path manually.
     *
     * @return A TextField object representing the sendPathField.
     */
    private TextField createSendPathField() {
        TextField sendPathField = new TextField();
        sendPathField.setEditable(false);
        return sendPathField;
    }

    /**
     * Opens a file chooser dialog to allow the user to select a file.
     * If a file is selected, the absolute path of the file is set as the text in the provided sendPathField.
     *
     * @param stage         The primary stage of the application.
     * @param sendPathField The text field representing the path of the file to be sent.
     */
    private void chooseFile(Stage stage, TextField sendPathField) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            sendPathField.setText(file.getAbsolutePath());
        }
    }

    /**
     * Creates a button with the label "Choose File to Send" that opens a file chooser dialog when clicked.
     * The selected file path will be set as the text in the provided sendPathField.
     *
     * @param stage         The primary stage of the application.
     * @param sendPathField The text field representing the path of the file to be sent.
     * @return The created button with the click event handler to open the file chooser dialog.
     */
    private Button createChooseFileButton(Stage stage, TextField sendPathField) {
        Button chooseFileToSend = new Button("Choose File to Send");
        chooseFileToSend.setOnAction(e -> chooseFile(stage, sendPathField));
        return chooseFileToSend;
    }
}