package com.github.a5809909.mygpsapplication.yandexlbs;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.github.a5809909.mygpsapplication.model.PhoneState;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class PhoneStateCollector {

    private final ArrayList<WifiInfo> wifiInfos;
    private final WifiManager wifi;
    private final long lastSendDataTime;

    private int cellId;
    private int lac;
    private int cellSize;
    private SimpleDateFormat formatter;
    private String mcc;
    private String  mnc;
    private String radioType;
    private String networkType;
    private String network;
    private String manufacturer;
    private String model;

    public static final String GSM = "gsm";
    public static final String CDMA = "cdma";
    private List<ScanResult> wifiNetworks;
    private List<CellInfo> cellInfos;

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

    public PhoneStateCollector(Context context) {

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        GsmCellLocation gsmCell = (GsmCellLocation) tm.getCellLocation();
        List<NeighboringCellInfo> cellList = tm.getNeighboringCellInfo();
        cellSize = cellList.size();
        if (tm != null) {
            networkType = networkTypeStr.get(tm.getNetworkType());
            radioType = getRadioType(tm.getNetworkType());

            cellId = gsmCell.getCid();
            lac = gsmCell.getLac();
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

        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy:HHmmss");
        wifiInfos = new ArrayList<WifiInfo>();
        wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        lastSendDataTime = System.currentTimeMillis();

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
          //  logi();

        }
    }

    public void logi() {
        Log.i("Cell", "cellId: "+cellId+"\n" +
                "lac: "+lac+"\n" +
                "radioType: "+radioType+"\n" +
                "networkType: "+networkType+"\n"+
                "mcc: "+mcc+"\n"+
                "mnc: "+mnc+"\n"+

                "model: "+model+"\n"+
                "manufacturer: "+ manufacturer +"\n"+
                "lastSendDataTime: "+lastSendDataTime+"\n"+
                "formatter: "+ formatter.format(lastSendDataTime)+"\n"+
                "cellSize: "+cellSize+"\n"+
                "wifiInfos.size: "+wifiInfos.size()+"\n"+
                "mac[0]: "+wifiInfos.get(0).mac+"\n"+
                "mac[1]: "+wifiInfos.get(1).mac+"\n"+
                "mac[2]: "+wifiInfos.get(2).mac+"\n"+
                "mac[3]: "+wifiInfos.get(3).mac+"\n"+
                "signalStrength[0]: "+wifiInfos.get(0).signalStrength+"\n"+
                "signalStrength[1]: "+wifiInfos.get(1).signalStrength+"\n"+
                "signalStrength[2]: "+wifiInfos.get(2).signalStrength+"\n"+
                "signalStrength[3]: "+wifiInfos.get(3).signalStrength+"\n"

        );
    }

//            String stringNeighboring = "Neighboring List- Lac : Cid : RSSI\n";
//
//            cellSize = NeighboringList.size();
//            for (int i = 0; i < cellSize; i++) {
//
//                String dBm;
//                int rssi = NeighboringList.get(i).getRssi();
//                if (rssi == NeighboringCellInfo.UNKNOWN_RSSI) {
//                    dBm = "Unknown RSSI";
//                } else {
//                    dBm = String.valueOf(-113 + 2 * rssi) + " dBm";
//                }
//
//                stringNeighboring = stringNeighboring
//                        + String.valueOf(NeighboringList.get(i).getLac()) + " : "
//                        + String.valueOf(NeighboringList.get(i).getCid()) + " : "
//                        + String.valueOf(NeighboringList.get(i).getPsc()) + " : "
//                        + String.valueOf(NeighboringList.get(i).getNetworkType()) + " : "
//                        + dBm + "\n";
//
//            }
//
//        }







    public PhoneState getPhoneState() {
        PhoneState phoneState = new PhoneState();
        phoneState.setLac_0(lac);
        phoneState.setMnc(mnc);
        phoneState.setMcc(mcc);
        phoneState.setNumberOfCells(cellSize);


        return phoneState;
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
    }    protected static final boolean[] WWW_FORM_URL = new boolean[256];

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


}





