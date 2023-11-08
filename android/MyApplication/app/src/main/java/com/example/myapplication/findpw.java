package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class findpw extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findpw);
        final Button complete = (Button) findViewById(R.id.complete);
        complete.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                AlertDialog.Builder dlg = new AlertDialog.Builder(findpw.this);
                dlg.setTitle("계발에서 개발까지"); //제목
                dlg.setMessage("안녕하세요 계발에서 개발까지 입니다."); // 메시지
//                dlg.setIcon(R.drawable.deum); // 아이콘 설정
//                버튼 클릭시 동작
                dlg.setPositiveButton("확인",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which) {
                        //토스트 메시지
                        //Toast.makeText(findpw.this,"확인을 눌르셨습니다.",Toast.LENGTH_SHORT).show();
                    }
                });
                dlg.show();
            }
        });
    }
    public void back(View view) {
        finish();
    }
}