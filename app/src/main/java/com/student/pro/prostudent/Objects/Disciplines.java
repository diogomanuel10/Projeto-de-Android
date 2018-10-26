package com.student.pro.prostudent.Objects;

/**
 * Created by jonnh on 3/15/2018.
 */

public class Disciplines {
    String name, year, tag, id;

    public Disciplines(String name, String year, String tag, String id) {
        this.name = name;
        this.year = year;
        this.tag = tag;
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public String getYear() {
        return year;
    }

    public String getTag() {
        return tag;
    }

    public String getId() {
        return id;
    }
}
