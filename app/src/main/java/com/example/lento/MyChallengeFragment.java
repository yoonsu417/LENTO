package com.example.lento;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
public class MyChallengeFragment extends Fragment {
    private boolean isFirstTime = true; // 처음인지 여부를 나타내는 플래그
    private Button ingBt;
    private Button endBt;

    public MyChallengeFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_challenge, container, false);
        ImageView addchell = view.findViewById(R.id.addchell);
        addchell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 클릭하면 이동할 화면으로 인텐트 생성
                Intent intent = new Intent(getActivity(), ChellengeActivity.class);
                startActivity(intent); // 화면 전환
            }
        });

        // 처음에는 진행 중 프래그먼트를 추가
        if (isFirstTime) {
            getParentFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new IngFragment())
                    .commit();
            isFirstTime = false; // 다음에는 이 부분이 실행되지 않도록 플래그 변경
        }

        // 진행중 버튼 클릭 시
        ingBt = view.findViewById(R.id.ingBt);
        ingBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 진행중 프래그먼트를 보여줌
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new IngFragment())
                        .commit();

                // 진행중 버튼의 배경색 변경
                ingBt.setBackgroundResource(R.drawable.challingbtn);
                // 마감 버튼의 배경색 원래대로 변경
                endBt.setBackgroundResource(R.drawable.challendbtn);
            }
        });

        // 마감 버튼 클릭 시
        endBt = view.findViewById(R.id.endBt);
        endBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 마감 프래그먼트를 보여줌
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new EndFragment())
                        .commit();

                // 마감 버튼의 배경색 변경
                endBt.setBackgroundResource(R.drawable.challingbtn);
                // 진행중 버튼의 배경색 원래대로 변경
                ingBt.setBackgroundResource(R.drawable.challendbtn);
            }
        });

        return view;
    }


}
