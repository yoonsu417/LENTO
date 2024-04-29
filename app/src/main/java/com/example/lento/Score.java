package com.example.lento;

public class Score {
    private String title;
    private String composer;
    private String imagePath;

    public Score(String title, String composer, String imagePath) {
        this.title = title;
        this.composer = composer;
        this.imagePath = imagePath;
    }

    public String getTitle() {
        return title;
    }

    public String getComposer() {
        return composer;
    }

    public String getImagePath() {
        return imagePath;
    }
}
