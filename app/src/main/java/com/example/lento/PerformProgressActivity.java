package com.example.lento;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;

public class PerformProgressActivity extends AppCompatActivity {
    private LineChart lineChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performprogress);

        lineChart = findViewById(R.id.lineChart);
        setupLineChart();
    }
    private void setupLineChart() {
        // 데이터 설정
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(1, 3)); // 예시 데이터
        entries.add(new Entry(6, 4));
        entries.add(new Entry(8, 5));
        entries.add(new Entry(12, 6));
        entries.add(new Entry(19, 7));

        LineDataSet dataSet = new LineDataSet(entries, "연주 정확도");

        // 데이터 표시 원 크기 설정
        dataSet.setCircleRadius(8f); // 원하는 크기로 설정
        dataSet.setCircleHoleRadius(4f); // 중앙 구멍 크기 설정
        dataSet.setDrawCircles(true); // 원 그리기
        dataSet.setDrawCircleHole(false); // 중앙 구멍 그리기
        //dataSet.setCircleColor(R.color.third);
        //dataSet.setCircleHoleColor(R.color.third);

        // 연결 라인 제거
        dataSet.setDrawValues(false); // y값 동그라미 위에 표시하는거
        dataSet.setLineWidth(0f); // 라인 두께를 0으로 설정하여 라인 제거

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // 설명 텍스트 제거
        Description description = new Description();
        description.setText("");
        lineChart.setDescription(description);

        // X축 라벨 제거 및 그리드 선 제거
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawLabels(false);
        xAxis.setDrawGridLines(false);

        // Y축 라벨 제거 및 그리드 선 설정
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawLabels(false);
        leftAxis.setDrawGridLines(true);
        leftAxis.setLabelCount(5, true); // 가로 그리드 선 5줄로 설정
        leftAxis.setGridLineWidth(2f);


        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setDrawLabels(false);
        rightAxis.setDrawGridLines(false); // 가로 그리드 선 제거
        // X축에 연습한 날짜 표시
        String[] practiceDates = {"5/17", "5/18", "5/19", "5/20", "5/21"};
        xAxis.setValueFormatter(new IndexAxisValueFormatter(practiceDates));
        xAxis.setGranularity(1f); // 간격 설정 (1f는 각 데이터의 간격이 1임을 나타냅니다)
        xAxis.setLabelCount(practiceDates.length); // x축 라벨 개수 설정
        xAxis.setTextSize(10f); // X축 라벨의 텍스트 크기 조절 (원하는 크기로 변경)

        // 라인 차트 새로고침
        lineChart.invalidate(); // refresh

        /*// 점 클릭 이벤트
        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int xValue = (int) e.getX();
                Toast.makeText(MainActivity.this, "Selected: " + xValue, Toast.LENGTH_SHORT).show();

                // 다음 페이지로 이동
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("date", xValue);
                startActivity(intent);
            }*/

           /* @Override
            public void onNothingSelected() {
            }
        });*/
    }

}
