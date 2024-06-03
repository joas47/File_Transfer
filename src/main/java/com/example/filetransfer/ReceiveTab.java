package com.example.filetransfer;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * Represents a tab for receiving files.
 * This class extends the Tab class and provides functionality for creating and displaying a UI for receiving files.
 */
public class ReceiveTab extends Tab {

    private final LogTab logTab;
    private final DatabaseHandler db = FileTransfer.getDatabaseHandler();
    private TextField portTextField = new TextField("8080");
    private ProgressBar receiveProgressBar;

    public ReceiveTab(Stage stage, LogTab logTab) {
        this.logTab = logTab;
        setText("Receive File");
        TextField receivePathField = createReceivePathField();
        receiveProgressBar = createProgressBar();
        setupReceiveTabUI(stage, receivePathField);
    }

    // Sets up the UI for the receive tab.
    private void setupReceiveTabUI(Stage stage, TextField receivePathField) {
        Button chooseSaveLocationButton = createChooseSaveLocationButton(stage, receivePathField);
        Button receiveFileButton = createReceiveFileButton(receivePathField);

        VBox vBox = createLayout(chooseSaveLocationButton, receivePathField, receiveFileButton);
        setContent(vBox);
    }

    /**
     * Creates a layout using the given components.
     *
     * @param chooseSaveLocationButton The button to choose the save location.
     * @param receivePathField         The text field to receive the path.
     * @param receiveFileButton        The button to receive the file.
     * @return The VBox layout containing the components.
     */
    private VBox createLayout(Button chooseSaveLocationButton, TextField receivePathField, Button receiveFileButton) {
        TextField portText = new TextField("Port: ");
        portText.setEditable(false);

        HBox portBox = new HBox(portText, portTextField);
        portBox.setAlignment(Pos.CENTER);

        VBox vBox = new VBox(portBox, chooseSaveLocationButton, receivePathField, receiveFileButton, receiveProgressBar);
        vBox.setAlignment(Pos.CENTER);

        return vBox;
    }

    /**
     * Creates a receive file button.
     * When clicked, the button will call the receiveFile() method.
     *
     * @param receivePathField The text field to receive the path.
     * @return The receive file button.
     */
    private Button createReceiveFileButton(TextField receivePathField) {
        Button receiveFileButton = new Button("Receive File");
        receiveFileButton.setOnAction(e -> receiveFile(receivePathField));
        return receiveFileButton;
    }

    /**
     * Opens a directory chooser dialog for the user to choose a save location.
     * If a directory is chosen, the absolute path of the chosen directory will be set as the text of the given receivePathField.
     *
     * @param stage            The stage in which the directory chooser dialog will be displayed.
     * @param receivePathField The text field to set the chosen save location.
     */
    private void chooseSaveLocation(Stage stage, TextField receivePathField) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File dir = directoryChooser.showDialog(stage);

        if (dir != null) {
            receivePathField.setText(dir.getAbsolutePath());
        }
    }

    /**
     * Creates a button that, when clicked, opens a directory chooser dialog for the user to choose a save location.
     * If a directory is chosen, the absolute path of the chosen directory will be set as the text of the given receivePathField.
     *
     * @param stage            The stage in which the directory chooser dialog will be displayed.
     * @param receivePathField The text field to set the chosen save location.
     * @return A Button object that can be added to the user interface.
     */
    private Button createChooseSaveLocationButton(Stage stage, TextField receivePathField) {
        Button chooseSaveLocationButton = new Button("Choose Save Location");
        chooseSaveLocationButton.setOnAction(e -> chooseSaveLocation(stage, receivePathField));
        return chooseSaveLocationButton;
    }

    /**
     * Creates a progress bar that is initially not visible.
     *
     * @return A ProgressBar object that can be added to the user interface.
     */
    private ProgressBar createProgressBar() {
        ProgressBar progressBar = new ProgressBar();
        progressBar.setVisible(false);
        return progressBar;
    }

    /**
     * Creates a TextField for displaying the receive path.
     * The TextField is initially not editable to prevent the user from changing the path.
     *
     * @return A TextField object that can be added to the user interface.
     */
    private TextField createReceivePathField() {
        TextField receivePathField = new TextField();
        receivePathField.setEditable(false);
        return receivePathField;
    }

    /**
     * Displays an error message in an alert dialog.
     * The alert dialog is shown on the JavaFX application thread
     * using the Platform.runLater() method to avoid concurrency issues.
     *
     * @param message The error message to be displayed.
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
     * Displays an information message in an alert dialog.
     * The alert dialog is shown on the JavaFX application thread
     * using the Platform.runLater() method to avoid concurrency issues.
     *
     * @param message The information message to be displayed.
     */
    private void showInformationMessage(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Success");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Method to receive a file over the network.
     * This method does some initialization work, including checking if all necessary fields are filled,
     * and starting a new FileReceiver object.
     * <p>
     * It then starts a new Thread which continually checks if the FileReceiver is still receiving data,
     * updating a progress bar on the GUI with the current transfer progress.
     * If an InterruptedException occurs, it will print a stack trace.
     * After the file has been received, this method will hide the progress bar,
     * log the received file, insert the file transfer into the database,
     * and display a message saying the file was received successfully.
     *
     * @param receivePathField The TextField containing the path where the file should be received to.
     */
    private void receiveFile(TextField receivePathField) {
        if (receivePathField.getText().isEmpty() || portTextField.getText().isEmpty()) {
            showErrorMessage("Please fill in all fields and select a save location.");
            return;
        }

        String receiveFilePath = receivePathField.getText();
        int port = Integer.parseInt(portTextField.getText());

        FileReceiver fileReceiver = new FileReceiver(receiveFilePath, port);

        receiveProgressBar.setVisible(true);

        new Thread(() -> {
            fileReceiver.run();

            // TODO: never seems to get here. Investigate.
            while (fileReceiver.isReceiving()) {
                double progress = fileReceiver.getProgress();
                Platform.runLater(() -> receiveProgressBar.setProgress(progress));

                try {
                    // Sleep for 100 milliseconds to avoid using too much CPU while waiting for the file to be received.
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

            receiveProgressBar.setVisible(false);
            logTab.logReceived(fileReceiver.getFilename(), "localhost", port);
            db.insertReceivedFileTransfer(fileReceiver.getFilename(), fileReceiver.getTotalBytes(), "localhost", String.valueOf(port));
            Platform.runLater(() -> showInformationMessage("File received successfully."));
        }).start();
    }
}