package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Map;

public class MyBackgroundService extends FirebaseMessagingService {
    private WakeLockManager wakeLockManager;

    @Override
    public void onNewToken(String token){
        Log.d("FCM Log","Refreshed token : " + token);
        //sendRegistrationToServer(token);

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        if (remoteMessage.getData().size() > 0) {
            // 화면 깨우기
            wakeLockManager = new WakeLockManager();
            wakeLockManager.acquireWakeLock(this);

            // FCM 데이터 메시지 수신
            Map<String, String> data = remoteMessage.getData();

            // 데이터에서 원하는 정보 추출
            String messageBody = data.get("body");
            String messageTitle = data.get("title");

            // 채널 설정
            String channelId = "test";
            String channelName = "Test Channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                channel = new NotificationChannel(channelId, channelName, importance);
            }

            // 알림 매니저를 통해 채널 등록
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.createNotificationChannel(channel);
            }

            // 푸시 알림 생성
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(messageTitle)
                    .setContentText(messageBody)
                    .setAutoCancel(true);

            notificationManager.notify(0, notificationBuilder.build());

            // 화면 꺼짐 방지 해제
            wakeLockManager.releaseWakeLock();
        }
    }
}
