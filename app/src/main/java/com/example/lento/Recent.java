package com.example.lento;

public class Recent {
    private String name;

    private String imagePath;
    private String title;
    private String composer;
    private String genre;

    private String recentDate;


    public Recent(String name, String imagePath, String title, String composer, String genre, String recentDate) {
        this.name = name;
        this.imagePath = imagePath;
        this.title = title;
        this.composer = composer;
        this.genre = genre;
        this.recentDate = recentDate;
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

    public String getRecentDate() {
        return recentDate;
    }

    public void setRecentDate(String recentDate) {
        this.recentDate = recentDate;
    }
}
