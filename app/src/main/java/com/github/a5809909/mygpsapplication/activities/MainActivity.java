package com.github.a5809909.mygpsapplication.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.github.a5809909.mygpsapplication.R;
import com.github.a5809909.mygpsapplication.Services.LoggerService;
import com.github.a5809909.mygpsapplication.model.LbsInfo;
import com.github.a5809909.mygpsapplication.model.PhoneState;
import com.github.a5809909.mygpsapplication.sql.DatabaseHelper;
import com.github.a5809909.mygpsapplication.yandexlbs.PhoneStateCollector;
import com.github.a5809909.mygpsapplication.yandexlbs.WifiAndCellCollector;

import java.util.Calendar;

public class MainActivity extends Activity {
    private static final int LOCATION_PERMISSION_CODE = 855;
    private static final int PERMISSION_CODE = 8785;

    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };
    private static final int REQUEST_LOCATION_PERMISSIONS = 0;
    private static final String TAG = "Logg";
    private MainActivity instance;
    private WifiAndCellCollector wifiAndCellCollector;
    private Button btnDoLbs;
    private TextView lbsLatitude, lbsLongtitude, lbsAltitude, lbsPrecision, lbsType;
    private AlertDialog alert;
    private ProgressDialog progressDialog;
    private PhoneStateCollector phoneStateCollector;
    private DatabaseHelper databaseHelper;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (isLocationPermissionsAllowed(this)) {
                startService();
            } else {
                requestStoragePermissions(this, LOCATION_PERMISSION_CODE);
                startService();
            }
        } else {
            startService();
        }


        instance = this;
        wifiAndCellCollector = new WifiAndCellCollector(this);
        phoneStateCollector = new PhoneStateCollector(this);

        btnDoLbs = findViewById(R.id.btn_show_database);
        btnDoLbs.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intentRegister = new Intent(getApplicationContext(), DataActivity.class);
                startActivity(intentRegister);
            }
        });
        btnDoLbs = findViewById(R.id.btn_send_gps_com);
        btnDoLbs.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                phoneStateCollector.logi();
            }
        });
        btnDoLbs = findViewById(R.id.btn_do_lbs);
        btnDoLbs.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                SimpleAsyncTask mAsyncTask = new SimpleAsyncTask();
                mAsyncTask.execute();
            }
        });
        btnDoLbs = findViewById(R.id.btn_auth_gps_com);
        btnDoLbs.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                PhoneStateAsyncTask mPhoneStateAsyncTask = new PhoneStateAsyncTask();
                mPhoneStateAsyncTask.execute();
            }
        });
    }

    public static void requestStoragePermissions(Activity activity, int PERMISSION_REQUEST_CODE) {
        java.lang.String[] perms = {"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.READ_PHONE_STATE"};
        ActivityCompat.requestPermissions(activity, perms, PERMISSION_REQUEST_CODE);
    }

    public static boolean isLocationPermissionsAllowed(Activity activity) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            return activity.checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_GRANTED
                    &&
                    activity.checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_GRANTED
                    &&
                    activity.checkSelfPermission("android.permission.READ_PHONE_STATE") == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public void startService() {
        Log.d(TAG, "startService() called");
        Calendar cal = Calendar.getInstance();
        Intent intent = new Intent(this, LoggerService.class);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),60 * 1000, pintent);
    }







    public class SimpleAsyncTask extends AsyncTask<Void, Integer, LbsInfo> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.hide();
            }
            progressDialog = ProgressDialog.show(instance, null, "Please wait");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected LbsInfo doInBackground(Void... params) {

            LbsInfo lbsInfo = wifiAndCellCollector.requestMyLocation();
           // wifiAndCellCollector.stopCollect();
            return lbsInfo;
        }

        @Override
        protected void onPostExecute(LbsInfo result) {
            super.onPostExecute(result);
            initViews();
            lbsLatitude.setText("Latitude=" + result.lbsLatitude);
            lbsLongtitude.setText("Longtitude=" + result.lbsLongtitude);
            lbsAltitude.setText("Altitude=" + result.lbsAltitude);
            lbsPrecision.setText("Precision=" + result.lbsPrecision);
            lbsType.setText("Type=" + result.lbsType);
            progressDialog.hide();
            //    saveInSql(result);
        }

    }

    public class PhoneStateAsyncTask extends AsyncTask<Void, Integer, PhoneState> {

        @Override
        protected PhoneState doInBackground(Void... params) {

            PhoneState phoneState = phoneStateCollector.getPhoneState();
            return phoneState;
        }

        @Override
        protected void onPostExecute(PhoneState result) {
            super.onPostExecute(result);
            saveInSql(result);
        }

    }

    private void initViews() {
        lbsLatitude = findViewById(R.id.lbs_latitude);
        lbsLongtitude = findViewById(R.id.lbs_longtitude);
        lbsAltitude = findViewById(R.id.lbs_altitude);
        lbsPrecision = findViewById(R.id.lbs_precision);
        lbsType = findViewById(R.id.lbs_type);
    }

    private void saveInSql(PhoneState result) {
        databaseHelper = new DatabaseHelper(this);
        databaseHelper.addUser(result);

    }

    public void print(String message) {
        lbsAltitude.setText(message);

    }

    private boolean hasLocationPermission() {
        int result = ContextCompat
                .checkSelfPermission(this, LOCATION_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onStart() {
        super.onStart();
//        if (hasLocationPermission()) {
//            startService(new Intent(MainActivity.this, LogService.class));
//            wifiAndCellCollector.startCollect();
//        } else {
//            requestPermissions(LOCATION_PERMISSIONS,
//                    REQUEST_LOCATION_PERMISSIONS);
//        }

    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                                           int[] grantResults) {
//        switch (requestCode) {
//            case REQUEST_LOCATION_PERMISSIONS:
//                if (hasLocationPermission()) {
//                    startService(new Intent(MainActivity.this, LogService.class));
//                    wifiAndCellCollector.startCollect();
//                }
//            default:
//                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }
//    }



    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    //      wifiAndCellCollector.stopCollect();
}

}