package com.example.lento;

import java.util.Date;

public class Challenge {
    private int code;
    private String category;
    private String title;
    private Date startDate;
    private Date deadline;
    private int progress;

    public Challenge(int code, String category, String title, Date startDate, Date deadline, int progress) {
        this.code = code;
        this.category = category;
        this.title = title;
        this.startDate = startDate;
        this.deadline = deadline;
        this.progress = progress;
    }

    public int getCode() {
        return code;
    }

    public String getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getDeadline() {
        return deadline;
    }

    public int getProgress() {
        return progress;
    }

    // 필요한 경우 세터 메서드도 추가할 수 있습니다.
}
