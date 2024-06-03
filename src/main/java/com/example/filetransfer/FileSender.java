package com.example.filetransfer;

import org.json.JSONObject;

import javax.net.SocketFactory;
import java.io.*;
import java.net.Socket;

/**
 * The FileSender class is responsible for sending a file to a specified server and port number.
 * It provides methods to check if the file is currently being sent, get the total size of the file,
 * send the file, and retrieve the progress of the file sending operation.
 */
public class FileSender {
    private final String filename;
    private final String server;
    private final int port;
    private final long totalBytes;
    private long bytesSent = 0;

    public FileSender(String filename, String server, int port) {
        this.filename = filename;
        this.server = server;
        this.port = port;
        this.totalBytes = new File(filename).length();
    }

    /**
     * Sends a file over the network using a socket connection.
     * The method reads the file, creates JSON-formatted metadata,
     * and writes the metadata as well as the file content to the socket connection.
     * The metadata contains the name and the total size of the file.
     * Right now the connection is not secure but can be implemented using SSL.
     *
     * @throws IOException if an I/O error occurs when creating the socket,
     *                     reading the file or writing to the socket connection
     */
    public void send() throws IOException {
        // TODO: Make this SSL: https://github.com/Hakky54/sslcontext-kickstart
        // SocketFactory factory = SSLSocketFactory.getDefault();
        SocketFactory factory = SocketFactory.getDefault();

        try (Socket socket = factory.createSocket(server, port);
             InputStream fileInput = new FileInputStream(filename);
             DataOutputStream socketOutput = new DataOutputStream(socket.getOutputStream())) {
            byte[] buffer = new byte[8192];
            int bytesRead;

            // create JSON formatted metadata
            JSONObject json = new JSONObject();
            json.put("name", new File(filename).getName());
            json.put("size", totalBytes);
            String metadata = json.toString();

            // write metadata to socket
            socketOutput.writeUTF(metadata);

            // write actual file content to socket
            while ((bytesRead = fileInput.read(buffer)) != -1) {
                socketOutput.write(buffer, 0, bytesRead);
                bytesSent += bytesRead;
            }
        }
    }

    public boolean isSending() {
        return bytesSent < totalBytes;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public double getProgress() {
        // If the total bytes is more than 0, return the ratio of bytes sent to total bytes.
        return totalBytes > 0 ? (double) bytesSent / totalBytes : 1.0;
    }
}