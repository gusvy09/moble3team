package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.VideoView;

public class LiveActivity extends AppCompatActivity {
    private WebView Cam1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);
        Cam1  = findViewById(R.id.cam1);

        WebSettings webSettings = Cam1.getSettings();
        webSettings.setJavaScriptEnabled(true);
        Cam1.getSettings().setMediaPlaybackRequiresUserGesture(false); // 사용자 제스처 필요 여부 설정

        Cam1.setWebViewClient(new WebViewClient());

        //Cam1.loadUrl(""); //로컬
        Cam1.loadUrl(""); // server
    }
    public void back(View view) {
        finish();
    }
}