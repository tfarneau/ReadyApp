package com.ready.readyapp;

import android.graphics.drawable.Drawable;

/**
 * Created by clement on 16/06/15.
 */
public class Person {
    int id;
    String name;
    String status;
    Drawable picture;
    Drawable online;

    public Person(int id, String name, String status, Drawable picture, Drawable online) {
        super();
        this.id = id;
        this.name = name;
        this.status = status;
        this.picture = picture;
        this.online = online;
    }

    public Person() {
        super();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Drawable getPicture() {
        return picture;
    }

    public void setPicture(Drawable picture) {
        this.picture = picture;
    }

    public Drawable getOnline() {
        return online;
    }

    public void setOnline(Drawable online) {
        this.online = online;
    }

    @Override
    public String toString() {
        return "Person [id=" + id + ", name=" + name + ", picture=" + picture
                + ", status=" + status + ", online=" + online + "]";
    }

}

