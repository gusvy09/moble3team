package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

public class FindidActivity extends AppCompatActivity {
    String idennum = "";
    int check = 0;
    // db에 담겨있는 유저 정보
    String db_pw = "";
    String db_id = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findid);
        // 인증확인 버튼
        final Button complete_pw = (Button) findViewById(R.id.complete_pw);
        final Button complete_id = (Button) findViewById(R.id.complete_id);
        // 인증요청 버튼
        final Button identify_pw = (Button) findViewById(R.id.identify_pw);
        final Button identify_id = (Button) findViewById(R.id.identify_id);

        // 아이디찾기 인증번호 보내기
        identify_id.setOnClickListener(v ->{
            check = 1;
            new find_DatabaseConnectTask().execute();
        });
        // 비밀번호찾기 인증번호 보내기
        identify_pw.setOnClickListener(v -> {
            check = 2;
            new find_DatabaseConnectTask().execute();
        });
        // 아이디찾기 확인 (인증번호 입력 후)
        complete_id.setOnClickListener(v ->complete_cus(1));
        // 비밀번호찾기 확인 (인증번호 입력 후)
        complete_pw.setOnClickListener(v ->complete_cus(2));
    }
    // 뒤로가기
    public void back(View view) {
        finish();
    }

    // 난수발생 4자리 리턴
    public String mkinum() {
        String inum = "";
        Random rand = new Random();
        for (int i = 0; i < 4; i++) {
            String ran = Integer.toString(rand.nextInt(10));
            inum += ran;
        }
        System.out.println(inum);
        return inum;
    }
    // 인증확인
    // 1 -> 아이디 인증번호 확인,  2-> 비밀번호 인증번호 확인
    public void complete_cus(int num){
        switch (num) {
            case 1: // 아이디 찾기 인증번호 눌렀을 경우
                EditText iden = (EditText) findViewById(R.id.editinumid);
                if (String.valueOf(iden.getText()).equals(idennum)){
                    //인증번호 맞은 경우
                    alert(db_id,check);
                }
                else{
                    ToastMsg("다시 확인해주세요");
                }
                break;
            case 2: // 비밀번호 찾기 인증번호 눌렀을 경우
                EditText idenpw = (EditText) findViewById(R.id.editinumpw);
                if (String.valueOf(idenpw.getText()).equals(idennum)){
                    //인증번호 맞은 경우
                    alert(db_pw,check);
                }
                else{
                    ToastMsg("다시 확인해주세요");
                }
                break;
        }
    }
    // 인증요청 버튼 눌릴시 아이디찾기, 비밀번호 찾기인지 구분 뒤
    // 인증번호 발송
    // 아이디가 db에 없는경우 toast메세지.
    // 아이디 찾은 경우 다이어로그 알람창으로 아이디 출력.
    // 비밀번호 수정?, 랜덤 10글자?
    // 백그라운드에서 실행 (db접근하는 함수)
    private class find_DatabaseConnectTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            // 데이터베이스 연결 작업을 이곳에 수행합니다.
            // 아이디 찾기
            if(check == 1){
                EditText ename = (EditText) findViewById(R.id.editnameid);
                EditText ephone = (EditText) findViewById(R.id.editphoneid);
                // 아이디 찾을때, 이름과 휴대폰 번호 둘 중 하나라도 비어있을 경우
                // 토스트 메세지로 비어있다고 알림
                if (String.valueOf(ename.getText()).equals("") || String.valueOf(ephone.getText()).equals("")){
                    return false;
                }
                // 둘 다 기재한 경우, db에 접근하여 이름과 휴대폰 번호로 아이디를 찾아야함
                else{
                    return read_db(String.valueOf(ename.getText()),String.valueOf(ephone.getText()),"",check);
                }

            } //비밀번호 찾기
            else{
                EditText ename = (EditText) findViewById(R.id.editnamepw);
                EditText ephone = (EditText) findViewById(R.id.editphonepw);
                EditText eid = (EditText) findViewById(R.id.editid);
                return read_db(String.valueOf(ename.getText()),String.valueOf(ephone.getText()),String.valueOf(eid.getText()),check);
            }
        }
        // db연결 뒤 결과 나온 후 실행
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            // db접속 후 return값이 true여야 실행함
            if(result){
                EditText ephoneid = (EditText) findViewById(R.id.editphoneid);
                EditText ephonepw = (EditText) findViewById(R.id.editphonepw);
                idennum = mkinum();
                if(check == 1){
                    add_sms(String.valueOf(ephoneid.getText()),idennum);
                } else if (check == 2) {
                    add_sms(String.valueOf(ephonepw.getText()),idennum);
                }
                ToastMsg("인증번호를 발송하였습니다");
            }
            else{
                ToastMsg("다시 확인해주세요");
            }
        }
    }

    // 아이디, 비밀번호 찾을때의 db접근
    public boolean read_db(String e_name, String e_phone,String e_id,int check) {
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


        // 아이디 찾기
        if(check == 1){
            //con 엔 파라미터로 url(data source source 마우스우 프로퍼티로 가서 커넥션 유알엘 확인)
            try {
                Class.forName(driver); // 에드케치 2번쩨걸로함.
                // 정의해둔 변수들 이용하여 db연결
                con = DriverManager.getConnection(url,id,pw);
                System.out.println("연결성공");
                // sql문 table이름 대문자로 해야됨
                // 입력한 userid를 이용하여 정보 검색
                System.out.println(e_name + " " + e_phone);
                String sqlQuery = String.format("SELECT * FROM USER");
                System.out.println(sqlQuery);
                stmt = con.createStatement();
                // 실행
                rs = stmt.executeQuery(sqlQuery);

                // 결과 처리 column이름으로 해야됨
                // sql문으로 이름, 전화번호를 이용하여 id검색
                while (rs.next()) {
                    System.out.println(rs);
                    System.out.println(String.format("이름 : %s, 번호 : %s",e_name,e_phone));
                    String mail = rs.getString("user_mail");
                    System.out.println(rs.getString("user_name") + ", " + rs.getString("user_mail"));
                    System.out.println();
                    if (e_phone.equals(mail)&&e_name.equals(rs.getString("user_name"))){
                        System.out.println("찾았다");
                        db_id = rs.getString("user_id");
                        break;
                    }
                    //System.out.println("usernum : " + user_num + " username : " + user_name + " userid : " + userId + " userpw : " + userPw);
                    // 결과를 원하는 대로 처리 or 출력
                }
                // 실행 끝나면 닫음
                con.close();
                System.out.println("데이터베이스가 잘 종료되었다.");
                System.out.println("userid : " + db_id);
                // 입력한 아이디 찾았으면 true 리턴
                if (db_id.equals("")){
                    return false;
                }
                else{
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

        // 비밀번호 찾기
        else if(check == 2){
            //con 엔 파라미터로 url(data source source 마우스우 프로퍼티로 가서 커넥션 유알엘 확인)
            try {
                Class.forName(driver); // 에드케치 2번쩨걸로함.
                // 정의해둔 변수들 이용하여 db연결
                con = DriverManager.getConnection(url,id,pw);
                System.out.println("연결성공");
                // sql문 table이름 대문자로 해야됨
                // 입력한 userid를 이용하여 정보 검색
                String sqlQuery = String.format("SELECT * from USER");
                stmt = con.createStatement();
                // 실행
                rs = stmt.executeQuery(sqlQuery);

                // 결과 처리 column이름으로 해야됨
                // 입력한 userid를 이용하여 pw를 찾음
                while (rs.next()) {
                    if (e_id.equals(rs.getString("user_id")) && e_phone.equals(rs.getString("user_mail"))){
                        db_pw = rs.getString("user_pw");
                        break;
                    }
                    // 결과를 원하는 대로 처리 or 출력
                }
                System.out.println(db_pw);
                // 실행 끝나면 닫음
                con.close();
                System.out.println("데이터베이스가 잘 종료되었다.");
                // 비밀번호 알아냈으면 true, 아니면 false
                if (db_pw.equals("")){
                    return false;
                }
                else{
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
        else{
            return false;
        }
    }
    //토스트 알림 함수
    public void ToastMsg(String msg){
        Toast.makeText(FindidActivity.this,String.format("%s",msg),Toast.LENGTH_SHORT).show();
    }
    // 아이디 혹은 비밀번호 알림창
    public void alert(String info,int check){
        String title;
        String Message;
        if (check == 1){
            title = "아이디 찾기";
            EditText ename = (EditText) findViewById(R.id.editnameid);
            Message = String.valueOf(ename.getText()) + "님의 아이디는 " + info + "입니다";
        }
        else{
            title = "비밀번호 찾기";
            EditText ename = (EditText) findViewById(R.id.editnamepw);
            Message = String.valueOf(ename.getText()) + "님의 비밀번호는 " + info + "입니다";
        }
        AlertDialog.Builder dlg = new AlertDialog.Builder(FindidActivity.this);
        dlg.setTitle(title); //제목
        dlg.setMessage(Message); // 메시지
        dlg.setPositiveButton("확인",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which) {
                //토스트 메시지
                //Toast.makeText(findpw.this,"확인을 눌르셨습니다.",Toast.LENGTH_SHORT).show();
            }
        });
        dlg.show();
    }
    // 이메일 발송
    public void add_sms(String mail,String num){
        new MailSender().sendEmail(mail,num);
    }
}
