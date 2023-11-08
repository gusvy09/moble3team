package com.example.myapplication;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.AsyncTask;
import android.widget.Toast;

import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;


public class AddMember extends AppCompatActivity {
    // 유저 아이디, 비밀번호, 이름, 핸드폰번호
    String identify_num;
    String user_id; 
    String user_pw;
    String user_name;
    String user_phone;
    boolean bmailuse;
    private boolean bMobile; // 모바일인증 여부
    private boolean biduse; // 아이디 존재 여부
    private boolean bnext; // 회원가입 가능 여부
    private Button checkid; //아이디 중복확인 버튼
    private ImageView back; // 뒤로가기 버튼
    private Button identify; // 본인인증 버튼
    private Button complete; // 확인 버튼
    private Button identi_com; // 인증확인 버튼


    private View myLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);

        
        bMobile = false; // 모바일 인증 여부
        biduse = false; // 아이디 중복체크
        bmailuse = false; // 이메일 중복
        bnext = false; // 회원가입 가능 여부
        // 초기값
        user_id = ""; 
        user_pw = ""; 
        user_name = ""; 
        user_phone = "";

        // 중복확인 버튼
        checkid = findViewById(R.id.checkid);
        // 뒤로가기
        back = findViewById(R.id.back);
        // 본인인증
        identify = findViewById(R.id.identify);
        // 확인버튼
        complete = findViewById(R.id.complete);
        // 인증확인 버튼 (처음엔 숨겨져있음)
        identi_com = findViewById(R.id.identi_com);

        // 중복확인 이벤트
        checkid.setOnClickListener(v -> {
            // 중복확인
            // 연결을 시도할 AsyncTask를 실행합니다.
            new DatabaseConnectTask().execute();
        });
        //뒤로가기
        back.setOnClickListener(v -> back(v));
        // 본인인증
        identify.setOnClickListener(v->identify());
        // 회원가입
        complete.setOnClickListener(v->{
            // 회원가입
            // 연결을 시도할 AsyncTask를 실행합니다.
            new DatabaseConnectTask2().execute();
        });
        identi_com.setOnClickListener(v->identify_com());
    }

    // AsyncTask 클래스를 정의하여 데이터베이스 연결 작업을 수행합니다.
    // 중복확인 백그라운드
    private class DatabaseConnectTask extends AsyncTask<Void, Void, Boolean> {
        
        //백그라운드에서 db연결 메서드 실행
        @Override
        protected Boolean doInBackground(Void... voids) {
            // 데이터베이스 연결 작업을 이곳에 수행합니다.
            biduse = read_db(getid(),"비밀번호입력이안됐음","checkid");
            // id가 사용중인경우 true, 아닌 경우 false를 return
            return biduse;
        }
        //
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            // 아이디 사용중인경우 true, 아닌경우 false를 리턴받음
            if(!biduse){ // 사용할 수 있는 아이디를 입력했을때 실행됨
                Toast.makeText(AddMember.this,"사용할 수 있는 아이디 입니다",Toast.LENGTH_SHORT).show();
            }
            else{ // 이미 사용중인 아이디를 입력했을때 실행됨
                Toast.makeText(AddMember.this,"이미 사용하고 있는 아이디 입니다",Toast.LENGTH_SHORT).show();
            }
        }
    }
    // 회원가입 백그라운드
    private class DatabaseConnectTask2 extends AsyncTask<Void, Void, Boolean> {

        //백그라운드에서 db연결 메서드 실행
        @Override
        protected Boolean doInBackground(Void... voids) {
            // 데이터베이스 연결 작업을 이곳에 수행합니다.
            bnext =  read_db(getid(),getpw(),"add");
            return bnext;
        }
        //
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            // 회원가입이 완료 된 경우
            // 회원가입 메세지 띄우고 로그인페이지로 나가기
            if (bnext){
                String title;
                String Message;
                AlertDialog.Builder dlg = new AlertDialog.Builder(AddMember.this);
                title = "완료";
                Message = "회원가입이 완료되었습니다";
                dlg.setTitle(title); //제목
                dlg.setMessage(Message); // 메시지
//                dlg.setIcon(R.drawable.deum); // 아이콘 설정
//                버튼 클릭시 동작
                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //토스트 메시지
                        //Toast.makeText(findpw.this,"확인을 눌르셨습니다.",Toast.LENGTH_SHORT).show();
                        // 확인 버튼 눌리면 -> 현재 페이지 닫기
                        finish();
                    }
                });
                dlg.show();
            }
            // 회원가입이 안 된경우
            // 아이디 공백, 비밀번호 공백, 비밀번호, 확인 다를경우
            // 모바일 인증 안 한경우 (기본값이 false, not을 붙여주어야 인증을 안 했을때 true로 바뀌어 if문에 걸리게 됨
            else {
                if(getid().equals("") || getpw().equals("") || get_phone().equals("") || get_name().equals("")){
                    Toast.makeText(AddMember.this,"비어있는 항목이 있습니다",Toast.LENGTH_SHORT).show();
                } else if(!bMobile) {
                    Toast.makeText(AddMember.this,"모바일 인증을 완료해주세요",Toast.LENGTH_SHORT).show();
                } else if (!user_pw.equals(check_getpw())) {
                    Toast.makeText(AddMember.this,"비밀번호를 다시 확인해주세요",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    // 이메일 중복 체크
    private class DatabaseConnectTask3 extends AsyncTask<Void, Void, Boolean> {

        //백그라운드에서 db연결 메서드 실행
        @Override
        protected Boolean doInBackground(Void... voids) {
            // 데이터베이스 연결 작업을 이곳에 수행합니다.
            bmailuse = read_db(getid(),"비밀번호입력이안됐음","checkemail");
            // id가 사용중인경우 true, 아닌 경우 false를 return
            System.out.println(biduse);
            return bmailuse;
        }
        //
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            // 아이디 사용중인경우 true, 아닌경우 false를 리턴받음
            if(!bmailuse){ // 사용할 수 있는 아이디를 입력했을때 실행됨
                myLinearLayout.setVisibility(View.VISIBLE);
                identify_num = mkinum();
                add_sms(get_phone(),identify_num);
                System.out.println(identify_num);
            }
            else{ // 이미 사용중인 아이디를 입력했을때 실행됨
                Toast.makeText(AddMember.this,"이미 사용하고 있는 이메일 입니다",Toast.LENGTH_SHORT).show();
            }
        }
    }

    // db연결 메서드
    public boolean read_db(String userid,String userpw,String check) {
        boolean bcheck = false;
        //자바에서 지원하는 데이터베이스와 자바의 연결 클래스: Connection 객체
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        //연결 드라이버 메니저가 연결해준 연결 인스턴스를 con 변수에 저장
        // db관련 url, id, pw, driver 정의
        String url ="";
        String id ="";
        String pw ="";
        String driver = "";

        // 중복확인으로 들어왔을때 실행됨, 아이디 중복확인을 해야함
        if (userpw.equals("비밀번호입력이안됐음"))
        {
            //con 엔 파라미터로 url(data source source 마우스우 프로퍼티로 가서 커넥션 유알엘 확인)
            try {
                Class.forName(driver); // 에드케치 2번쩨걸로함.
                // 정의해둔 변수들 이용하여 db연결
                con = DriverManager.getConnection(url,id,pw);
                System.out.println("중복확인 연결성공");

                // sql문 table이름 대문자로 해야됨
                String sqlQuery = "SELECT * from USER";
                stmt = con.createStatement();
                // 실행
                rs = stmt.executeQuery(sqlQuery);
                if (check.equals("checkid")) {
                    // 결과 처리 column이름으로 해야됨
                    while (rs.next()) {
                        String dbid = rs.getString("user_id");
                        if (userid.equals(dbid)) {
                            bcheck = true;
                            break;
                        }
                    }
                }
                else {
                    // 결과 처리 column이름으로 해야됨
                    while (rs.next()) {
                        String dbid = rs.getString("user_mail");
                        if (get_phone().equals(dbid)) {
                            bcheck = true;
                            break;
                        }
                    }
                }
                // 실행 끝나면 닫음
                con.close();
                System.out.println("데이터베이스가 잘 종료되었다.");
                return bcheck;
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
        }
        // 확인(회원가입)으로 들어왔을때 실행됨, 데이터베이스에 값을 넣어야함
        else{
            // 회원 가입 안되는 모든 경우의수 넣기
            // 아이디 공백, 비밀번호 공백, 비밀번호, 확인 다를경우
            // 모바일 인증 안 한경우
            if(!userpw.equals(check_getpw()) || getid().equals("") || getpw().equals("") || !bMobile)
                // not bMobile로 해야 인증을 했을때 true->false로 들어와서 통과가능
                // false가 기본값 인증 안 했을경우 true로 바뀌어서 if문에 걸리게됨
                {
                    return false;
                }
            // 비밀번호, 비밀번호 확인이 같지 않을 경우
            // 이름, 전화번호 값 가져오기
            user_name = get_name();
            user_phone = get_phone();
            //con 엔 파라미터로 url(data source source 마우스우 프로퍼티로 가서 커넥션 유알엘 확인)
            try {
                Class.forName(driver); // 에드케치 2번쩨걸로함.
                // 정의해둔 변수들 이용하여 db연결
                con = DriverManager.getConnection(url,id,pw);
                con.setAutoCommit(false); // 수동 커밋 활성화 (완료되면 커밋해주어야함)
                System.out.println("커밋모드 수동으로 변경완료.");
                System.out.println("회원가입 연결성공");
                // sql문 table이름 대문자로 해야됨
                String sqlQuery = String.format("INSERT INTO USER (user_id, user_pw, user_name, user_mail) VALUES ('%s','%s','%s','%s')",userid,userpw,user_name,user_phone);
                stmt = con.createStatement();
                // 실행
                int num = stmt.executeUpdate(sqlQuery);
                
                if (num>0){
                    System.out.println("추가완료");
                    con.commit();
                    bnext = true;
                }
                else{
                    System.out.println("실패");
                    con.rollback();
                }
                con.setAutoCommit(true); // 다시 자동 커밋 모드로 변경
                System.out.println("커밋모드 자동으로 변경완료.");
                // 실행 끝나면 닫음
                con.close();
                System.out.println("데이터베이스가 잘 종료되었다.");
                return bnext;
                // 연결 실패
            }  catch (ClassNotFoundException e) {
                System.out.println("DB연결 실패 무언가 틀렸다..  드라이버 클래스 파일 오류");
                e.printStackTrace();
            }catch (SQLException e) {
                System.out.println("DB연결 실패 무언가 틀렸다.. 드라이버 연결 정보 오류" + e);
                e.printStackTrace();
            }catch (Exception e) {
                System.out.println("별도의 사유로 연결 실패");
                e.printStackTrace();
            }

        }
        // 만약 연결이 안되게되었다면 열려있는 con객체를 닫아야한다.
        if(con!= null)
            try {
                con.close();
                System.out.println("데이터베이스가 잘 종료되었다.(맨 마지막)");
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        return false;
}


    // 뒤로가기
    public void back(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    // 본인인증 -> 이름, 전화번호가 없으면 toast메세지 띄움.
    // 둘 다 적혀있으면 메세지 발송 후
    public void identify(){
        myLinearLayout = findViewById(R.id.invisible);
        // 이름과 전화번호 둘 중 하나라도 공백일 경우
        if(get_name().equals("") || get_phone().equals(""))
            {
                Toast.makeText(AddMember.this,"이름과 이메일을 입력해주세요",Toast.LENGTH_SHORT).show();
            }
        else { // 둘 다 적혀있을 경우 인증번호 적는 창이 나타남
            // 입력된 전화번호에 메세지 보내는 함수 추가 해야됨
            new DatabaseConnectTask3().execute();

        }
    }
    // 본인인증 완료 (10.13기준 전화번호 중복 가능)
    // 완료되면 bMobile = true
    public void identify_com(){
        if(get_inum().equals(identify_num)){
            bMobile = true;
            Toast.makeText(AddMember.this,"인증이 완료되었습니다",Toast.LENGTH_SHORT).show();
            System.out.println("인증 완료");
        }
        else{
            Toast.makeText(AddMember.this,"인증번호를 확인해주세요",Toast.LENGTH_SHORT).show();
            System.out.println("인증 안 됨");
        }
    }
    // edittext에 있는 아이디 비밀번호 반환
    // 비어있을 경우 공백이 반환됨 return ""
    public String getid(){
        EditText euser_id = (EditText)findViewById(R.id.editid);
        user_id = String.valueOf(euser_id.getText());
        return user_id;
    }
    public String getpw(){
        EditText euser_pw = (EditText)findViewById(R.id.editpw);
        user_pw = String.valueOf(euser_pw.getText());
        return user_pw;
    }
    // 비밀번호 확인란 리턴
    public String check_getpw(){
        EditText euser_pw = (EditText)findViewById(R.id.editpw2);
        return String.valueOf(euser_pw.getText());
    }
    // 이름 리턴
    public String get_name(){
        EditText euser_name = (EditText)findViewById(R.id.editname);
        return String.valueOf(euser_name.getText());
    }
    // 핸드폰 번호 리턴 -> 이메일로 전환됐음 (10.24), 함수 이름은 그대로 씀
    public String get_phone(){
        EditText euser_phone = (EditText)findViewById(R.id.editphone);
        return String.valueOf(euser_phone.getText());
    }
    // 인증번호 리턴
    public String get_inum(){
        EditText euser_inum = (EditText)findViewById(R.id.identi_num);
        return String.valueOf(euser_inum.getText());
    }
    // 난수발생 4자리 리턴
    public String mkinum(){
        String inum = "";
        Random rand = new Random();

        for(int i = 0;i<4;i++){
            String ran = Integer.toString(rand.nextInt(10));
            inum += ran;
        }
        return inum;
    }
    public void add_sms(String mail,String num){
        new MailSender().sendEmail(mail,num);
    }
}


