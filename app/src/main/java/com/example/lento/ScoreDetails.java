package com.example.lento;

public class ScoreDetails {
    private int page;
    private String genre;
    private String uploadDate;

    public ScoreDetails(int page, String genre, String uploadDate) {
        this.page = page;
        this.genre = genre;
        this.uploadDate = uploadDate;
    }

    public int getPage() {
        return page;
    }

    public String getGenre() {
        return genre;
    }

    public String getUploadDate() {
        return uploadDate;
    }
}
