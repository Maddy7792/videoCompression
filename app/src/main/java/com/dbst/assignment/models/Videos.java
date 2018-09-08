package com.dbst.assignment.models;

public class Videos {

    private String steamUrl;

    public Videos() {
    }

    public Videos(String steamUrl) {
        this.steamUrl = steamUrl;

    }

    public String getSteamUrl() {
        return steamUrl;
    }

    public void setSteamUrl(String steamUrl) {
        this.steamUrl = steamUrl;
    }


}
