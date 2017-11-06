package de.mc.ladon.s3server.repository.impl;

import de.mc.ladon.s3server.entities.api.MongoS3User;
import de.mc.ladon.s3server.entities.api.S3Bucket;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document
public class MongoBucket implements S3Bucket {

    @Indexed(unique = true)
    private String bucketName;
    private Date creationDate;
    private MongoS3User mongoS3User;

    public MongoBucket() {
    }

    public MongoBucket(String bucketName, Date creationDate, MongoS3User mongoS3User) {
        this.bucketName = bucketName;
        this.creationDate = creationDate;
        this.mongoS3User = mongoS3User;
    }

    @Override
    public String getBucketName() {
        return this.bucketName;
    }

    @Override
    public Date getCreationDate() {
        return this.creationDate;
    }

    public MongoS3User getOwner() {
        return this.mongoS3User;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setMongoS3User(MongoS3User mongoS3User) {
        this.mongoS3User = mongoS3User;
    }
}
