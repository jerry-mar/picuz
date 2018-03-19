package com.jerry_mar.picuz.model;

import java.util.ArrayList;
import java.util.List;

public class Folder {
    private String name;
    private boolean used;
    private List<Image> images;
    private Image cover;

    public Folder(String name, boolean used) {
        this.name = name;
        this.used = used;
        images = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public List<Image> getImages() {
        return images;
    }

    public void add(Image image) {
        if (images.size() == 0) {
            cover = image;
        }
        images.add(image);
    }

    public Image getCover() {
        return cover;
    }

    public void setCover(Image cover) {
        this.cover = cover;
    }
}
