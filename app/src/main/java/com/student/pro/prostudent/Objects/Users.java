package com.student.pro.prostudent.Objects;

/**
 * Created by jonnh on 3/29/2018.
 */

public class Users {
    public String username, email, name, surname, url, status;

    public Users() {
    }

    public Users(String username, String email, String name, String surname, String url, String status) {
        this.username = username;
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.url = url;
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public String getStatus() {
        return status;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getUrl() {
        return url;
    }
}
