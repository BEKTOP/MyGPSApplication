package com.github.a5809909.mygpsapplication.yandexlbs;

import android.content.Context;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.widget.TextView;

import java.util.List;

public class PhoneInfoCollector {

public PhoneInfoCollector(Context context){

    String text;
    TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    GsmCellLocation gsmCell = (GsmCellLocation) manager.getCellLocation();
    if (gsmCell != null) {
        text = ("\n\tCID:\t" + gsmCell.getCid());
        text+=("\n\tLAC:\t" + gsmCell.getLac());
        text+=("\n\tNetworkOperator:\t" + manager.getNetworkOperator());
        text+=("\n\tgetNeighboringCellInfo:\t" + manager.getNeighboringCellInfo().size());


        List<NeighboringCellInfo> NeighboringList = manager.getNeighboringCellInfo();

        String stringNeighboring = "Neighboring List- Lac : Cid : RSSI\n";
        for(int i=0; i < NeighboringList.size(); i++){

            String dBm;
            int rssi = NeighboringList.get(i).getRssi();
            if(rssi == NeighboringCellInfo.UNKNOWN_RSSI){
                dBm = "Unknown RSSI";
            }else{
                dBm = String.valueOf(-113 + 2 * rssi) + " dBm";
            }

            stringNeighboring = stringNeighboring
                    + String.valueOf(NeighboringList.get(i).getLac()) +" : "
                    + String.valueOf(NeighboringList.get(i).getCid()) +" : "
                    + String.valueOf(NeighboringList.get(i).getPsc()) +" : "
                    + String.valueOf(NeighboringList.get(i).getNetworkType()) +" : "
                    + dBm +"\n";
            text+=(stringNeighboring);
        }


    }
}

}
