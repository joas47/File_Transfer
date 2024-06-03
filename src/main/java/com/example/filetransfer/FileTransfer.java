package com.example.filetransfer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/**
 * The FileTransfer class represents an application for transferring files between devices.
 * It extends the Application class provided by JavaFX.
 * <p>
 * This class provides methods for starting and stopping the application, as well as accessing the database handler.
 * The application GUI consists of a tab pane with tabs for sending files, receiving files, and displaying log information.
 */
public class FileTransfer extends Application {

    private static DatabaseHandler db;

    /**
     * Retrieves the instance of the DatabaseHandler class.
     * Used by other classes to access the database utilizing the singleton pattern.
     */
    public static DatabaseHandler getDatabaseHandler() {
        return db;
    }

    public static void main(String[] args) {
        db = DatabaseHandler.getInstance();
        launch(args);
    }

    /**
     * Starts the application and sets up the main user interface.
     *
     * @param primaryStage the main stage to display the user interface
     */
    @Override
    public void start(Stage primaryStage) {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        LogTab logTab = new LogTab(primaryStage);
        SendTab sendTab = new SendTab(primaryStage, logTab);
        ReceiveTab receiveTab = new ReceiveTab(primaryStage, logTab);


        tabPane.getTabs().addAll(sendTab, receiveTab, logTab);

        Scene scene = new Scene(tabPane, 550, 250);

        primaryStage.setScene(scene);
        primaryStage.setTitle("File Transfer");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void stop() {
        db.closeConnection();
    }
}