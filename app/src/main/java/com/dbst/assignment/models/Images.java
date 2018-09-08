package com.dbst.assignment.models;

public class Images {

    private String Id;
    private String Name;
    private String Image;

    public Images() {
    }

    public Images(String id, String name, String image) {
        this.Id = id;
        Name = name;
        Image = image;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        this.Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }
}
