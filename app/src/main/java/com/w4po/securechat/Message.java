package com.w4po.securechat;

import androidx.annotation.Nullable;

public class Message {
    private String messageID, from, to, message;
    private Long timestamp;

    public Message() {

    }

    public Message(String messageID, String from, String to, String message, Long timestamp) {
        this(messageID, from, to, message);
        this.timestamp = timestamp;
    }

    public Message(String messageID, String from, String to, String message) {
        this.messageID = messageID;
        this.from = from;
        this.to = to;
        this.message = message;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        Message message = (Message) obj;
        return message.getMessageID().equals(messageID) &&
                message.getFrom().equals(from) &&
                message.getTo().equals(to);
    }
}
