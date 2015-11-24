package org.telegram.messenger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by gzq on 15-11-24.
 */
public class SPAPollingService extends Service {

    public static final String ACTION = "org.telegram.android.spa.Key_Manager";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new PollingThread().start();
    }

    int count = 0;
    class PollingThread extends Thread {
        @Override
        public void run() {
            System.out.println("Polling...");
            ++count;
            if (count % 5 == 0) {
                System.out.println("New message!");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("SPAPollingService: onDestroy");
    }


}
