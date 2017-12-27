package com.github.a5809909.mygpsapplication.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;

//import com.google.gson.JsonArray;
//import com.google.gson.JsonObject;
//import com.koushikdutta.async.future.FutureCallback;
//import com.koushikdutta.ion.Ion;
//
//import org.json.JSONException;
//import org.json.JSONObject;

public class LoggerService extends Service {


    private static final String TAG = "LoggerService";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 0;
    private String lat;
    private String longitude;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class LocationListener implements android.location.LocationListener {

        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);


            Log.d(TAG, mLastLocation.getLatitude() + " ," + mLastLocation);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged: " + location);

            lat = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());
            Toast.makeText(getApplicationContext(),"lat:"+lat+" longitude:"+longitude,Toast.LENGTH_LONG).show();
            callGoogleApi();
            // sendData();
            mLastLocation.set(location);
        }

        @Override
        public void onStatusChanged(String provider, int i, Bundle bundle) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Testing", "Service got created");
        Toast.makeText(this, "ServiceClass.onCreate()", Toast.LENGTH_LONG).show();
        
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        initializeLocationManager();
    }
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub

        Log.e(TAG, "onDestroy");
        super.onDestroy();

        destroyLocationListner();
    }

    public void destroyLocationListner() {
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
          Toast.makeText(this, "ServiceClass.onStart()", Toast.LENGTH_LONG).show();
        Log.d("Testing", "Service got started");

      //  callGoogleApi();
    }


    private void initializeLocationManager() {
        Log.d(TAG, "init loc man");

        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) | mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //invoke location from phone
            Log.d(TAG, "init loc man");

            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        mLocationListeners[1]);

            } catch (java.lang.SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "network provider does not exist, " + ex.getMessage());
            }
            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        mLocationListeners[0]);
            } catch (java.lang.SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "gps provider does not exist " + ex.getMessage());
            }

        } else {

            Log.d(TAG, "call handler");

            callGoogleApi();


        }
    }

    public void callGoogleApi() {
        Log.d(TAG, "get data called");
        int mcc = 0, mnc = 0, cid = 0, lac = 0;

        final TelephonyManager telephony = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephony.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
            final GsmCellLocation location = (GsmCellLocation) telephony.getCellLocation();
            if (location != null) {
                cid = location.getCid();
                lac = location.getLac();
                Toast.makeText(this,"cid:"+cid+" lac:"+lac,Toast.LENGTH_LONG).show();
                Log.d(TAG, location.getCid() + "   " + location.getLac());

            }
            String networkOperator = telephony.getNetworkOperator();

            if (networkOperator != null) {
                mcc = Integer.parseInt(networkOperator.substring(0, 3));
                mnc = Integer.parseInt(networkOperator.substring(3));
            }
            Log.d(TAG, mcc + "  " + mnc);


        }
//        JsonObject object = new JsonObject();
//        JsonObject jsonArrayObject = new JsonObject();
//        JsonArray jsonArray = new JsonArray();
//
//
//        jsonArrayObject.addProperty("cellId", cid);
//        jsonArrayObject.addProperty("locationAreaCode", lac);
//        jsonArrayObject.addProperty("mobileCountryCode", mcc);
//        jsonArrayObject.addProperty("mobileNetworkCode", mnc);
//
//        jsonArray.add(jsonArrayObject);
//
//        object.add("cellTowers", jsonArray);
//
//        if (jsonArrayObject.toString() != null) {
//            Log.d("codeoServtoString", jsonArrayObject.toString());
//
//        }
//
//
//        try {
//            Ion.with(this)
//                    .load("https://www.googleapis.com/geolocation/v1/geolocate?key=AIzaSyBnF1iPIWboObGjMSXcLgZ9sPXsB5HPFHk")
//                    .setJsonObjectBody(object)
//                    .asString()
//                    .setCallback(new FutureCallback<String>() {
//                        @Override
//                        public void onCompleted(Exception e, String result) {
//                            if (result != null) {
//                                try {
//                                    JSONObject obj1 = null;
//                                    obj1 = new JSONObject(result);
//                                    String msg = String.valueOf(obj1.get("msg"));
//                                    //   Toast.makeText(MyService.this, msg, Toast.LENGTH_SHORT).show();
//                                } catch (JSONException e1) {
//                                    //    Toast.makeText(MyService.this, e1.getMessage(), Toast.LENGTH_SHORT).show();
//                                }
//                                Log.d(TAG, result);
//
//                            }
//                            JSONObject obj = null;
//                            try {
//                                obj = new JSONObject(result);
//                            } catch (JSONException e1) {
//                                e1.printStackTrace();
//                            }
//                            try {
//                                JSONObject location = obj.getJSONObject("location");
//                                String latit = location.getString("lat");
//                                String langi = location.getString("lng");
//
//                                Log.d(TAG, latit + "  " + langi);
//
//                               // sendData(latit, langi);
//
//                            } catch (JSONException e1) {
//                                e1.printStackTrace();
//                            }
//
//                        }
//                    });
//
//            Toast.makeText(this, "result called"+longitude+" "+lat, Toast.LENGTH_SHORT).show();
//
//        } catch (Exception e) {
//            //  Toast.makeText(this, "result exceptiom", Toast.LENGTH_SHORT).show();
//            Log.d(TAG, "error");
//        }


    }
}