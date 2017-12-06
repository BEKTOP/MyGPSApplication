package com.github.a5809909.mygpsapplication.yandexlbs;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.github.a5809909.mygpsapplication.model.LbsInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.GZIPOutputStream;

public class WifiAndCellCollector extends PhoneStateListener implements Runnable {

    private static final String[] lbsPostName = new String[]{"xml"};
    private static final String[] lbsContentType = new String[]{"xml"};

    private static final String[] wifipoolPostName = new String[]{"data"};
    private static final String[] wifipoolContentType = new String[]{"xml"};
    private static final String[] wifipoolContentTypeGzipped = new String[]{"xml/gzip"};

    public static final String PROTOCOL_VERSION = "1.0";
    public static final String API_KEY = "AIvQHVoBAAAAdhDDPQMAsE3v4yl-GtvM_p2mMfn9qdLurB4AAAAAAAAAAAB0XwzE9oAoNO3YU6Fo2DofJZan4A==";

    public static final String LBS_API_HOST = "http://api.lbs.yandex.net/geolocation";
    public static final String WIFIPOOL_HOST = "http://api.lbs.yandex.net/partners/wifipool?";

    public static final String GSM = "gsm";
    public static final String CDMA = "cdma";

    private static final long COLLECTION_TIMEOUT = 30000;
    private static final long WIFI_SCAN_TIMEOUT = 30000;
    private static final long GPS_SCAN_TIMEOUT = 2000;
    private static final long GPS_OLD = 3000;               // если со времени фикса прошло больше времени, то данные считаются устаревшие
    private static final long SEND_TIMEOUT = 30000;

    private Context context;
    private String uuid;
    private ArrayList<String> wifipoolChunks;
    private SimpleDateFormat formatter;
    private TelephonyManager tm;

    private String radioType;
    private String networkType;
    private String mcc;
    private String mnc;
    private List<CellInfo> cellInfos;
    private int cellId, lac, signalStrength;

    private WifiManager wifi;
    private long lastWifiScanTime;
    private List<WifiInfo> wifiInfos;

    private volatile Location lastGpsFix;
    private volatile long lastGpsFixTime;
    private long lastSendDataTime;

    private String manufacturer;
    private String model;

    private volatile boolean isRun;

