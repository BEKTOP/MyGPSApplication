package com.github.a5809909.mygpsapplication.Services;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.github.a5809909.mygpsapplication.Utils.ContextHolder;
import com.github.a5809909.mygpsapplication.yandexlbs.WifiAndCellCollector;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class LogService extends IntentService {

    public LogService() {
        super("LogService");
    }

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        // Normally we would do some work here, like download a file.
        // For our sample, we just sleep for 5 seconds.
        long endTime = System.currentTimeMillis() + 5*1000;
        while (System.currentTimeMillis() < endTime) {
            synchronized (this) {
                try {
                    Log.i("tag", "onHandleIntent: ");
                    wait(endTime - System.currentTimeMillis());
                    Toast.makeText(this, "service 5", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.i("tag", "onHandleIntent:2 ");
                }
            }
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent,flags,startId);
    }
}