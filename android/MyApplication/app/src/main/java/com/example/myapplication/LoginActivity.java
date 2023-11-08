package com.example.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LoginActivity extends AppCompatActivity {
    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;

    private boolean isremember;
    private CheckBox remember;
    // 로그인 버튼
    private Button Login;
    // 입력된 아이디, 비밀번호, 해당 유저의 이름
    String Token;
    String user_id;
    String user_pw;
    String user_name = null;
    boolean correct = false;

    //화면 깨우기 위한 변수
    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;
    // 어플 실행될때 실행되는 코드.checkbox 여부에 따른 id,pw를 가져와야함
    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        //로그인버튼 찾아서 함수 더해줌
        Login = findViewById(R.id.Login);
        Login.setOnClickListener(v->login());

        //자동로그인 체크박스
        remember = findViewById(R.id.remember);
        // SharedPreferences 객체 초기화
        sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        // SharedPreferences.Editor 객체 생성
        editor = sharedPreferences.edit();
        remember.setChecked(sharedPreferences.getBoolean("remember",false));
        // 2. SharedPreferences에서 데이터 가져오기

        if (remember.isChecked()) {
            user_id = sharedPreferences.getString("id", "default_value");
            user_pw = sharedPreferences.getString("pw", "default_value");
            new login_DatabaseConnectTask().execute();
        }
        else{
            System.out.println("Saved Value: " +"없음");
        }

        // FCM 토큰을 가져오고 처리합니다.
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TAG", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        Token = token;
                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d("TAG", msg);
                        //Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                        System.out.println(msg);
                    }
                });
        Intent serviceIntent = new Intent(this, MyBackgroundService.class);
        startService(serviceIntent);
        Toast.makeText(LoginActivity.this, "자동로그인을 선택하셔야\n 알림을 받을 수 있습니다.", Toast.LENGTH_SHORT).show();
    }
    // 회원가입 텍스트 클릭시 화면 전환
    public void add_member(View view){
        System.out.println("회원가입 버튼");
        Intent intent = new Intent(this, AddMember.class);
        startActivity(intent);
    }
    // 아이디 찾기
    public void movepage_id(View view){
        System.out.println("id찾기 버튼");
        Intent intent = new Intent(this, FindidActivity.class);
        startActivity(intent);
    }
    //로그인 버튼 누를시
    public void login(){
        EditText euser_id = (EditText)findViewById(R.id.user_id);
        EditText euser_pw = (EditText)findViewById(R.id.user_pw);

        // 아이디 비밀번호 체크
        user_id = String.valueOf(euser_id.getText());
        user_pw = String.valueOf(euser_pw.getText());
        isremember = remember.isChecked();

        //아이디 혹은 비밀번호가 입력되지 않은 경우
        if (user_id.length() == 0 || user_pw.length() == 0){
            alert_login(correct, "hello");
        }
        //둘 다 입력이 된 경우
        else{
            // 둘 다 입력이 되고, 체크박스가 체크 되어있으면 일단 대입
            if(isremember)
            {
                editor.putString("id", user_id);
                editor.putString("pw", user_pw);
                editor.putBoolean("remember",true);
            }
            new login_DatabaseConnectTask().execute();
        }

        //remember = findViewById(R.id.remember);
        //System.out.println("로그인 버튼");
        //Intent intent = new Intent(this, MenuActivity.class);
        //startActivity(intent);
    }
    // 백그라운드에서 db연결 (백그라운드에서 하지 않으면 오류)
    private class login_DatabaseConnectTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            // 데이터베이스 연결 작업을 이곳에 수행합니다.
            correct = read_db(user_id,user_pw);
            return correct;
        }
        // db연결 뒤 실행 (로그인)
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            alert_login(correct,user_name);
        }
    }
    // db연결 메서드 (id,pw 확인을 위함)
    public boolean read_db(String user_id, String user_pw) {
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
            String sqlQuery = String.format("SELECT * from USER where user_id = '%s'",user_id);
            stmt = con.createStatement();
            // 실행
            rs = stmt.executeQuery(sqlQuery);

            // 결과 처리 column이름으로 해야됨
            // 입력한 userid를 이용하여 pw를 찾음
            while (rs.next()) {
                user_num = rs.getString("user_mail");
                user_name = rs.getString("user_name");
                userId = rs.getString("user_id");
                userPw = rs.getString("user_pw");
                System.out.println("usernum : " + user_num + " username : " + user_name + " userid : " + userId + " userpw : " + userPw);
                break;
                // 결과를 원하는 대로 처리 or 출력
            }

            System.out.println("userpw : " + user_pw);
            System.out.println("userpw : " + userPw);
            // 입력한 비밀번호와 데이터베이스의 비밀번호가 같으면 return true
            // 아니라면 return false
            if (user_pw.equals(userPw)) {
                sqlQuery = String.format("UPDATE USER SET login = 'on' where user_id = '%s'",user_id);
                String sqlQuery2 = String.format("UPDATE USER SET token = '%s' where user_id = '%s'",Token, user_id);
                stmt = con.createStatement();
                // 쿼리 실행 및 영향 받은 행 수 반환
                if (isremember){
                    int rowsAffected = stmt.executeUpdate(sqlQuery);
                    int rowsAffected2 = stmt.executeUpdate(sqlQuery2);
                    if (rowsAffected > 0 && rowsAffected2 > 0) {
                        System.out.println("업데이트 성공");
                        //con.commit(); // 커밋
                    } else {
                        System.out.println("업데이트 실패");
                    }
                }
                // 실행 끝나면 닫음
                con.close();
                System.out.println("데이터베이스가 잘 종료되었다.");
                return true;
            }
            else {
                // 실행 끝나면 닫음
                con.close();
                System.out.println("데이터베이스가 잘 종료되었다.");
                return false;
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
    
    public void alert_login(boolean status,String username) {
        String title;
        String Message;
        AlertDialog.Builder dlg = new AlertDialog.Builder(LoginActivity.this);
        //로그인 실패시 status -> false로 들어옴
        if (status){
            title = "로그인 성공";
            Message = String.format("안녕하세요 %s님",username);
        }
        else{
            title = "로그인 실패";
            Message = "아이디 혹은 비밀번호를 확인하세요";
        }
        dlg.setTitle(title); //제목
        dlg.setMessage(Message); // 메시지
//                dlg.setIcon(R.drawable.deum); // 아이콘 설정
//                버튼 클릭시 동작
        dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //토스트 메시지
                //Toast.makeText(findpw.this,"확인을 눌르셨습니다.",Toast.LENGTH_SHORT).show();
                // 로그인 성공시 페이지 이동 -> 초기 페이지 꺼야함.
                if (status){
                    // 값을 저장
                    editor.apply();
                    finish();
                    Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                    intent.putExtra("id",user_id);
                    startActivity(intent);
                }
            }
        });
        dlg.show();
    }
}