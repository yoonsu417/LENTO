package com.example.lento;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabPagerAdapter extends FragmentPagerAdapter{
    public TabPagerAdapter(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT); // 수정된 부분
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        // 탭에 따른 Fragment 반환
        switch (position) {
            case 0:
                return new MyChallengeFragment();
            case 1:
                return new PracticeNoteFragment();
            case 2:
                return new StatisticsFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        // 탭 개수 반환
        return 3;
    }

    @NonNull
    @Override
    public CharSequence getPageTitle(int position) {
        // 탭의 제목 설정
        switch (position) {
            case 0:
                return "나의 챌린지";
            case 1:
                return "연습노트";
            case 2:
                return "통계";
            default:
                return null;
        }
    }
}
