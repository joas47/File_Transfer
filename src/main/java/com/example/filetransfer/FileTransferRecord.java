package com.example.filetransfer;

import java.time.Instant;

/**
 * Represents a record of a file transfer.
 * Each instance of this class stores information about a single file transfer, including the file name, file size,
 * transfer direction (upload or download), timestamp of the transfer, server address, and port used for the transfer.
 * This class can be used to store and read information about file transfers.
 */
public class FileTransferRecord {
    private final String filename;
    private final long filesize;
    private final String transferDirection;
    private final Instant timestamp;
    private final String server;
    private final String port;

    public FileTransferRecord(String filename, long filesize, String transferDirection, Instant timestamp, String server, String port) {
        this.filename = filename;
        this.filesize = filesize;
        this.transferDirection = transferDirection;
        this.timestamp = timestamp;
        this.server = server;
        this.port = port;
    }

    public String getServer() {
        return server;
    }

    public String getPort() {
        return port;
    }

    public String getFilename() {
        return filename;
    }

    public String getTransferDirection() {
        return transferDirection;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public long getFilesize() {
        return filesize;
    }
}
