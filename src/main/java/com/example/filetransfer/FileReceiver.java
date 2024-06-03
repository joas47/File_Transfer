package com.example.filetransfer;

import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ServerSocketFactory;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The FileReceiver class is responsible for receiving a file through a socket connection and saving it to disk.
 * It implements the Runnable interface to allow execution in a separate thread.
 * The class provides methods to check the receiving status, retrieve progress, filename, and total bytes of the received file.
 */
public class FileReceiver implements Runnable {
    private final String saveLocation;
    private final int port;
    private long totalBytes = -1;
    private long bytesReceived = 0;
    private String fName;

    public FileReceiver(String saveLocation, int port) {
        this.saveLocation = saveLocation;
        this.port = port;
    }

    /**
     * This method is responsible for receiving a file through a socket connection and saving it to disk.
     * It reads and parses metadata from the socket input stream, retrieves the filename and total bytes from the metadata JSON object,
     * and then writes the actual file content to the specified location on disk.
     */
    public void run() {
        // TODO: Make this SSL.
        ServerSocketFactory factory = ServerSocketFactory.getDefault();
        try (ServerSocket serverSocket = factory.createServerSocket(port);
             Socket clientSocket = serverSocket.accept();
             DataInputStream socketInput = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()))) {

            // read and parse metadata as JSON
            String metadata = socketInput.readUTF();
            JSONObject json = new JSONObject(metadata);
            String fileName = json.getString("name");
            fName = fileName;
            totalBytes = json.getLong("size");

            try (OutputStream fileOutput = new FileOutputStream(saveLocation + "/" + fileName)) {
                // write actual file content to disk
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = socketInput.read(buffer)) != -1) {
                    fileOutput.write(buffer, 0, bytesRead);
                    bytesReceived += bytesRead;
                }
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public double getProgress() {
        return totalBytes > 0 ? (double) bytesReceived / totalBytes : 1.0;
    }

    public boolean isReceiving() {
        return bytesReceived < totalBytes;
    }

    public String getFilename() {
        return fName;
    }

    public long getTotalBytes() {
        return totalBytes;
    }
}