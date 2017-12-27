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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.a5809909.mygpsapplication.R;
import com.github.a5809909.mygpsapplication.Services.LoggerService;
import com.github.a5809909.mygpsapplication.model.LbsInfo;
import com.github.a5809909.mygpsapplication.model.PhoneState;
import com.github.a5809909.mygpsapplication.sql.DatabaseHelper;
import com.github.a5809909.mygpsapplication.yandexlbs.PhoneStateCollector;
import com.github.a5809909.mygpsapplication.yandexlbs.WifiAndCellCollector;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;

public class MainActivity extends Activity {
    private static final int LOCATION_PERMISSION_CODE = 855;
    private static final String API_KEY = "AIzaSyDNsRNkiJddjICdCY9fiFw3U6_nziORLC4";
    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };
    private MainActivity instance;
    private static final String TAG = "Main";
    private DatabaseHelper databaseHelper;
    private Button btnDoLbs;
    private TextView lbsLatitude, lbsLongtitude, lbsAltitude, lbsPrecision, lbsType;



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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
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
                //   phoneStateCollector.logi();
            }
        });
//        btnDoLbs = findViewById(R.id.btn_do_lbs);
//        btnDoLbs.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                getLocationClicked(v);
////                SimpleAsyncTask mAsyncTask = new SimpleAsyncTask();
////                mAsyncTask.execute();
//            }
//        });
        btnDoLbs = findViewById(R.id.btn_auth_gps_com);
        btnDoLbs.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
//                PhoneStateAsyncTask mPhoneStateAsyncTask = new PhoneStateAsyncTask();
//                mPhoneStateAsyncTask.execute();
            }
        });
    }

    public void startService() {
        Log.d(TAG, "startService() called");
        Calendar cal = Calendar.getInstance();
        Intent intent = new Intent(this, LoggerService.class);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),60 * 1000, pintent);
    }

    public void getLocationClicked(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (isLocationPermissionsAllowed(this)) {

            } else {
                requestStoragePermissions(this, LOCATION_PERMISSION_CODE);

            }

            TelephonyManager tel = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

            if (tel != null) {
                CellLocation loc = tel.getCellLocation();

                if ((loc != null) && (loc instanceof GsmCellLocation)) {
                    GsmCellLocation gsmLoc = (GsmCellLocation) loc;
                    String op = tel.getNetworkOperator();

                    String cid = "" + gsmLoc.getCid();
                    String lac = "" + gsmLoc.getLac();
                    String mcc = op.substring(0, 3);
                    String mnc = op.substring(3);
                    new HttpPostTask().execute(cid, lac, mcc, mnc);
                } else {
                    Toast.makeText(instance, "No valid GSM network found",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private class HttpPostTask extends AsyncTask<String, Void, String> {

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(instance);
            pd.setTitle("Getting Location");
            pd.setMessage("Please Wait...");
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {

            String result = null;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpost = new HttpPost("https://www.googleapis.com/geolocation/v1/geolocate?key=" + API_KEY);

            StringEntity se;

            try {
                JSONObject cellTower = new JSONObject();
                cellTower.put("cellId", params[0]);
                cellTower.put("locationAreaCode", params[1]);
                cellTower.put("mobileCountryCode", params[2]);
                cellTower.put("mobileNetworkCode", params[3]);
                Log.i(TAG, "cellId: "+params[0]+
                        ", locationAreaCode: "+params[1]+
                        ", mobileCountryCode: "+params[2]+
                        ", mobileNetworkCode: "+params[3]);
                JSONArray cellTowers = new JSONArray();
                cellTowers.put(cellTower);

                JSONObject rootObject = new JSONObject();
                rootObject.put("cellTowers", cellTowers);

                se = new StringEntity(rootObject.toString());
                se.setContentType("application/json");

                httpost.setEntity(se);
                httpost.setHeader("Accept", "application/json");
                httpost.setHeader("Content-type", "application/json");
                Log.i(TAG, "se: "+se);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String response = httpclient.execute(httpost, responseHandler);

                result = response;
                Log.i(TAG, "ssseee: "+se);
            } catch (Exception e) {
                final String err = e.getMessage();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(instance, "Exception requesting location: " + err, Toast.LENGTH_LONG).show();
                    }

                });
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (pd != null) {
                try {
                    pd.dismiss();
                } catch (Exception e) {
                }
            }

            if (result != null) {
                try {
                    JSONObject jsonResult = new JSONObject(result);
                    JSONObject location = jsonResult.getJSONObject("location");
                    String lat, lng;
                    lat = location.getString("lat");
                    lng = location.getString("lng");

                    if ((lat != null) &&
                            (!lat.isEmpty()) &&
                            (lng != null) &&
                            (!lng.isEmpty())) {
                        Log.i(TAG, "Lat:"+lat+", Long:"+lng);
                        instance.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?q=" + lat + "," + lng + "&iwloc=A")));
                    }
                } catch (Exception e) {
                    Toast.makeText(instance, "Exception parsing response: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }

    }





//    public class SimpleAsyncTask extends AsyncTask<Void, Integer, LbsInfo> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            if (progressDialog != null && progressDialog.isShowing()) {
//                progressDialog.hide();
//            }
//            progressDialog = ProgressDialog.show(instance, null, "Please wait");
//            progressDialog.setCanceledOnTouchOutside(false);
//            progressDialog.setCancelable(false);
//            progressDialog.show();
//        }
//
//        @Override
//        protected LbsInfo doInBackground(Void... params) {
//
//            LbsInfo lbsInfo = wifiAndCellCollector.requestMyLocation();
//           // wifiAndCellCollector.stopCollect();
//            return lbsInfo;
//        }
//
//        @Override
//        protected void onPostExecute(LbsInfo result) {
//            super.onPostExecute(result);
//            initViews();
//            lbsLatitude.setText("Latitude=" + result.lbsLatitude);
//            lbsLongtitude.setText("Longtitude=" + result.lbsLongtitude);
//            lbsAltitude.setText("Altitude=" + result.lbsAltitude);
//            lbsPrecision.setText("Precision=" + result.lbsPrecision);
//            lbsType.setText("Type=" + result.lbsType);
//            progressDialog.hide();
//            //    saveInSql(result);
//        }
//
//    }
//
//    public class PhoneStateAsyncTask extends AsyncTask<Void, Integer, PhoneState> {
//
//        @Override
//        protected PhoneState doInBackground(Void... params) {
//
//            PhoneState phoneState = phoneStateCollector.getPhoneState();
//            return phoneState;
//        }
//
//        @Override
//        protected void onPostExecute(PhoneState result) {
//            super.onPostExecute(result);
//            saveInSql(result);
//        }
//
//    }

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

    }

}