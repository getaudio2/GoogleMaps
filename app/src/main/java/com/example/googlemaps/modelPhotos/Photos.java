package com.example.googlemaps.modelPhotos;

public class Photos {

    public int page;
    public int pages;
    public int perpage;
    public int total;
    public Photo[] photos;

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

    public Photo[] getPhotos() {
        return photos;
    }
}
