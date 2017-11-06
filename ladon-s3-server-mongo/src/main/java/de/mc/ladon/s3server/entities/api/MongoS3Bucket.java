package de.mc.ladon.s3server.entities.api;

import de.mc.ladon.s3server.repository.impl.MongoBucket;

import java.util.Date;

public class MongoS3Bucket implements S3Bucket {

    private final MongoBucket mongoBucket;

    public MongoS3Bucket(MongoBucket mongoBucket) {
        this.mongoBucket = mongoBucket;
    }

    @Override
    public String getBucketName() {
        return mongoBucket.getBucketName();
    }

    @Override
    public Date getCreationDate() {
        return mongoBucket.getCreationDate();
    }

    @Override
    public MongoS3User getOwner() {
        return mongoBucket.getOwner();
    }

}
