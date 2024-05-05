package com.example.lento;

public class Score {
    private String title;
    private String composer;
    private String imagePath;
    private boolean isStarred; // 즐겨찾기 상태

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
    public boolean isStarred() {
        return isStarred;
    }

    public void setStarred(boolean starred) {
        isStarred = starred;
    }
}
