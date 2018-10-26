package com.student.pro.prostudent.Objects;

public class Chat {
    String content,date,sender;

    public Chat(String content, String date, String sender) {
        this.content = content;
        this.date = date;
        this.sender = sender;
    }

    public Chat() {
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {

        return content;
    }

    public String getDate() {
        return date;
    }

    public String getSender() {
        return sender;
    }
}
