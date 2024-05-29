package com.example.lento;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class PlayActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final String LOG_TAG = "AudioRecordTest";

    private ImageView iconPlay;
    private ImageView iconPause;
    private ImageView iconBack;
    private boolean isRecording = false;
    private AudioRecord audioRecord;
    private Thread recordingThread; // 녹음을 위한 스레드 추가
    private String outputFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        // 아이콘 및 이미지뷰 초기화
        iconBack = findViewById(R.id.icon_back);
        iconPlay = findViewById(R.id.icon_play);
        iconPause = findViewById(R.id.icon_pause);

        // 녹음을 위한 권한 요청
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);

        // 뒤로 가기 버튼 클릭 리스너 설정
        iconBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // 재생/정지 버튼 클릭 리스너 설정
        iconPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleRecording();
            }
        });

        // 정지 버튼 클릭 리스너 설정
        iconPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
            }
        });

        // 녹음 파일 경로 설정 (수정된 부분)
        outputFile = getExternalFilesDir(null).getAbsolutePath() + "/" + UUID.randomUUID().toString() + ".pcm"; // 외부 캐시 대신 외부 파일 디렉토리 사용
    }

    private void toggleRecording() {
        if (!isRecording) {
            startRecording();
        } else {
            pauseRecording(); // 녹음 일시중지
        }
    }

    private void startRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
            return;
        }

        int bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(LOG_TAG, "AudioRecord initialization failed.");
            return;
        }

        audioRecord.startRecording();
        isRecording = true;

        // 녹음을 위한 스레드 시작 (추가된 부분)
        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeAudioDataToFile(); // 녹음 데이터를 파일에 씀
            }
        });
        recordingThread.start();
        // 파일 경로를 로그로 출력
        Log.i(LOG_TAG, "Recording to file: " + outputFile);

        iconPlay.setImageResource(R.drawable.stop);
        iconPause.setVisibility(View.VISIBLE);
        Toast.makeText(this, "연주를 시작하세요.", Toast.LENGTH_SHORT).show();
    }

    private void pauseRecording() {
        isRecording = false;
        if (audioRecord != null) {
            audioRecord.stop();
        }

        // 일시 중지 아이콘을 시작 아이콘으로 변경
        iconPlay.setImageResource(R.drawable.play);
    }

    private void stopRecording() {
        isRecording = false;
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        recordingThread = null; // 녹음 중지 시 스레드도 null로 설정

        // 녹음 중지 시 경고창 표시
        showStopWarning();
    }

    private void showStopWarning() {
        new AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage("연주를 마무리하지 않으면 정확도 판단이 불가합니다.")
                .setPositiveButton("멈추기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 녹음 중지 후 아이콘 상태 변경
                        iconPlay.setImageResource(R.drawable.play);
                        iconPause.setVisibility(View.GONE);
                        // 녹음 파일 저장 등 추가 작업
                        saveRecording();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    // 녹음 데이터를 파일에 쓰기 위한 메서드 (추가된 부분)
    private void writeAudioDataToFile() {
        int bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        byte[] buffer = new byte[bufferSize];
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(new File(outputFile));
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error creating output file", e);
            return;
        }

        while (isRecording) {
            int read = audioRecord.read(buffer, 0, buffer.length);
            if (read != AudioRecord.ERROR_INVALID_OPERATION && read != AudioRecord.ERROR_BAD_VALUE) {
                try {
                    os.write(buffer, 0, read);
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error writing to file", e);
                    return;
                }
            }
        }

        try {
            os.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing file", e);
        }
    }

    private void saveRecording() {
        // 녹음 파일 저장 및 추가 작업을 여기에 구현
        Toast.makeText(this, "연주가 녹음되었습니다.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Record Audio Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Record Audio Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}




