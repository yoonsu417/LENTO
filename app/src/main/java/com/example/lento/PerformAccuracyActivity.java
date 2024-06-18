package com.example.lento;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ca.uol.aig.fftpack.RealDoubleFFT;


public class PerformAccuracyActivity extends AppCompatActivity {

    private SQLiteHelper dbHelper;
    TextView date, accuracy;
    ImageView back;
    SQLiteDatabase db;
    Button goHome;
    Button again;
    public static final String SHARED_PREF_NAME = "practicePrefs";
    public static final String KEY_IMAGE_PATH = "imagePath";
    public static final String KEY_PRACTICE_DATE = "practiceDate";

    int scale2;
    ArrayList<Integer> playScale = new ArrayList<>();
    ArrayList<Integer> check = new ArrayList<>();

    // *** FFT 관련 ***
    private static final String WAV_FILE_PATH = "/storage/emulated/0/Download/test.wav"; // test wav 파일 경로
    // 피치에 따른 계이름 변환
    private static final int REQUEST_PERMISSION = 100;
    private static final int SAMPLE_RATE = 22050 ;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private RealDoubleFFT transformer;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performaccuracy);

        dbHelper = new SQLiteHelper(this);

        date = (TextView)findViewById(R.id.accuracyDate);
        back = (ImageView)findViewById(R.id.back);
        accuracy = (TextView)findViewById(R.id.accuracyPer);

        // *** FFT 관련 ***
        // RealDoubleFFT 클래스 컨스트럭터는 한번에 처리할 샘플들의 수를 받는다. 그리고 출력될 주파수 범위들의 수를 나타낸다.
        // FFT 변환을 위한 초기화
        transformer = new RealDoubleFFT(SAMPLE_RATE);

        // test 권한 추가
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        } else {
            analyzeWavFile();
        }

        OpenCVtestActivity openCVtestActivity = new OpenCVtestActivity();
        // 지금은 고향의 봄 악보로 고정
        openCVtestActivity.processImage(this, R.drawable.sheet);

        List<int[]> beatPitch = openCVtestActivity.getBeatPitchStat();

        System.out.println("PerfromAccuracyActivity: 박자, 계이름, 객체 위치 출력");
        if (beatPitch != null) {
            for (int[] list : beatPitch) {
                System.out.println(Arrays.toString(list));
            }
            MatchScale(beatPitch, playScale);
        } else {
            Log.e("PerformAccuracyActivity", "beatPitch 리스트가 null입니다.");
        }

        // 틀린 부분 체크 위한 출력
        Log.d("Match", "총 check: " + check);

        // 오늘 날짜 출력
        Date currentDate = new Date();
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formatDate = mFormat.format(currentDate);

        date.setText(formatDate);

        // 정확도 계산
        double accuracyPer = 0;
        double total = beatPitch.toArray().length;
        double correct = total - check.toArray().length;
        accuracyPer = (correct / total * 100);
        String result = String.format("%.1f", accuracyPer);
        accuracy.setText("연주정확도 : " + result + "%");

        // 페이지 뒤로 가기
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // 이미지 경로 및 연습한 날짜 저장
        String imagePath = getIntent().getStringExtra("imagePath");
        String practicedate = formatDate;

        RecentPractice recentPractice = new RecentPractice(this);
        recentPractice.saveDB(imagePath,practicedate);

        goHome = (Button)findViewById(R.id.homeButton);
        goHome.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(PerformAccuracyActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
        again = (Button)findViewById(R.id.again);
        again.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(PerformAccuracyActivity.this, PlayActivity.class);
                startActivity(intent);
            }
        });
    }


    // *** FFT 관련 ***
    // 권한o -> 함수 실행
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            analyzeWavFile();
        }
    }

    // FFT 사용 + 분석 함수
    private void analyzeWavFile() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    File wavFile = new File(WAV_FILE_PATH);
                    int bufferSize = SAMPLE_RATE; //SAMPLE_RATE = 22050. 120템포 기준 반박자씩 처리
                    short[] shortBuffer = new short[bufferSize];
                    double[] doubleData = new double[bufferSize];

                    try (FileInputStream fis = new FileInputStream(wavFile)) {
                        byte[] byteBuffer = new byte[bufferSize * 2]; // 16bit = 2 bytes

                        int bytesRead;
                        while ((bytesRead = fis.read(byteBuffer)) != -1) {
                            int shortsRead = bytesRead / 2;

                            // byte[] to short[]
                            for (int i = 0; i < shortsRead; i++) {
                                shortBuffer[i] = (short) ((byteBuffer[2 * i] & 0xFF) | (byteBuffer[2 * i + 1] << 8));
                            }

                            // short[] to double[]
                            for (int i = 0; i < shortsRead; i++) {
                                doubleData[i] = (double) shortBuffer[i] / Short.MAX_VALUE;
                            }

                            // FFT 변환
                            transformer.ft(doubleData);

                            // 주파수 변환
                            //String scale = whichScale2(doubleData);
                            int scale = whichScale2(doubleData);
                            playScale.add(scale);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 결과 출력
                                    //playScale.add(scale);
                                    Log.d("FFT Result", "Detected Scale: " + scale);
                                }
                            });
                        }
                    }

                } catch (IOException e){
                    Log.e("AnalyzeWavFile", "File reading failed", e);
                }
                Log.d("Result", "총 연주: " + playScale);
            }
        });
    }
    public int whichScale2(double[]... toTransform){

        if(toTransform[0][111]>99999){
        // 예외 위한 처리
        }
        // 4옥타브
        // all 50
        else if(toTransform[0][349]>60 || toTransform[0][348]>60 || toTransform[0][347]>60 ||
                toTransform[0][346]>60 || toTransform[0][350]>60 || toTransform[0][351]>60 ){
            scale2 = 9; //"F4";
        }
        // 5옥타브
        else if(toTransform[0][523]>75  ||toTransform[0][524]>75  || toTransform[0][521]>75  ){
            scale2 = 13; //"C5";
        }
        // 55
        else if(toTransform[0][259]>180 ||toTransform[0][260]>180){
            scale2 = 6; // "C4";
        }
        // all 55
        else if(toTransform[0][391]>60  || toTransform[0][390]>60  || toTransform[0][389]>60  ||
                toTransform[0][392]>60  || toTransform[0][393]>60  || toTransform[0][394]>60   ){
            scale2 = 10; //"G4";
        }
        // 30, 30, 55, 30, 55, 55
        else if(toTransform[0][440]>40    || toTransform[0][441]>40    || toTransform[0][442]>65  ||
                toTransform[0][439]>40    || toTransform[0][438]>40    || toTransform[0][437]>65 ){
            scale2 = 11; //"A4";
        }
        else if(toTransform[0][493]>80 ||toTransform[0][494]>80 || toTransform[0][495]>80 ||
                toTransform[0][496]>80  ){
            scale2 = 12; //"B4";
        }
        else if(toTransform[0][587]>64  ||toTransform[0][588]>64  || toTransform[0][589]>64  ){
            scale2 = 14; //"D5";
        }
        // 15, 30, 20, 30, 30
        else if(toTransform[0][293]>75 || toTransform[0][292]>75 || toTransform[0][294]>75 ||
                toTransform[0][295]>75 || toTransform[0][296]>75 ){
            scale2 = 7; //"D4";
        }
        // 15, 20, 20, 20, 15, 28 -> 30
        else if(toTransform[0][660]>140 ||toTransform[0][659]>140 ||
                toTransform[0][663]>140 ||toTransform[0][658]>140){
            scale2 = 15; //"E5";
        }
        // 50
        else if(toTransform[0][329]>90 ||toTransform[0][328]>90 || toTransform[0][330]>90  ){
            scale2 = 8; //"E4";
        }
        // 안써서 주석
        /*
        else if(toTransform[0][697]>60 ||toTransform[0][698]>60 ||  toTransform[0][699]>60 || toTransform[0][700]>60  ){
            scale2 = "F5";
        }
        else if(toTransform[0][783]>55 ||toTransform[0][784]>55 ){
            scale2 = "G5";
        }
        else if(toTransform[0][880]>60 ||toTransform[0][881]>60 || toTransform[0][882]>60 ){
            scale2 = "A5";
        }
        else if(toTransform[0][987]>33 ||toTransform[0][988]>33 || toTransform[0][989]>33 ){
            scale2 = "B5";
        }
         */
        //3옥타브
        else if(toTransform[0][129]>18 ||toTransform[0][130]>18){
            scale2 = 6;//"C4";
        }

        else if(toTransform[0][145]>18 ||toTransform[0][144]>18 ||toTransform[0][146]>18 ){
            scale2 = 7;//"D4";
        }
        else if(toTransform[0][164]>18 ||toTransform[0][163]>18 ||toTransform[0][165]>18 ){
            scale2 = 8;//"E4";
        }
        /*
        else if(toTransform[0][174]>18 ||toTransform[0][173]>18 ||toTransform[0][175]>18 ){
            scale2 = "F3";
        }
        else if(toTransform[0][195]>18 ||toTransform[0][196]>18 ||toTransform[0][194]>18 ){
            scale2 = "G3";
        }
        else if(toTransform[0][220]>18 ||toTransform[0][221]>18 ||toTransform[0][119]>18 ){
            scale2 = "A3";
        } else if(toTransform[0][246]>18 ||toTransform[0][245]>18 ||toTransform[0][247]>18 ){
            scale2 = "B3";
        }

         */
        else{    }
        return scale2;
    }

    public void MatchScale(List<int[]> beatPitch, ArrayList<Integer> playScale) {
        playScale.removeAll(Arrays.asList(Integer.valueOf(0)));
        int i = 0;
        int s = 0;
        int currentBeatPitch = 0;
        // beatPitch 배열이 끝날 때 까지
        for (int[] list : beatPitch) {
            // 악보 객체 박자에 따라 구분
            switch(list[0]) {
                case 8:
                    Log.d("beat", "박자: " + list[0]);
                    // 8분음표 = 반박이므로 1개
                    s = i;  i += 1;
                    while(s <= i) {
                        // 박자만큼 매치했는데도 일치하지 않으면 체크하고 나오기
                        if(s == i) {
                            Log.d("Match", "X. pitch: " + list[1] + ", 내 연주 : " + playScale.get(s-1));
                            check.add(currentBeatPitch);
                            break;
                        }
                        // 음 일치하면 체크하지 않고 나오기
                        if(playScale.get(s) == list[1]) {
                            Log.d("Match", "O. pitch: " + list[1]);
                            break;
                        } else {    s++;    }
                    }
                    currentBeatPitch++;
                    break;
                case 4:
                    Log.d("beat", "박자: " + list[0]);
                    // 4분음표 = 1박이므로 2개
                    s = i;  i += 2;
                    while(s <= i) {
                        // 박자만큼 매치했는데도 일치하지 않으면 체크하고 나오기
                        if(s == i) {
                            Log.d("Match", "X. pitch: " + list[1] + ", 내 연주 : " + playScale.get(s-1));
                            check.add(currentBeatPitch);
                            break;
                        }
                        // 음 일치하면 체크하지 않고 나오기
                        if(playScale.get(s) == list[1]) {
                            Log.d("Match", "O. pitch: " + list[1]);
                            break;
                        } else {    s++;    }
                    }
                    currentBeatPitch++;
                    break;
                case 2:
                    Log.d("beat", "박자: " + list[0]);
                    // 2분음표 = 2박이므로 4개
                    s = i;  i += 4;
                    while(s <= i) {
                        // 박자만큼 매치했는데도 일치하지 않으면 체크하고 나오기
                        if(s == i) {
                            Log.d("Match", "X. pitch: " + list[1] + ", 내 연주 : " + playScale.get(s-1));
                            check.add(currentBeatPitch);
                            break;
                        }
                        // 음 일치하면 체크하지 않고 나오기
                        if(playScale.get(s) == list[1]) {
                            Log.d("Match", "O. pitch: " + list[1]);
                            break;
                        } else {    s++;    }
                    }
                    currentBeatPitch++;
                    break;
                case -2:
                    Log.d("beat", "박자: " + list[0]);
                    // 점2분이면 3박이지만 마디 하나를 채우므로 4박으로 계산 = 8개
                    s = i;  i += 8;
                    while(s <= i) {
                        // 박자만큼 매치했는데도 일치하지 않으면 체크하고 나오기
                        if(s == i) {
                            Log.d("Match", "X. pitch: " + list[1] + ", 내 연주 : " + playScale.get(s-1));
                            check.add(currentBeatPitch);
                            break;
                        }
                        // 음 일치하면 체크하지 않고 나오기
                        if(playScale.get(s) == list[1]) {
                            Log.d("Match", "O. pitch: " + list[1]);
                            break;
                        } else {    s++;    }
                    }
                    currentBeatPitch++;
                    break;
                default : break;
            }
        }
    }

}