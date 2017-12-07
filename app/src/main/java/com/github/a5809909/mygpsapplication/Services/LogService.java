package com.github.a5809909.mygpsapplication.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.github.a5809909.mygpsapplication.yandexlbs.WifiAndCellCollector;

public class LogService extends Service {

    WifiAndCellCollector wifiAndCellCollector;
    @Nullable
    @Override

    public IBinder onBind(Intent intent) {
                return null;
            }

            @Override
    public void onCreate() {

                Toast.makeText(this, "Create", Toast.LENGTH_SHORT).show();

                        super.onCreate();
            }

            @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
              //  wifiAndCellCollector.startCollect();
                Toast.makeText(this, "onStart",Toast.LENGTH_SHORT).show();
                        return super.onStartCommand(intent, flags, startId);
            }

            @Override
    public void onDestroy() {
                Toast.makeText(this, "Destroy",Toast.LENGTH_SHORT).show();
          //     wifiAndCellCollector.stopCollect();
                super.onDestroy();
            }
}