package com.github.a5809909.mygpsapplication.yandexlbs;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.github.a5809909.mygpsapplication.model.PhoneState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhoneStateCollector {
    private int sizeOfCells;
    private int cellId;
    private int lac;
    private String mcc;
    private String  mnc;
    private String radioType;
    private String networkType;
    private String network;
    public static final String GSM = "gsm";
    public static final String CDMA = "cdma";
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

        String text;
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        GsmCellLocation gsmCell = (GsmCellLocation) manager.getCellLocation();

        if (gsmCell != null) {
            cellId = gsmCell.getCid();
            lac = gsmCell.getLac();
            networkType = networkTypeStr.get(manager.getNetworkType());
            radioType = getRadioType(manager.getNetworkType());
            String mccAndMnc = manager.getNetworkOperator();

            if (mccAndMnc != null && mccAndMnc.length() > 3) {
                mcc = mccAndMnc.substring(0, 3);
                mnc = mccAndMnc.substring(3);
            } else {
                mcc = mnc = null;
            }
            long currentTime = System.currentTimeMillis();
            cellInfos = new ArrayList<CellInfo>();
          //  int signalStrength


            sizeOfCells=  manager.getNeighboringCellInfo().size();

            Log.i("Cell", "cellId: "+cellId+"\n" +
                    "lac: "+lac+"\n" +
                    "radioType: "+radioType+"\n" +
                    "networkType: "+networkType+"\n"+
                    "mcc: "+mcc+"\n"+
                    "mnc: "+mnc+"\n"+
                    "mccAndMnc: "+mccAndMnc+"\n"+
                    "currentTime: "+currentTime+"\n"+
                    "sizeOfCells: "+sizeOfCells+"\n"
            );


//
//
//            List<NeighboringCellInfo> NeighboringList = manager.getNeighboringCellInfo();
//
//            String stringNeighboring = "Neighboring List- Lac : Cid : RSSI\n";
//            for (int i = 0; i < NeighboringList.size(); i++) {
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
//                text += (stringNeighboring);
//            }

        }


    }



    public PhoneState getPhoneState() {
        PhoneState phoneState = new PhoneState();
        phoneState.setCellId_0(cellId);
        phoneState.setLac_0(lac);
        phoneState.setMcc(mcc);

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


    private class CellInfo {
        private int cellId;
        private String lac;
        private String signalStrength;
    }

    private class WifiInfo {
        private String mac;
        private int signalStrength;
    }
}



