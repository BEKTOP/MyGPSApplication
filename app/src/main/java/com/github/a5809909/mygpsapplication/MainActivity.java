package com.github.a5809909.mygpsapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.github.a5809909.mygpsapplication.activities.UsersListActivity;
import com.github.a5809909.mygpsapplication.model.User;
import com.github.a5809909.mygpsapplication.sql.DatabaseHelper;
import com.github.a5809909.mygpsapplication.model.LbsInfo;
import com.github.a5809909.mygpsapplication.yandexlbs.PhoneInfoCollector;
import com.github.a5809909.mygpsapplication.yandexlbs.WifiAndCellCollector;

public class MainActivity extends Activity {

    private MainActivity instance;
    private WifiAndCellCollector wifiAndCellCollector;

    private Button btnDoLbs;
    private TextView lbsLatitude, lbsLongtitude, lbsAltitude, lbsPrecision, lbsType;
    private AlertDialog alert;
    private ProgressDialog progressDialog;
    private DatabaseHelper databaseHelper;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;

//        SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);
//        String uuid = settings.getString("UUID", null);
//        if (uuid == null) {
//            uuid = generateUUID();
//            Editor edit = settings.edit();
//            edit.putString("UUID", uuid);
//            edit.commit();
//        }
        wifiAndCellCollector = new WifiAndCellCollector(this);
        final PhoneInfoCollector phoneInfoCollector = new PhoneInfoCollector(this);
        setContentView(R.layout.activity_main);

        btnDoLbs = (Button) findViewById(R.id.btn_show_database);
        btnDoLbs.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intentRegister = new Intent(getApplicationContext(), UsersListActivity.class);
                startActivity(intentRegister);
            }
        });

                btnDoLbs = (Button) findViewById(R.id.btn_do_lbs);
        btnDoLbs.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                SimpleAsyncTask mAsyncTask = new SimpleAsyncTask();
                mAsyncTask.execute();

//                if (progressDialog != null && progressDialog.isShowing()) {
//                    progressDialog.hide();
//                }
//                progressDialog = ProgressDialog.show(instance, null, "Please wait");
//                progressDialog.setCanceledOnTouchOutside(false);
//                progressDialog.setCancelable(false);
//                progressDialog.show();
//                (new Thread() {
//                    @Override
//                    public void run() {
//                LbsInfo lbsInfo = wifiAndCellCollector.requestMyLocation();
//                    }
//                }).start();

//                lbsLatitude = (TextView) findViewById(R.id.lbs_latitude);
//                lbsLongtitude = (TextView) findViewById(R.id.lbs_longtitude);
//                lbsAltitude = (TextView) findViewById(R.id.lbs_altitude);
//                lbsPrecision = (TextView) findViewById(R.id.lbs_precision);
//                lbsType = (TextView) findViewById(R.id.lbs_type);
//                lbsLatitude.setText("Latitude=" + lbsInfo.lbsLatitude);
//                lbsLongtitude.setText("Longtitude=" + lbsInfo.lbsLongtitude);
//                lbsAltitude.setText("Altitude=" + lbsInfo.lbsAltitude);
//                lbsPrecision.setText("Precision=" + lbsInfo.lbsPrecision);
//                lbsType.setText("Type=" + lbsInfo.lbsType);

            }
        });

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

            initObjects(result);

            progressDialog.hide();
        }

    }

    private void initViews() {
        lbsLatitude = (TextView) findViewById(R.id.lbs_latitude);
        lbsLongtitude = (TextView) findViewById(R.id.lbs_longtitude);
        lbsAltitude = (TextView) findViewById(R.id.lbs_altitude);
        lbsPrecision = (TextView) findViewById(R.id.lbs_precision);
        lbsType = (TextView) findViewById(R.id.lbs_type);
    }

    private void initObjects(LbsInfo result) {
        databaseHelper = new DatabaseHelper(this);
        User user = new User();
        user.setName(result.lbsLatitude);
        user.setEmail(result.lbsLongtitude);
        user.setPassword(result.lbsType);

        databaseHelper.addUser(user);

    }
    @Override
    protected void onResume() {
        super.onResume();
        wifiAndCellCollector.startCollect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        wifiAndCellCollector.stopCollect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //
    public void onLocationChange(final LbsInfo lbsInfo) {
        if (lbsInfo != null) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.hide();
                    }
                    if (lbsInfo.isError) {
                        if (alert != null && alert.isShowing()) {
                            alert.hide();
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(instance);
                        builder.setMessage(lbsInfo.errorMessage)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        alert = builder.create();
                        alert.show();
                        lbsLatitude.setText("");
                        lbsLongtitude.setText("");
                        lbsAltitude.setText("");
                        lbsPrecision.setText("");
                        lbsType.setText("");
                    } else {
                        lbsLatitude.setText("Latitude=" + lbsInfo.lbsLatitude);
                        lbsLongtitude.setText("Longtitude=" + lbsInfo.lbsLongtitude);
                        lbsAltitude.setText("Altitude=" + lbsInfo.lbsAltitude);
                        lbsPrecision.setText("Precision=" + lbsInfo.lbsPrecision);
                        lbsType.setText("Type=" + lbsInfo.lbsType);
                    }
                }
            });
        }
    }
//
//    /**
//     * RFC UUID generation
//     */
//    public String generateUUID() {
//        UUID uuid = UUID.randomUUID();
//        StringBuilder str = new StringBuilder(uuid.toString());
//        int index = str.indexOf("-");
//        while (index > 0) {
//            str.deleteCharAt(index);
//            index = str.indexOf("-");
//        }
//        return str.toString();
//    }
//
}