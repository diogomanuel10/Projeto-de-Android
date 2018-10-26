package com.student.pro.prostudent.Objects;

public class Course_Class {
    String course_id, class_id;

    public Course_Class(String course_id, String class_id) {
        this.course_id = course_id;
        this.class_id = class_id;
    }

    public Course_Class() {
    }

    public String getCourse_id() {
        return course_id;
    }

    public String getClass_id() {
        return class_id;
    }

    public void setCourse_id(String course_id) {
        this.course_id = course_id;
    }

    public void setClass_id(String class_id) {
        this.class_id = class_id;
    }
}
