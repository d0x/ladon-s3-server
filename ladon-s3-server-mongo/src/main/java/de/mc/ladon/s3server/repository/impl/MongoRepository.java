package de.mc.ladon.s3server.repository.impl;

import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import de.mc.ladon.s3server.entities.api.*;
import de.mc.ladon.s3server.entities.impl.S3ListBucketResultImpl;
import de.mc.ladon.s3server.entities.impl.S3ResponseHeaderImpl;
import de.mc.ladon.s3server.exceptions.InternalErrorException;
import de.mc.ladon.s3server.exceptions.InvalidDigestException;
import de.mc.ladon.s3server.exceptions.NoSuchBucketException;
import de.mc.ladon.s3server.exceptions.NoSuchKeyException;
import de.mc.ladon.s3server.repository.api.S3Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.gridfs.GridFsCriteria.whereFilename;

public class MongoRepository implements S3Repository {

    private static final boolean VALIDATION = false;

    private static Logger logger = LoggerFactory.getLogger(MongoRepository.class);

    private final GridFsTemplate gridFsTemplate;
    private final MongoTemplate mongoTemplate;

    public MongoRepository(GridFsTemplate gridFsTemplate, MongoTemplate mongoTemplate) {
        this.gridFsTemplate = gridFsTemplate;
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void setupIndex() {
        mongoTemplate.indexOps("fs.files").ensureIndex(new Index("metadata.bucketName", Sort.Direction.DESC));
        mongoTemplate.indexOps("fs.files").ensureIndex(new Index("filename", Sort.Direction.ASC));
    }

    @Override
    public List<S3Bucket> listAllBuckets(S3CallContext callContext) {
        return mongoTemplate.findAll(MongoBucket.class)
                .stream()
                .map(it -> (S3Bucket) it)
                .collect(Collectors.toList());
    }

    @Override
    public void createBucket(S3CallContext callContext, String bucketName, String locationConstraint) {
        mongoTemplate.save(new MongoBucket(bucketName, new Date(), new MongoS3User(callContext.getUser())));
    }

    @Override
    public void deleteBucket(S3CallContext callContext, String bucketName) {
        // TODO check user
        mongoTemplate.remove(query(where("bucketName").is(bucketName)));
        gridFsTemplate.delete(query(where("bucketName").is(bucketName)));
    }

    @Override
    public void createObject(S3CallContext callContext, String bucketName, String objectKey) {
        try {
            GridFSFile storedFile = gridFsTemplate.store(
                    callContext.getContent(),
                    toGridFsFileName(bucketName, objectKey),
                    callContext.getHeader().getContentType(),
                    new FileMetaData(bucketName, callContext.getUser(), objectKey, callContext.getHeader().getFullHeader())
            );

            if (VALIDATION) {
                if (storedFile == null) {
                    throw new InternalErrorException(objectKey, callContext.getRequestId());
                }

                Long contentLength = callContext.getHeader().getContentLength();
                String contentMD5 = callContext.getHeader().getContentMD5();

                if ((contentLength != null && storedFile.getLength() != contentLength) ||
                        (contentMD5 != null && !storedFile.getMD5().equals(contentMD5))
                        ) {
                    gridFsTemplate.delete(query(where("_id").is(storedFile.getId())));
                    throw new InvalidDigestException(objectKey, callContext.getRequestId());
                }
            }
        } catch (IOException e) {
            throw new InternalErrorException(objectKey, callContext.getRequestId());
        }
    }

    @Override
    public S3Object copyObject(S3CallContext callContext, String srcBucket, String srcObjectKey, String destBucket, String destObjectKey, boolean copyMetadata) {
        return null;
    }

    @Override
    public void getObject(S3CallContext callContext, String bucketName, String objectKey, boolean head) {

        GridFsResource resource = gridFsTemplate.getResource(toGridFsFileName(bucketName, objectKey));
        if (resource == null) {
            if (mongoTemplate.exists(query(where("bucketName").is(bucketName)), MongoBucket.class)) {
                throw new NoSuchKeyException(objectKey, callContext.getRequestId());
            } else {
                throw new NoSuchBucketException(bucketName, callContext.getRequestId());
            }
        }

        try {
            S3ResponseHeader header = new S3ResponseHeaderImpl();
            header.setContentLength(resource.contentLength());
            header.setContentType(resource.getContentType());

            callContext.setResponseHeader(header);

            if (!head) {
                callContext.setContent(resource.getInputStream());
            }
        } catch (IOException e) {
            logger.error("internal error", e);
            throw new InternalErrorException(objectKey, callContext.getRequestId());
        }
    }

    @Override
    public S3ListBucketResult listBucket(S3CallContext s3CallContext, String bucketName) {

        List<GridFSDBFile> files = gridFsTemplate.find(query(whereBucketNameIs(bucketName)));

        List<S3Object> s3Files = files.stream()
                .map(MongoS3Object::new)
                .collect(Collectors.toList());

        return new S3ListBucketResultImpl(s3Files, false, bucketName, null, null);
    }

    @Override
    public void deleteObject(S3CallContext callContext, String bucketName, String objectKey) {
        //TODO no feedback whether bucket/file existed before deleting it?
        gridFsTemplate.delete(query(whereFilename().is(toGridFsFileName(bucketName, objectKey))));
    }

    private String toGridFsFileName(String bucketName, String objectKey) {
        return bucketName + "/" + objectKey;
    }

    @Override
    public void getBucket(S3CallContext callContext, String bucketName) {

    }

    @Override
    public S3User getUser(S3CallContext callContext, String accessKey) {
        return null;
    }


    private static Criteria whereBucketNameIs(String bucketName) {
        return where("metadata.bucketName").is(bucketName);
    }

}
