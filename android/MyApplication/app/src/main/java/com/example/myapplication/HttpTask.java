package com.example.myapplication;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... params) {
        String result = null;

        try {
            // 네트워크 요청을 보낼 URL
            String requestUrl = params[0];

            // URL 객체 생성
            URL url = new URL(requestUrl);

            // HttpURLConnection 객체 생성
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // 요청 메소드 설정 (GET 요청)
            connection.setRequestMethod("GET");

            // 연결 시간 제한 설정
            connection.setConnectTimeout(5000); // 5초

            // 응답 데이터 읽기
            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            inputStream.close();

            result = response.toString();

            // 연결 종료
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        // 네트워크 요청 완료 후 처리할 내용을 이 메서드에서 구현
        if (result != null) {

        } else {
            // 요청 실패나 결과가 없을 때의 처리
        }
    }
}