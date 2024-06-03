package com.example.filetransfer;

/**
 * Use this class to launch the application to avoid the following error:
 * https://stackoverflow.com/questions/54806788/javafx-cant-build-artifact-fxdeploy-is-not-available-in-this-jdk
 */
public class Launcher {
    public static void main(String[] args) {
        FileTransfer.main(args);
    }
}
