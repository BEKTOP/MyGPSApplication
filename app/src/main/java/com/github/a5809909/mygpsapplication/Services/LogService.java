package com.github.a5809909.mygpsapplication.Services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.github.a5809909.mygpsapplication.model.PhoneState;
import com.github.a5809909.mygpsapplication.sql.DatabaseHelper;
import com.github.a5809909.mygpsapplication.yandexlbs.Base64;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


public class LogService extends IntentService {

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
    private static final Class[] emptyParamDesc = new Class[]{};
    private static final Object[] emptyParam = new Object[]{};
    private static final long COLLECTION_TIMEOUT = 20000;
    private static final long WIFI_SCAN_TIMEOUT = 30000;
    private static final long GPS_SCAN_TIMEOUT = 2000;
    private static final long GPS_OLD = 3000;               // если со времени фикса прошло больше времени, то данные считаются устаревшие
    private static final long SEND_TIMEOUT = 30000;

    private ArrayList<String> wifipoolChunks;
    private SimpleDateFormat formatter;
    private TelephonyManager tm;

    private String radioType;
    private String networkType;
    private String mcc;
    private String mnc;
    private List<CellInfo> cellInfos;


    private WifiManager wifi;
    private long lastWifiScanTime;
    private List<LogService.WifiInfo> wifiInfos;
    private StringBuilder cellStr;
    private StringBuilder wifiStr;

    private long lastSendDataTime;

    private String manufacturer;
    private String model;
    private int cellSize;
    private volatile boolean isRun;

    public static Map<Integer, String> networkTypeStr;

    static {
        networkTypeStr = new HashMap<Integer, String>();
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

    private int mCellId_0 = 0;
    private int mLac_0 = 0;
    private int signalStrength_0 = 0;


    public LogService() {
        super("LogService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        isRun = true;
        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (tm != null) {
            networkType = networkTypeStr.get(tm.getNetworkType());
            radioType = getRadioType(tm.getNetworkType());
            String mccAndMnc = tm.getNetworkOperator();

            tm.getCellLocation();
            cellInfos = new ArrayList<CellInfo>();

            if (mccAndMnc != null && mccAndMnc.length() > 3) {
                mcc = mccAndMnc.substring(0, 3);
                mnc = mccAndMnc.substring(3);
            } else {
                mcc = mnc = null;
            }
        }

        tm.getNetworkOperatorName();
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
        formatter.setTimeZone(TimeZone.getTimeZone("Europe/Minsk"));

        wifipoolChunks = new ArrayList<String>();
        wifiInfos = new ArrayList<WifiInfo>();
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        lastWifiScanTime = 0;


        while (isRun) {
            lastSendDataTime = System.currentTimeMillis();
            collectWifiInfo();
            collectCellInfo();
            logi();
            lastSendDataTime = System.currentTimeMillis();
            int wifiSize = wifiInfos.size();

            PhoneState phoneState = new PhoneState();
            phoneState.setTime(formatter.format(lastSendDataTime));
            phoneState.setMcc(mcc);
            phoneState.setMnc(mnc);
            phoneState.setNumberOfCells(cellSize);
            phoneState.setNumberOfWifi(wifiSize);
            phoneState.setCellId(mCellId_0);
            phoneState.setLac(mLac_0);
            phoneState.setSignalStrength_0(signalStrength_0);
            phoneState.setCellInfo(cellStr.toString());
            phoneState.setWifiInfo(wifiStr.toString());
            phoneState.setRadioType(radioType);




            DatabaseHelper databaseHelper = new DatabaseHelper(this);
            databaseHelper.addUser(phoneState);
            try {
                Thread.sleep(COLLECTION_TIMEOUT);
            } catch (InterruptedException ie) {
            }
        }

    }

    public void collectCellInfo() {
        if (tm == null) {
            return;
        }
        GsmCellLocation cellLocation = (GsmCellLocation) tm.getCellLocation();
        List<android.telephony.CellInfo> signal = tm.getAllCellInfo();
      // signal.get(0).

        mCellId_0 = cellLocation.getCid();
        mLac_0 = cellLocation.getLac();
        cellStr = new StringBuilder(300);

      //  signalStrength_0= cellLocation;

      //  cellInfos.clear();
        List<NeighboringCellInfo> cellList = tm.getNeighboringCellInfo();
        cellSize = cellList.size();

        int i=0;
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
            Log.i("celll2", "signalStrength: "+ signalStrength);

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
                    Log.i("celll1", "signalStrength: "+ sSignalStrength);
                }


               cellStr.append(i+",");
               i++;
                cellStr.append(cellId+",");
                cellStr.append(sLac+",");
                cellStr.append(sSignalStrength+",");
                Log.i("Celll", "collectCellInfo: " + cellStr);

//                LogService.CellInfo info = new LogService.CellInfo();
//                info.cellId = cellId;
//                info.lac = sLac;
//                info.signalStrength = sSignalStrength;
//                cellInfos.add(info);
            }
        }
    }

    public void collectWifiInfo() {
        wifiStr = new StringBuilder(3000);

        wifiInfos.clear();
        int j=0;

        if (wifi != null && wifi.isWifiEnabled()) {
            List<ScanResult> wifiNetworks = wifi.getScanResults();
            if (wifiNetworks != null && wifiNetworks.size() > 0) {
                for (ScanResult net : wifiNetworks) {
                    LogService.WifiInfo info = new LogService.WifiInfo();
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
                    wifiStr.append(j+",");
                    wifiStr.append(wifiInfos.get(j).mac+",");
                    wifiStr.append(wifiInfos.get(j).signalStrength+",");
                    j++;
                    Log.i("wifiS", "collectWifiInfo: "+wifiStr);
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

    public void logi() {
        String message = "cellId: " + mCellId_0 + "\n" +
                "lac: " + mLac_0 + "\n" +
                "radioType: " + radioType + "\n" +
                "networkType: " + networkType + "\n" +
                "mcc: " + mcc + "\n" +
                "mnc: " + mnc + "\n" +
                "model: " + model + "\n" +
                "cellStr: " + cellStr + "\n" +
                "wifiStr: " + wifiStr + "\n" +
                "lastSendDataTime: " + lastSendDataTime + "\n" +
                "formatter: " + formatter.format(lastSendDataTime) + "\n" +
                "cellSize: " + cellSize + "\n" +
                "wifiInfos.size: " + wifiInfos.size() + "\n" +
                "mac[0]: " + wifiInfos.get(0).mac + "\n" +
                "mac[1]: " + wifiInfos.get(1).mac + "\n" +

                "signalStrength[0]: " + wifiInfos.get(0).signalStrength + "\n" +
                "signalStrength[1]: " + wifiInfos.get(1).signalStrength + "\n";


        Log.i("Cell", message);


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
            return (char) ('0' + digit);
        }
        return (char) ('a' - 10 + digit);
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



}