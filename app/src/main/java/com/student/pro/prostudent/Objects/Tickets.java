package com.student.pro.prostudent.Objects;

/**
 * Created by jonnh on 3/27/2018.
 */

public class Tickets {
    private String title;
    private String content;
    private String sprivate;
    private String user_id;
    private String id_disc;
    private String tag_disc;
    private String date;
    private String url;
    private String ticket_id;
    private String response;
    private String response_date;
    private String solved;


    public Tickets(String title, String content, String sprivate, String user_id, String id_disc, String tag_disc, String date, String url,String solved) {
        this.title = title;
        this.content = content;
        this.sprivate = sprivate;
        this.user_id = user_id;
        this.id_disc = id_disc;
        this.tag_disc = tag_disc;
        this.date = date;
        this.url = url;
        this.solved=solved;
    }

    public Tickets(String response, String response_date) {
        this.response = response;
        this.response_date = response_date;
    }

    public String getSolved() {
        return solved;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getSprivate() {
        return sprivate;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getId_disc() {
        return id_disc;
    }

    public String getTag_disc() {
        return tag_disc;
    }

    public String getDate() {
        return date;
    }

    public String getUrl() {
        return url;
    }

    public String getTicket_id() {
        return ticket_id;
    }

    public String getResponse() {
        return response;
    }

    public String getResponse_date() {
        return response_date;
    }

    public void setTicket_id(String ticket_id) {
        this.ticket_id = ticket_id;
    }


}
