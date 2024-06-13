package com.example.lento;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

import java.io.ByteArrayOutputStream;
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
    private ByteArrayOutputStream pcmBuffer; // PCM 데이터를 저장할 버퍼

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

        // 녹음 파일 경로 설정
        outputFile = getExternalFilesDir(null).getAbsolutePath() + "/" + UUID.randomUUID().toString() + ".wav";
        //outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + UUID.randomUUID().toString() + ".wav";
        //outputFile = getExternalFilesDir(null).getAbsolutePath() + "/" + UUID.randomUUID().toString() + ".wav";
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

        pcmBuffer = new ByteArrayOutputStream(); // PCM 버퍼 초기화
        audioRecord.startRecording();
        isRecording = true;

        // 녹음을 위한 스레드 시작
        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeAudioDataToBuffer();
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
                .setMessage("연주를 마무리 하시겠습니까?")
                .setPositiveButton("저장", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 녹음 중지 후 아이콘 상태 변경
                        iconPlay.setImageResource(R.drawable.play);
                        iconPause.setVisibility(View.GONE);
                        // 녹음 파일 저장 등 추가 작업
                        saveRecording();

                        // 화면 이동
                        Intent intent = new Intent(PlayActivity.this, PerformAccuracyActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    // PCM 데이터를 버퍼에 쓰기 위한 메서드
    private void writeAudioDataToBuffer() {
        int bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        byte[] buffer = new byte[bufferSize];

        while (isRecording) {
            int read = audioRecord.read(buffer, 0, buffer.length);
            if (read != AudioRecord.ERROR_INVALID_OPERATION && read != AudioRecord.ERROR_BAD_VALUE) {
                pcmBuffer.write(buffer, 0, read);
            }
        }
    }

    private void saveRecording() {
        if (pcmBuffer == null) {
            Log.e(LOG_TAG, "PCM Buffer is null. Cannot save recording.");
            return;
        }

        // PCM 데이터를 WAV 파일로 저장
        try {
            byte[] pcmData = pcmBuffer.toByteArray();
            FileOutputStream out = new FileOutputStream(outputFile);
            writeWavHeader(out, pcmData.length);
            out.write(pcmData);
            out.close();
            Toast.makeText(this, "녹음이 저장되었습니다: " + outputFile, Toast.LENGTH_LONG).show();
            Log.i(LOG_TAG, "녹음이 저장되었습니다: " + outputFile); // 로그 출력
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error saving WAV file", e);
        }
    }

    private void writeWavHeader(FileOutputStream out, int pcmDataSize) throws IOException {
        int totalDataLen = pcmDataSize + 36;
        int byteRate = 44100 * 2 * 1;

        byte[] header = new byte[44];
        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = 1;  // channels
        header[23] = 0;
        header[24] = (byte) (44100 & 0xff);
        header[25] = (byte) ((44100 >> 8) & 0xff);
        header[26] = (byte) ((44100 >> 16) & 0xff);
        header[27] = (byte) ((44100 >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 1);  // block align
        header[33] = 0;
        header[34] = 16;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (pcmDataSize & 0xff);
        header[41] = (byte) ((pcmDataSize >> 8) & 0xff);
        header[42] = (byte) ((pcmDataSize >> 16) & 0xff);
        header[43] = (byte) ((pcmDataSize >> 24) & 0xff);

        out.write(header, 0, 44);
    }
    // 권한 요청 결과 처리 메서드
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




