package com.example.googlemaps.modelPhotos;

import java.util.ArrayList;

public class Photos {

    public int page;
    public int pages;
    public int perpage;
    public int total;
    public ArrayList<Photo> photos;

    public int getPage() {
        return page;
    }

    public int getPages() {
        return pages;
    }

    public int getPerpage() {
        return perpage;
    }

    public int getTotal() {
        return total;
    }

    public ArrayList<Photo> getPhotos() {
        return photos;
    }
}
