package com.w4po.securechat;

public class UserKey {
    public String uid, privateKey, publicKey;

    public UserKey (String uid, String privateKey, String publicKey) {
        this.uid = uid;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }
}
