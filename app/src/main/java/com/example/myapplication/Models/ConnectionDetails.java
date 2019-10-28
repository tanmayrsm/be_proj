package com.example.myapplication.Models;


public class ConnectionDetails {
    public boolean ring ,connection ;
    public String uid;

    public ConnectionDetails(){}

    public ConnectionDetails(boolean ring, boolean connection, String uid) {
        this.ring = ring;
        this.connection = connection;
        this.uid = uid;
    }

    public boolean isRing() {
        return ring;
    }

    public void setRing(boolean ring) {
        this.ring = ring;
    }

    public boolean isConnection() {
        return connection;
    }

    public void setConnection(boolean connection) {
        this.connection = connection;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
