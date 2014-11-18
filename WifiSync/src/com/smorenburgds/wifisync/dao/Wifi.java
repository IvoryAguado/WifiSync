package com.smorenburgds.wifisync.dao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table WIFI.
 */
public class Wifi {

    private Long id;
    private String name;
    private String password;
    private String rawData;

    public Wifi() {
    }

    public Wifi(Long id) {
        this.id = id;
    }

    public Wifi(Long id, String name, String password, String rawData) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.rawData = rawData;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRawData() {
        return rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

}
