package com.w4po.securechat;

import androidx.annotation.Nullable;

public class Contact {
    private String uid, name, publicKey, status, image;
    private Object userState;

    public Contact() {}
    public Contact(String uid, String name, String publicKey, String status, String image, Object userState) {
        this.uid = uid;
        this.name = name;
        this.publicKey = publicKey;
        this.status = status;
        this.image = image;
        this.userState = userState;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public Object getUserState() { return userState; }
    public void setUserState(Object userState) { this.userState = userState; }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof String)
            return obj.equals(uid);
        else
            return ((Contact) obj).getUid().equals(uid);
    }
}
