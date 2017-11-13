package de.mc.ladon.s3server.repository.impl;

import de.mc.ladon.s3server.entities.api.*;
import de.mc.ladon.s3server.entities.impl.S3RequestParamsImpl;
import org.mockito.Mockito;

import java.io.InputStream;
import java.util.UUID;

import static org.mockito.Mockito.when;

public class DummyS3CallContext implements S3CallContext {

    private S3User user;
    private S3RequestHeader header;
    private S3RequestId requestId = () -> UUID.randomUUID().toString();
    private String method;
    private String uri;
    private String queryString;
    private InputStream content;
    private S3ResponseHeader responseHeader;

    public DummyS3CallContext(S3User user) {
        this.user = user;
    }

    @Override
    public S3User getUser() {
        return user;
    }

//    public void setUser(S3User user) {
//        this.user = user;
//    }

    @Override
    public S3RequestHeader getHeader() {
        return header;
    }

//    public void setHeader(S3RequestHeader header) {
//        this.header = header;
//    }

    @Override
    public S3RequestParams getParams() {
        S3RequestParams s3RequestParams = Mockito.mock(S3RequestParams.class);
        when(s3RequestParams.getMaxKeys()).thenReturn(1000);
        return s3RequestParams;
    }

//    public void setParams(S3RequestParams params) {
//        this.params = params;
//    }

    @Override
    public S3RequestId getRequestId() {
        return requestId;
    }

//    public void setRequestId(S3RequestId requestId) {
//        this.requestId = requestId;
//    }

    @Override
    public String getMethod() {
        return method;
    }

//    public void setMethod(String method) {
//        this.method = method;
//    }

    @Override
    public String getUri() {
        return uri;
    }

//    public void setUri(String uri) {
//        this.uri = uri;
//    }

    @Override
    public String getQueryString() {
        return queryString;
    }

//    public void setQueryString(String queryString) {
//        this.queryString = queryString;
//    }

    @Override
    public InputStream getContent() {
        return content;
    }

    @Override
    public void setResponseHeader(S3ResponseHeader responseHeader) {
        this.responseHeader = responseHeader;
//        S3ResponseHeaderImpl.appendHeaderToResponse(responseHeader, (S3ResponseHeaderImpl) responseHeader);
//        this.header = responseHeader;
    }

    public S3ResponseHeader getResponseHeader() {
        return responseHeader;
    }

    @Override
    public void setContent(InputStream content) {
        this.content = content;
    }
}
