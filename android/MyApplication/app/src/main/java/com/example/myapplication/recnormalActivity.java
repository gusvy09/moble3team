package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.data.UrspRule;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class recnormalActivity extends AppCompatActivity {
    ListView listView1;
    ArrayAdapter<String> adapter;
    ArrayList<String> Urls = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recnormal);
        new HttpTask().execute();

    }
    private class HttpTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                // 서버 URL
                //String serverUrl = "http://"; // 로컬
                String serverUrl = "http://"; // 서버

                // HTTP 연결 설정
                URL url = new URL(serverUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // 응답 읽기
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // JSON 데이터 파싱
                JSONObject jsonObject = new JSONObject(response.toString());
                JSONArray jsonArray = jsonObject.getJSONArray("data");

                // JSON 데이터 출력
                for (int i = 1; i < jsonArray.length(); i++) {
                    Log.d("JSON Item", jsonArray.getString(i));
                    Urls.add(jsonArray.getString(i));
                }

                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result){
            System.out.println("백그라운드 후 들어옴");

            ArrayList<String> modifiedData = new ArrayList<>();
            for (String originalText : Urls) {

                int startIndex = originalText.indexOf("live/"); // "qqq/"의 인덱스
                int endIndex = originalText.indexOf("?", startIndex); // "?"의 인덱스, 시작 위치를 지정해서 "qqq/" 이후부터 검색


                if (startIndex != -1 && endIndex != -1) {
                    String extracted = originalText.substring(startIndex + 4, endIndex); // "qqq/" 다음부터 "?" 이전까지 추출
                    System.out.println(extracted); // "fire" 출력
                }
                String truncatedText = originalText.substring(startIndex+4, endIndex); // 예: 처음 10자만 추출, 원하는 길이로 변경
                truncatedText = truncatedText.replace("%3A",":");
                truncatedText = truncatedText.replace("%", " ");
                System.out.println(truncatedText);

                modifiedData.add(truncatedText);
            }
            adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,modifiedData);
            listView1 = findViewById(R.id.listView1);
            listView1.setAdapter(adapter);
            listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                // 콜백매개변수는 순서대로 어댑터뷰, 해당 아이템의 뷰, 클릭한 순번, 항목의 아이디
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    // 해당 리스트뷰의 이름 반환
                    // Urls.get(i).toString()
                    Go_Show(Urls.get(i).toString());
                    System.out.println(Urls.get(i).toString());
                }
            });
        }
    }
    public void Go_Show(String url){
        // 정보를 전달할 Intent 생성
        Intent intent = new Intent(this, ShowVideoActivity.class);

        // 정보 추가
        String dataToPass = "이 정보를 다음 액티비티로 전달합니다.";
        intent.putExtra("key", url);

        // 다음 액티비티 시작
        startActivity(intent);
    }
    //뒤로가기
    public void back(View view) {
        finish();
    }
}