    public static Map<Integer,String> networkTypeStr;
    static {
        networkTypeStr = new HashMap<Integer,String>();
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_GPRS, "GPRS");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_EDGE, "EDGE");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_UMTS, "UMTS");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_HSDPA, "HSDPA");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_HSUPA, "HSUPA");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_HSPA, "HSPA");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_CDMA, "CDMA");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_EVDO_0, "EVDO_0");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_EVDO_A, "EVDO_A");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_1xRTT, "1xRTT");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_IDEN, "IDEN");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_UNKNOWN, "UNKNOWN");
    }

    public WifiAndCellCollector(Context context) {
   //     this.listener = listener;
        this.context = context;
        this.uuid = "123";
        tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            networkType = networkTypeStr.get(tm.getNetworkType());
            radioType = getRadioType(tm.getNetworkType());
            String mccAndMnc = tm.getNetworkOperator();
            cellInfos = new ArrayList<CellInfo>();
            if (mccAndMnc != null && mccAndMnc.length() > 3) {
                mcc = mccAndMnc.substring(0, 3);
                mnc = mccAndMnc.substring(3);
            } else {
                mcc = mnc = null;
            }
        }

        try {
            model = new String(encodeUrl(Build.MODEL.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            model = new String(encodeUrl(Build.MODEL.getBytes()));
        }
        try {
            manufacturer = new String(encodeUrl(getDeviceManufacturer().getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            manufacturer = new String(encodeUrl(getDeviceManufacturer().getBytes()));
        }

        formatter = new SimpleDateFormat("ddMMyyyy:HHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        wifipoolChunks = new ArrayList<String>();
        wifiInfos = new ArrayList<WifiInfo>();
        wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        lastWifiScanTime = 0;
        lastSendDataTime = System.currentTimeMillis();
    }

    public void startCollect() {
        isRun = true;
        if (tm != null) {
            tm.listen(this, PhoneStateListener.LISTEN_SIGNAL_STRENGTH | PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
        }
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
     //   locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_SCAN_TIMEOUT, 1, this);
        (new Thread(this)).start();
    }

    public void stopCollect() {
        isRun = false;
        if (tm != null) {
            tm.listen(this, PhoneStateListener.LISTEN_NONE);
        }
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
     //   locationManager.removeUpdates(this);
    }

    @Override
    public void run() {
        while (isRun) {
            collectWifiInfo();
            collectCellInfo();
            generateAndAddWifiPoolChunk();
            sendDataIfNeed();
            try {
                Thread.sleep(COLLECTION_TIMEOUT);
            } catch (InterruptedException ie) {}
        }
    }

    public void collectWifiInfo() {
        wifiInfos.clear();
        if (wifi != null && wifi.isWifiEnabled()) {
            List<ScanResult> wifiNetworks = wifi.getScanResults();
            if (wifiNetworks != null && wifiNetworks.size() > 0) {
                for (ScanResult net:wifiNetworks) {
                    WifiInfo info = new WifiInfo();
                    info.mac = net.BSSID.toUpperCase();
                    char[] mac = net.BSSID.toUpperCase().toCharArray();
                    info.signalStrength = net.level;
                    char ch;
                    StringBuilder ssid = new StringBuilder(12);
                    for (int i = 0; i < mac.length; i++) {
                        ch = mac[i];
                        if (ch != ':') {
                            ssid.append(ch);
                        }
                    }
                    info.ssid = ssid.toString();
                    info.name = Base64.encode(net.SSID.getBytes());
                    wifiInfos.add(info);
                }
            }

            long currentTime = System.currentTimeMillis();
            if (lastWifiScanTime > currentTime) {
                lastWifiScanTime = currentTime;
            } else if (currentTime - lastWifiScanTime > WIFI_SCAN_TIMEOUT) {
                lastWifiScanTime = currentTime;
                wifi.startScan();
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static final Class[] emptyParamDesc = new Class[]{};
    private static final Object[] emptyParam = new Object[]{};

    public void collectCellInfo() {
        if (tm == null) {
            return;
        }
        cellInfos.clear();
        List<NeighboringCellInfo> cellList = tm.getNeighboringCellInfo();
        for (NeighboringCellInfo cell : cellList) {
            int cellId = cell.getCid();
            int lac = NeighboringCellInfo.UNKNOWN_CID;
            try {
                // Since: API Level 5
                Method getLacMethod = NeighboringCellInfo.class.getMethod("getLac", emptyParamDesc);
                if (getLacMethod != null) {
                    lac = ((Integer) getLacMethod.invoke(cell, emptyParam)).intValue();
                }
            } catch (Throwable e) {
            }
            
            int signalStrength = cell.getRssi();//since 1.5
            int psc = NeighboringCellInfo.UNKNOWN_CID;
            if (cellId == NeighboringCellInfo.UNKNOWN_CID) {
                try {
                    // Since: API Level 5
                    Method getPscMethod = NeighboringCellInfo.class.getMethod("getPsc", emptyParamDesc);
                    if (getPscMethod != null) {
                        psc = ((Integer) getPscMethod.invoke(cell, emptyParam)).intValue();
                    }
                } catch (Throwable e) {
                }
                cellId = psc;
            }
            
            if (cellId != NeighboringCellInfo.UNKNOWN_CID) {
                String sLac = (lac != NeighboringCellInfo.UNKNOWN_CID) ? String.valueOf(lac) : "";
                String sSignalStrength = "";
                if (signalStrength != NeighboringCellInfo.UNKNOWN_RSSI) {
                    if (GSM.equals(radioType)) {
                        sSignalStrength = String.valueOf(-113 + 2 * signalStrength);
                    } else {
                        sSignalStrength = String.valueOf(signalStrength);
                    }
                }

                CellInfo info = new CellInfo();
                info.cellId = cellId;
                info.lac = sLac;
                info.signalStrength = sSignalStrength;
                cellInfos.add(info);
            }
        }
    }
    
    private void sendDataIfNeed() {
        if (System.currentTimeMillis() - lastSendDataTime >= SEND_TIMEOUT) {
            StringBuilder xml = new StringBuilder(3000);
            xml.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
            xml.append("<wifipool uuid=\"").append(uuid).append("\"")
               .append(" manufacturer=\"").append(manufacturer).append("\"")
               .append(" model=\"").append(model).append("\" >");
            xml.append("<api_key>").append(API_KEY).append("</api_key>");
            if (wifipoolChunks != null && wifipoolChunks.size() > 0) {
                for (String chunk:wifipoolChunks) {
                    xml.append(chunk);
                }
            }
            xml.append("</wifipool>");
            sendToServer(xml.toString());
            lastSendDataTime = System.currentTimeMillis();
            wifipoolChunks.clear();
        }
    }
    
    private void sendToServer(String xml) {
        byte[] dataBytes = xml.toString().getBytes();
        byte[] packedData = null;
        boolean gzipped = false;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzipStream;
            gzipStream = new GZIPOutputStream(baos);
            gzipStream.write(dataBytes, 0, dataBytes.length);
            gzipStream.finish();
            packedData = baos.toByteArray();
            gzipped = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (packedData == null) {
            packedData = dataBytes;
            gzipped = false;
        }
        
        StringBuilder urlBuilder = new StringBuilder(100);
        urlBuilder.append(WIFIPOOL_HOST)
                  .append("uuid=").append(uuid)
                  .append("&ver=1")
                  .append("&gzip=").append(gzipped ? 1 : 0);
        
        byte[] request = HttpConnector.encodeMIME(wifipoolPostName, gzipped ? wifipoolContentTypeGzipped : wifipoolContentType, new byte[][]{packedData});
        HttpConnector.doRequest(urlBuilder.toString(), request);
    }
    
    @Override
    public void onSignalStrengthChanged(int asu) {
        signalStrength = -113 + 2 * asu;
    }

    @Override
    public void onCellLocationChanged(CellLocation location) {
      if (location != null) {
          if (location instanceof GsmCellLocation) {
              GsmCellLocation gsmLoc = (GsmCellLocation) location;
              lac = gsmLoc.getLac();
              cellId = gsmLoc.getCid();
          }
      }
    }

//    // API SDK < 7
//    @Override
//    public void onDataConnectionStateChanged(int state) {
//        this.networkType = networkTypeStr.get(tm.getNetworkType());
//    }
//
//    // API SDK >= 7
//    @Override
//    public void onDataConnectionStateChanged(int state, int networkType) {
//        this.networkType = networkTypeStr.get(networkType);
//    }

//    @Override
//    public void onLocationChanged(Location location) {
//        lastGpsFixTime = System.currentTimeMillis();
//        lastGpsFix = location;
//    }

    public LbsInfo requestMyLocation() {
        String xmlRequest = generateRequestLbsXml();
        byte[] request = HttpConnector.encodeMIME(lbsPostName, lbsContentType, new byte[][]{xmlRequest.getBytes()});
        byte[] response = HttpConnector.doRequest(LBS_API_HOST, request);
        LbsInfo lbsInfo = LbsInfo.parseByteData(response);
        return lbsInfo;
//        if (listener != null) {
//            listener.onLocationChange(lbsInfo);
//        }
    }
    
    private synchronized String generateRequestLbsXml() {
        StringBuilder xml = new StringBuilder(200);
        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
        xml.append("<ya_lbs_request>");
        xml.append("<common>");
            xml.append("<version>").append(PROTOCOL_VERSION).append("</version>");
            xml.append("<radio_type>").append(radioType).append("</radio_type>");
            xml.append("<api_key>").append(API_KEY).append("</api_key>");
        xml.append("</common>");
        
        if (mcc != null && mnc != null) {
            xml.append("<gsm_cells>");
            
            if (cellInfos != null && cellInfos.size() > 0) {
                for (CellInfo info:cellInfos) {
                    xml.append("<cell>");
                    xml.append("<countrycode>").append(mcc).append("</countrycode>");
                    xml.append("<operatorid>").append(mnc).append("</operatorid>");
                    xml.append("<cellid>").append(info.cellId).append("</cellid>");
                    xml.append("<lac>").append(info.lac).append("</lac>");
                    xml.append("<signal_strength>").append(info.signalStrength).append("</signal_strength>");
                    xml.append("</cell>");
                }
            } else {
             // add current connected cell
                xml.append("<cell>");
                xml.append("<countrycode>").append(mcc).append("</countrycode>");
                xml.append("<operatorid>").append(mnc).append("</operatorid>");
                xml.append("<cellid>").append(cellId).append("</cellid>");
                xml.append("<lac>").append(lac).append("</lac>");
                xml.append("<signal_strength>").append(signalStrength).append("</signal_strength>");
                xml.append("</cell>");
            }
            
            xml.append("</gsm_cells>");
        }
        
        if (wifiInfos != null && wifiInfos.size() > 0) {
            xml.append("<wifi_networks>");
            for (WifiInfo info:wifiInfos) {
                xml.append("<network>");
                xml.append("<mac>").append(info.mac).append("</mac>");
                xml.append("<signal_strength>").append(info.signalStrength).append("</signal_strength>");
                xml.append("</network>");
            }
            xml.append("</wifi_networks>");
        }

        xml.append("</ya_lbs_request>");
        return xml.toString();
    }
    
    private synchronized void generateAndAddWifiPoolChunk() {
        StringBuilder xml = new StringBuilder(200);
        String time = formatter.format(System.currentTimeMillis());
        xml.append("<chunk type=\"normal\" ").append("time=\"").append(time).append("\" >");
        
//        // GPS
//        Location lastGpsFixCopy = lastGpsFix;
//        if (lastGpsFixCopy != null && System.currentTimeMillis() - lastGpsFixTime < GPS_OLD) {
//            int bearing = lastGpsFixCopy.hasBearing() ? (int) lastGpsFixCopy.getBearing() : -1;
//            xml.append("<gps lat=\"").append(formatCoord(lastGpsFixCopy.getLatitude())).append("\"")
//               .append(" lon=\"").append(formatCoord(lastGpsFixCopy.getLongitude())).append("\"")
//               .append(" speed=\"").append(lastGpsFixCopy.getSpeed() * 3.6f).append("\"");
//            if (bearing != -1) {
//               xml.append(" course=\"").append(bearing).append("\"");
//            }
//            xml.append(" >");
//        }
        
        // BSSID
        if (wifiInfos != null && wifiInfos.size() > 0) {
            xml.append("<bssids>");
            for (WifiInfo info:wifiInfos) {
                xml.append("<bssid name=\"").append(info.name).append("\"")
                   .append(" sigstr=\"").append(info.signalStrength).append("\"")
                   .append(" >");
                xml.append(info.ssid);
                xml.append("</bssid>");
            }
            xml.append("</bssids>");
        }
        
        // CELLS
        xml.append("<cellinfos network_type=\"").append(networkType).append("\" radio_type=\"").append(radioType).append("\" >");
        if (cellInfos != null && cellInfos.size() > 0) {
            for (CellInfo info:cellInfos) {
                xml.append("<cellinfo cellid=\"").append(info.cellId).append("\"")
                   .append(" lac=\"").append(info.lac).append("\"")
                   .append(" operatorid=\"").append(mnc).append("\"")
                   .append(" countrycode=\"").append(mcc).append("\"")
                   .append(" sigstr=\"").append(info.signalStrength).append("\"")
                   .append(" />");
            }
        } else {
            // add current connected cell
            xml.append("<cellinfo cellid=\"").append(cellId).append("\"")
               .append(" lac=\"").append(lac).append("\"")
               .append(" operatorid=\"").append(mnc).append("\"")
               .append(" countrycode=\"").append(mcc).append("\"")
               .append(" sigstr=\"").append(signalStrength).append("\"")
               .append(" />");
        }
        xml.append("</cellinfos>");
        
        xml.append("</chunk>");
        Log.d("LBS","xml = "+xml.toString());
        wifipoolChunks.add(xml.toString());
    }

    private String getRadioType(int networkType) {
        switch (networkType) {
            case -1:
                return "NONE";
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return GSM;
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return CDMA;
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
            default:
                return "UNKNOWN";
        }
    }
    
    private static String formatCoord(double number) {
        final double eps = 0.000000001;
        StringBuilder sb = new StringBuilder(10);
        int ipart = Math.abs((int) number);
        if (number < 0) {
            sb.append('-');
        }
        sb.append(ipart);
        sb.append('.');
        double num = Math.abs(number) - ipart;
        number += eps;
        for (int i = 0; i < 6; i++) {
            num *= 10;
            ipart = Math.abs((int) (num + eps));
            sb.append(ipart);
            num -= ipart;
            num += eps;
        }
        return sb.toString();
    }
    
    public static String getDeviceManufacturer() {
        String manufact;
        try {
            Class<android.os.Build> buildClass = android.os.Build.class;
            Field field = buildClass.getField("MANUFACTURER");
            manufact = (String) field.get(new android.os.Build());
        } catch (Throwable e) {
            manufact = "Unknown";
        }
        return manufact;
    }
    
    protected static final boolean[] WWW_FORM_URL = new boolean[256];

    // Static initializer for www_form_url
    static {
        // alpha characters
        for (int i = 'a'; i <= 'z'; i++) {
            WWW_FORM_URL[i] = true;
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            WWW_FORM_URL[i] = true;
        }
        // numeric characters
        for (int i = '0'; i <= '9'; i++) {
            WWW_FORM_URL[i] = true;
        }
        // special chars
        WWW_FORM_URL['-'] = true;
        WWW_FORM_URL['_'] = true;
        WWW_FORM_URL['.'] = true;
        WWW_FORM_URL['*'] = true;
        // blank to be replaced with +
        WWW_FORM_URL[' '] = true;
    }
    
    public static byte[] encodeUrl(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        boolean[] urlsafe = WWW_FORM_URL;

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i];
            if (b < 0) {
                b = 256 + b;
            }
            if (urlsafe[b]) {
                if (b == ' ') {
                    b = '+';
                }
                buffer.write(b);
            } else {
                buffer.write('%');
                char hex1 = Character.toUpperCase(forDigit((b >> 4) & 0xF, 16));
                char hex2 = Character.toUpperCase(forDigit(b & 0xF, 16));
                buffer.write(hex1);
                buffer.write(hex2);
            }
        }
        return buffer.toByteArray();
    }
    
    private static char forDigit(int digit, int radix) {
        if ((digit >= radix) || (digit < 0)) {
            return '\0';
        }
        if ((radix < Character.MIN_RADIX) || (radix > Character.MAX_RADIX)) {
            return '\0';
        }
        if (digit < 10) {
            return (char)('0' + digit);
        }
        return (char)('a' - 10 + digit);
    }
    
    private class CellInfo {
        private int cellId;
        private String lac;
        private String signalStrength;
    }
    
    private class WifiInfo {
        private String mac;
        private int signalStrength;
        
        private String ssid; 
        private String name;
    }

//    @Override
//    public void onProviderDisabled(String provider) {
//
//    }
//
//    @Override
//    public void onProviderEnabled(String provider) {
//
//    }
//
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//
//    }
}
