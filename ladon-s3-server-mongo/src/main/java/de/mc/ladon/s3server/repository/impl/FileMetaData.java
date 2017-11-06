package de.mc.ladon.s3server.repository.impl;

import de.mc.ladon.s3server.entities.api.MongoS3User;
import de.mc.ladon.s3server.entities.api.S3User;

import java.util.Map;

public class FileMetaData {

    private final MongoS3User user;
    private final String bucketName;
    private final String filename;

    private final Map<String, String> metadata;

    public FileMetaData(String bucketName, S3User user, String filename, Map<String, String> header) {
        this.bucketName = bucketName;
        this.user = new MongoS3User(user);
        this.filename = filename;
        this.metadata = header;
    }

}
