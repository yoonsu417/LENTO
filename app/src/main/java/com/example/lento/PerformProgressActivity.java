package com.example.lento;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;

public class PerformProgressActivity extends AppCompatActivity {
    private BarChart barChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performprogress);

        ImageView backImageView = findViewById(R.id.back);
        backImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        barChart = findViewById(R.id.barChart);
        // 사용자 정의 렌더러 설정
        barChart.setRenderer(new RoundedBarChartRenderer(barChart, new ChartAnimator(), barChart.getViewPortHandler(), 25f));
        setupBarChart();

        // 막대를 클릭했을 때 다음 페이지로 이동하는 코드 추가
        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                // 막대를 클릭했을 때 실행되는 코드
                // 다음 페이지로 이동하는 코드를 여기에 작성합니다.
                // Intent를 사용하여 다음 페이지로 이동할 수 있습니다.
                Intent intent = new Intent(PerformProgressActivity.this, PerformAccuracyActivity.class);
                startActivity(intent);
            }

            @Override
            public void onNothingSelected() {
                // 아무 막대도 선택되지 않았을 때 실행되는 코드
            }
        });
    }
    private void setupBarChart() {
        // 데이터 설정
        ArrayList<BarEntry> entries = new ArrayList<>();
        // (x, y) 쌍으로 입력합니다. y 값은 연주 정확도를 나타냅니다.
        entries.add(new BarEntry(0, 40)); // MON
        entries.add(new BarEntry(1, 100)); // TUE
        entries.add(new BarEntry(2, 60)); // WED
        entries.add(new BarEntry(3, 30)); // THU
        entries.add(new BarEntry(4, 100)); // FRI
        entries.add(new BarEntry(5, 100)); // SAT
        entries.add(new BarEntry(6, 100)); // SUN

        BarDataSet dataSet = new BarDataSet(entries, "하루 평균 연주 정확도");

        // 막대 색상 설정
        dataSet.setColors(new int[]{
                Color.parseColor("#FF95A9FE"), // 정확도가 100 미만일 때 색상
                Color.parseColor("#EEEFF3") // 정확도가 100일 때 색상
        });


        // 막대 위에 수치 표시하지 않기
        dataSet.setDrawValues(false);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        // 차트 배경색 제거
        barChart.setBackgroundColor(Color.TRANSPARENT);
        barChart.setDrawGridBackground(false); // 그리드 배경 숨김

        // 설명 텍스트 제거
        Description description = new Description();
        description.setText("");
        barChart.setDescription(description);

        // X축 설정
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"}));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.DKGRAY);
        xAxis.setDrawGridLines(false); // 세로 그리드 라인 제거
        xAxis.setDrawAxisLine(false); // X축 라인 제거

        // Y축 설정
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawLabels(false);
        leftAxis.setDrawGridLines(true); // 가로 그리드 라인
        leftAxis.setDrawAxisLine(false); // Y축 라인 제거
        leftAxis.setLabelCount(5, true);
        leftAxis.setGridLineWidth(1f);
        leftAxis.setGridColor(Color.parseColor("#F3F3F3"));

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setDrawLabels(false);
        rightAxis.setDrawGridLines(false); // 오른쪽 Y축 라인 제거
        rightAxis.setDrawAxisLine(false); // 오른쪽 Y축 라인 제거

        // 막대 차트 스타일
        barChart.getLegend().setEnabled(false); // 범례 숨김
        barChart.setDrawGridBackground(false); // 그리드 배경 숨김
        barChart.setDrawValueAboveBar(true); // 막대 위에 값 표시
        barChart.setFitBars(true); // 막대 크기 조정
        barChart.setExtraOffsets(10f, 10f, 10f, 10f); // 여백 설정
        barChart.setTouchEnabled(false); // 차트 터치 비활성화

        // 막대 너비 설정
        barData.setBarWidth(0.5f);

        // 차트 새로고침
        barChart.invalidate();
    }


}
