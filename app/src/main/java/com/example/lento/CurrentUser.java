// 현재 로그인 된 회원이 누구인지를 알기 위해
// 전역변수 설정을 위한 클래스 파일.

package com.example.lento;

import android.app.Application;

public class CurrentUser extends Application {
    private String email;

    @Override
    public void onCreate() {
        super.onCreate();
        email = "";
    }

    public void setCurrentUser (String email) {
        this.email = email;
    }

    public String getCurrentUser () {
        return email;
    }
}
