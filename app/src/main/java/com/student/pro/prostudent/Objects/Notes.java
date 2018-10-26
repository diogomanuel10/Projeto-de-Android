package com.student.pro.prostudent.Objects;

/**
 * Created by jonnh on 3/27/2018.
 */

public class Notes {
    private String title, content, tag_disc, id_disc, date;
    private String note_id;


    public Notes(String title, String content, String tag_disc, String id_disc, String date) {
        this.title = title;
        this.content = content;
        this.tag_disc = tag_disc;
        this.id_disc = id_disc;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getTag_disc() {
        return tag_disc;
    }

    public String getId_disc() {
        return id_disc;
    }

    public String getDate() {
        return date;
    }

    public String getNote_id() {
        return note_id;
    }

    public void setNote_id(String note_id) {
        this.note_id = note_id;
    }
}
