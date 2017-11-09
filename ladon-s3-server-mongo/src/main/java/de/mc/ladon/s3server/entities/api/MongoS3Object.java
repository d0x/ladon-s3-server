package de.mc.ladon.s3server.entities.api;


import com.mongodb.gridfs.GridFSFile;
import de.mc.ladon.s3server.repository.impl.FileMetaData;

import java.io.InputStream;
import java.util.Date;

public class MongoS3Object implements S3Object {

    private GridFSFile delegate;

    public MongoS3Object(GridFSFile it) {
        delegate = it;
    }

    @Override
    public String getKey() {
        return (String) delegate.getMetaData().get("filename");
    }

    @Override
    public Date getLastModified() {
        return delegate.getUploadDate();
    }

    @Override
    public S3User getOwner() {
        return FileMetaData.STATIC_USER;
    }

    @Override
    public String getETag() {
        // TODO
        return null;
    }

    @Override
    public String getVersionId() {
        // TODO
        return null;
    }

    @Override
    public Long getSize() {
        return delegate.getLength();
    }

    @Override
    public String getMimeType() {
        return delegate.getContentType();
    }

    @Override
    public String getStorageClass() {
        return null;
    }

    @Override
    public String getBucket() {
        return (String) delegate.getMetaData().get("bucketName");
    }

    @Override
    public boolean isDeleted() {
        return false;
    }

    @Override
    public S3Metadata getMetadata() {
        return null;
    }

    @Override
    public InputStream getContent() {
        return null;
    }

    @Override
    public boolean isLatest() {
        return false;
    }
}
