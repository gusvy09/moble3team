package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
//import com.google.firebase.messaging.FirebaseMessaging;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MenuActivity extends AppCompatActivity {
    String Changepw;
    boolean correct;
    String id;
    AlertDialog.Builder builder;
    String[] colors;
    TextView user;
    private  Button ToggleBtn;
    private LinearLayout textViewContainer;
    private Animation slideDownAnimation;
    private Animation slideUpAnimation;
    private boolean isVisible;
    private ImageView setting; // 세팅이미지 버튼화하기위한 변수
    Switch sw ;
    boolean fire; // 화재감지모드
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        correct = false;
        isVisible = false;

        //세팅 이미지
        setting =  findViewById(R.id.setting);
        // 버튼
        ToggleBtn = findViewById(R.id.recstreaming);
        // 슬라이드에서 나오는 텍스트 컨테이너
        textViewContainer = findViewById(R.id.viewtext);
        user = findViewById(R.id.user);

        // Toggle Switch
        sw = (Switch) findViewById(R.id.firemode);
        // 스위치 이벤트 처리 중지
        sw.setOnCheckedChangeListener(null);
        new HttpTask(){
            @Override
            protected void onPostExecute(String result){
                if (result != null){
                    if(result.equals("True")){
                        sw.setChecked(true);
                    } else if (result.equals("False")) {
                        sw.setChecked(false);
                    }
                    sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked){
                                //new HttpTask().execute(""); //로컬 test
                                new HttpTask().execute(""); //서버
                                System.out.println("감지모드 on");                }
                            else{
                                //new HttpTask().execute(""); //로컬 test
                                new HttpTask().execute(""); //서버
                                System.out.println("감지모드 off");
                            }
                        }
                    });
                }
                else{
                    System.out.println("실패");
                }
            }
        }//.execute("http://"); // 로컬 test
                .execute("http://"); //서버




        setting.setOnClickListener(v->show_alert(v));
        
        slideDownAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        user.setText(intent.getStringExtra("id") + "님");

        Intent serviceIntent = new Intent(this, MyBackgroundService.class);
        startService(serviceIntent);

        System.out.println("채널 : " + getString(R.string.default_notification_channel_id));

        ToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isVisible) {
                    // 위로 슬라이드하여 숨기기
                    textViewContainer.startAnimation(slideUpAnimation);
                    textViewContainer.setVisibility(View.GONE);
                    System.out.println("숨기기");
                } else {
                    // 아래로 슬라이드하여 나타내기
                    textViewContainer.setVisibility(View.VISIBLE);
                    textViewContainer.startAnimation(slideDownAnimation);
                    System.out.println("보이기");
                }
                // 상태 변경
                isVisible = !isVisible;
            }
        });
    }
    public void movepage_live(View view){
        Intent intent = new Intent(this, LiveActivity.class);
        startActivity(intent);
    }
    public void rec_normal(View view){
        Intent intent = new Intent(this, recnormalActivity.class);
        startActivity(intent);
    }
    public void rec_event(View view){
        Intent intent = new Intent(this, RecEventActivity.class);
        startActivity(intent);
    }
    public void show_alert(View view){
        colors = getResources().getStringArray(R.array.setting);
        builder = new AlertDialog.Builder(MenuActivity.this);

        builder.setTitle("설정");

        builder.setItems(colors, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                switch (which){
                    case 0: // 비밀번호 변경
                        pwchange();
                        break;
                    case 1: // 로그아웃
                        new Menu_DatabaseConnectTask().execute();
                        break;
                    default:
                        System.out.println("완료");
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    // 백그라운드에서 db연결 (백그라운드에서 하지 않으면 오류) -> 로그아웃뒤 token, login여부 초기화를 위해 db접근
    // 토큰, 로그인 여부를 위한 백그라운드
    private class Menu_DatabaseConnectTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            // 데이터베이스 연결 작업을 이곳에 수행합니다.
            correct = read_db(id,1);
            return correct;
        }
        // db연결 뒤 실행 (로그인)
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(correct){
                logout();
            }
        }
    }
    // 비밀번호 변경을 위한 백그라운드
    private class Menu_DatabaseConnectTask2 extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            // 데이터베이스 연결 작업을 이곳에 수행합니다.
            correct = read_db(id,2);
            System.out.println("성공");
            System.out.println(correct);
            return correct;
        }
        // db연결 뒤 실행 (로그아웃)
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(correct){
                System.out.println("들어오는지?");
                AlertDialog.Builder dlg = new AlertDialog.Builder(MenuActivity.this);
                dlg.setTitle("변경완료"); // 제목
                dlg.setMessage("로그인을 다시 해주세요");//내용
                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    // 변경 완료 후 확인버튼 (업데이트 끝남)
                    public void onClick(DialogInterface dialog, int which) {
                        logout();
                    }
                });
                dlg.show();

            }
        }
    }
    //
    public boolean read_db(String user_id, int num) {
        //자바에서 지원하는 데이터베이스와 자바의 연결 클래스: Connection 객체
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        // 유저 정보
        String user_num;
        String userId;
        String userPw = null;

        //연결 드라이버 메니저가 연결해준 연결 인스턴스를 con 변수에 저장
        // db관련 url, id, pw, driver 정의
        String url ="";
        String id ="";
        String pw ="";
        String driver = "";

        //con 엔 파라미터로 url(data source source 마우스우 프로퍼티로 가서 커넥션 유알엘 확인)
        try {
            Class.forName(driver); // 에드케치 2번쩨걸로함.
            // 정의해둔 변수들 이용하여 db연결
            con = DriverManager.getConnection(url,id,pw);
            System.out.println("연결성공");
            // sql문 table이름 대문자로 해야됨
            // 입력한 userid를 이용하여 정보 검색
            if(num ==1 ){
                String sqlQuery = String.format("UPDATE USER SET login = '' where user_id = '%s'",user_id);
                String sqlQuery2 = String.format("UPDATE USER SET token = '' where user_id = '%s'",user_id);
                stmt = con.createStatement();
                // 쿼리 실행 및 영향 받은 행 수 반환
                int rowsAffected = stmt.executeUpdate(sqlQuery);
                int rowsAffected2 = stmt.executeUpdate(sqlQuery2);
                if (rowsAffected > 0 && rowsAffected2 > 0) {
                    System.out.println("업데이트 성공");
                    //con.commit(); // 커밋
                } else {
                    System.out.println("업데이트 실패");
                }
                // 실행 끝나면 닫음
                stmt.close();
                con.close();
                System.out.println("데이터베이스가 잘 종료되었다.");
                return true;
            } else if (num == 2) {
                String sqlQuery = String.format("UPDATE USER SET user_pw = '%s' where user_id = '%s'",Changepw,user_id);
                stmt = con.createStatement();
                int rowsAffected = stmt.executeUpdate(sqlQuery);
                if (rowsAffected > 0) {
                    System.out.println("업데이트 성공");
                } else {
                    System.out.println("업데이트 실패");
                }
                // 실행 끝나면 닫음
                stmt.close();
                con.close();
                System.out.println("데이터베이스가 잘 종료되었다.");
                return true;
            }
            // 연결 실패
        }  catch (ClassNotFoundException e) {
            System.out.println("DB연결 실패 무언가 틀렸다..  드라이버 클래스 파일 오류");
            e.printStackTrace();
        }catch (SQLException e) {
            System.out.println("DB연결 실패 무언가 틀렸다.. 드라이버 연결 정보 오류");
            e.printStackTrace();
        }catch (Exception e) {
            System.out.println("별도의 사유로 연결 실패");
            e.printStackTrace();
        }
        // 만약 연결이 안되게되었다면 열려있는 con객체를 닫아야한다.
        if(con!= null)
            try {
                con.close();
                System.out.println("데이터베이스가 잘 종료되었다.");
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        return false;
    }
    public void logout(){
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("id","");
        editor.putString("pw","");
        editor.putBoolean("remember", false);
        editor.apply();
        finish();
        Intent newActivityIntent = new Intent(MenuActivity.this,LoginActivity.class);
        startActivity(newActivityIntent); // 새로운 액티비티를 시작한다.
    }
    public void pwchange(){

        AlertDialog.Builder dlg = new AlertDialog.Builder(MenuActivity.this);
        dlg.setTitle("비밀번호 변경"); // 제목

        // 다이얼로그에 사용될 커스텀 레이아웃을 불러옵니다.
        View dialogView = getLayoutInflater().inflate(R.layout.custom_dialog, null);
        dlg.setView(dialogView);

        // 커스텀 레이아웃에서 EditText를 찾습니다.
        EditText editText = dialogView.findViewById(R.id.editText);

        dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            //확인버튼
            public void onClick(DialogInterface dialog, int which) {
                //EditText에서 입력한 텍스트를 추출합니다.
                Changepw = editText.getText().toString();
                // 입력한 텍스트를 사용하여 원하는 동작 수행
                //Toast.makeText(MenuActivity.this, "입력한 텍스트: " + userInput, Toast.LENGTH_SHORT).show();
                // db접근 후 비밀번호 업데이트
                new Menu_DatabaseConnectTask2().execute();
            }
        });
        dlg.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // 사용자가 취소 버튼을 클릭했을 때 수행할 동작을 여기에 추가

            }
        });
        dlg.show();
    }
}
