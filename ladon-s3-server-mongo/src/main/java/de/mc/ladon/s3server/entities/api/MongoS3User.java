package de.mc.ladon.s3server.entities.api;

import de.mc.ladon.s3server.entities.api.S3User;

import java.util.Set;

public class MongoS3User implements S3User {

    private String userId;
    private String userName;
    private String secretKey;
    private String publicKey;
    private Set<String> roles;

    public MongoS3User() {
    }

    public MongoS3User(S3User user) {
        this.userId = user.getUserId();
        this.userName = user.getUserName();
        this.secretKey = user.getSecretKey();
        this.publicKey = user.getPublicKey();
        this.roles = user.getRoles();
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }


    @Override
    public String getUserId() {
        return this.userId;
    }

    @Override
    public String getUserName() {
        return this.userName;
    }

    @Override
    public String getSecretKey() {
        return this.secretKey;
    }

    @Override
    public String getPublicKey() {
        return this.publicKey;
    }

    @Override
    public Set<String> getRoles() {
        return this.roles;
    }
}
