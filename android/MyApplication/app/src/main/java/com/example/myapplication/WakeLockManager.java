package com.example.myapplication;

import android.content.Context;
import android.os.PowerManager;

public class WakeLockManager {
    private PowerManager.WakeLock wakeLock;

    // 화면 깨우기 메서드
    void acquireWakeLock(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "MyApp:WakeLock"
        );
        wakeLock.acquire();
        System.out.println("알림");
    }

    // 화면 꺼짐 방지 해제 메서드
    void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }
}
