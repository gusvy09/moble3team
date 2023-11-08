package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

public class ShowVideoActivity extends AppCompatActivity {
    private VideoView videoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_video);

        // 정보를 받아오기
        String receivedData = getIntent().getStringExtra("key");

        // 받은 정보를 활용하여 작업 수행

        videoView = findViewById(R.id.cam1);

        String videoUrl = receivedData;

        Uri uri = Uri.parse(videoUrl);
        // MediaController를 사용하여 재생 컨트롤러 추가
        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);

        // VideoView에 Uri 설정
        videoView.setVideoURI(uri);

        // 영상 재생 시작
        videoView.start();
    }

    //뒤로가기
    public void back(View view) {
        if (videoView != null) {
            if (videoView.isPlaying()) {
                videoView.pause(); // 비디오 일시 중지
            }
            videoView.setMediaController(null); // 미디어 컨트롤러 숨기기
        }
        finish(); // 액티비티 종료
    }
}