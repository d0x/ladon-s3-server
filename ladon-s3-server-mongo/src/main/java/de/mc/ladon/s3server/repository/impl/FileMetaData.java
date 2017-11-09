package de.mc.ladon.s3server.repository.impl;

import de.mc.ladon.s3server.entities.api.MongoS3User;
import de.mc.ladon.s3server.entities.api.S3User;
import de.mc.ladon.s3server.entities.impl.S3UserImpl;

import java.util.Map;

public class FileMetaData {

    public static final MongoS3User STATIC_USER = new MongoS3User(new S3UserImpl("SYSTEM", "SYSTEM", null, null, null));

    private MongoS3User user = STATIC_USER;
    private final String bucketName;
    private final String filename;

    private final Map<String, String> metadata;

    public FileMetaData(String bucketName, S3User user, String filename, Map<String, String> header) {
        this.bucketName = bucketName;
        this.user = STATIC_USER;
        this.filename = filename;
        this.metadata = header;
    }

}
