package com.example.lento;

public class Recent {
    private String imagePath;
    private String title;
    private String composer;
    private String genre;

    public Recent(String imagePath, String title, String composer, String genre) {
        this.imagePath = imagePath;
        this.title = title;
        this.composer = composer;
        this.genre = genre;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComposer() {
        return composer;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }


}
