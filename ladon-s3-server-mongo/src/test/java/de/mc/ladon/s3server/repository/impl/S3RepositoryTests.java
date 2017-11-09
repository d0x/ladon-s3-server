package de.mc.ladon.s3server.repository.impl;

import com.google.common.io.ByteStreams;
import de.mc.ladon.s3server.entities.api.S3Bucket;
import de.mc.ladon.s3server.entities.api.S3CallContext;
import de.mc.ladon.s3server.entities.api.S3ListBucketResult;
import de.mc.ladon.s3server.entities.api.S3RequestHeader;
import de.mc.ladon.s3server.entities.impl.S3ResponseHeaderImpl;
import de.mc.ladon.s3server.entities.impl.S3UserImpl;
import de.mc.ladon.s3server.exceptions.NoSuchBucketException;
import de.mc.ladon.s3server.exceptions.NoSuchKeyException;
import de.mc.ladon.s3server.repository.api.S3Repository;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class S3RepositoryTests {

    private final String exampleBucketName = "examplebucketname";
    private byte[] exampleFile = "Hallo Welt".getBytes();
    private String exampleKey = "exampleKey";

    private S3UserImpl user = new S3UserImpl(null, null, null, null, null);

    // TODO: Use Parameterized tests?
    protected S3Repository cut;


    @Test
    public void should_return_empty_list_when_no_bucket_is_created() throws Exception {
        List<S3Bucket> s3Buckets = cut.listAllBuckets(null);
        assertTrue(s3Buckets.isEmpty());
    }

    @Test
    public void should_list_created_bucket() throws Exception {
        createBucket();
        List<S3Bucket> s3Buckets = cut.listAllBuckets(null);
        assertTrue(s3Buckets.size() == 1);
    }

    @Test(expected = NoSuchBucketException.class)
    public void should_throw_error_on_get_bucket_which_doesnt_exists() throws Exception {
        cut.getBucket(new DummyS3CallContext(user), exampleBucketName);
    }

    @Test
    public void should_get_bucket() throws Exception {
        createBucket();

        cut.getBucket(null, exampleBucketName);
        // no exception thrown
    }

    @Test
    public void should_read_a_stored_file() throws Exception {
        createBucket();
        storeFile();

        DummyS3CallContext antwort = new DummyS3CallContext(user);
        cut.getObject(antwort, exampleBucketName, exampleKey, false);
        byte[] readFile = ByteStreams.toByteArray(antwort.getContent());

        assertTrue(Arrays.equals(readFile, exampleFile));
    }

    @Test
    public void should_copy_file() throws Exception {
        createBucket();
        storeFile();

        String destBucket = exampleBucketName + "copy";
        String destObjectKey = exampleFile + "copy";

        DummyS3CallContext callContext = new DummyS3CallContext(user);
        cut.copyObject(callContext, exampleBucketName, exampleKey, destBucket, destObjectKey, true);

        callContext = new DummyS3CallContext(user);
        cut.getObject(callContext, destBucket, destObjectKey, false);
        byte[] readFile = ByteStreams.toByteArray(callContext.getContent());

        assertTrue(Arrays.equals(readFile, exampleFile));
    }

    @Test
    public void should_read_store_mime_type() throws Exception {
        createBucket();
        storeFile();

        DummyS3CallContext antwort = new DummyS3CallContext(user);
        cut.getObject(antwort, exampleBucketName, exampleKey, false);

        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        S3ResponseHeaderImpl.appendHeaderToResponse(mockResponse, (S3ResponseHeaderImpl) antwort.getResponseHeader());

        assertTrue("text/plain".equals(mockResponse.getContentType()));
    }

    @Test
    public void should_delete_a_stored_file() throws Exception {
        createBucket();
        storeFile();

        cut.deleteObject(null, exampleBucketName, exampleKey);

        DummyS3CallContext antwort = new DummyS3CallContext(user);
        try {
            cut.getObject(antwort, exampleBucketName, exampleKey, false);
            fail();
        } catch (NoSuchKeyException e) {
            // pass
        }
    }

    @Test(expected = NoSuchBucketException.class)
    public void should_delete_files_inside_bucket() throws Exception {
        createBucket();
        storeFile();

        cut.deleteBucket(null, exampleBucketName);

        DummyS3CallContext antwort = new DummyS3CallContext(user);
        cut.getObject(antwort, exampleBucketName, exampleKey, false);
    }

    @Test
    public void should_delete_bucket() throws Exception {
        createBucket();
        storeFile();

        cut.deleteBucket(null, exampleBucketName);

        assertTrue(cut.listAllBuckets(null).isEmpty());
    }

    @Test
    public void should_throw_on_deleting_object_on_transient_bucket() throws Exception {
        cut.deleteObject(null, exampleBucketName, exampleKey);

        DummyS3CallContext antwort = new DummyS3CallContext(user);
        try {
            cut.getObject(antwort, exampleBucketName, exampleKey, false);
            fail();
        } catch (NoSuchBucketException e) {
            // pass
        }
    }

    @Test
    public void should_list_empty_bucket() throws Exception {
        S3ListBucketResult s3ListBucketResult = cut.listBucket(null, exampleBucketName);

        assertTrue(s3ListBucketResult.getObjects().isEmpty());
    }

    @Test
    public void should_list_created_file() throws Exception {
        createBucket();
        storeFile();

        S3ListBucketResult s3ListBucketResult = cut.listBucket(null, exampleBucketName);

        assertTrue(s3ListBucketResult.getObjects().size() == 1);
        assertTrue(s3ListBucketResult.getObjects().get(0).getKey().equals(exampleKey));
    }

    private void createBucket() {
        DummyS3CallContext callContext = new DummyS3CallContext(user);
        cut.createBucket(callContext, exampleBucketName, null);
    }

    private void storeFile() throws NoSuchAlgorithmException, IOException {

        S3RequestHeader s3RequestHeader = mock(S3RequestHeader.class);
        when(s3RequestHeader.getContentMD5()).thenReturn("5c372a32c9ae748a4c040ebadc51a829");
        when(s3RequestHeader.getContentType()).thenReturn("text/plain");

        S3CallContext callContext = mock(S3CallContext.class);
        when(callContext.getHeader()).thenReturn(s3RequestHeader);
        when(callContext.getContent()).thenReturn(new ByteArrayInputStream(exampleFile));
        when(callContext.getUser()).thenReturn(user);


        cut.createObject(callContext, exampleBucketName, exampleKey);
    }
}
