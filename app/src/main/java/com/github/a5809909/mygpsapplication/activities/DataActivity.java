package com.github.a5809909.mygpsapplication.activities;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;

import com.github.a5809909.mygpsapplication.R;
import com.github.a5809909.mygpsapplication.model.PhoneState;
import com.github.a5809909.mygpsapplication.sql.DatabaseHelper;

import java.util.List;

public class DataActivity extends Activity {
    private Cursor mCursor;
    private DatabaseHelper databaseHelper;
    private SimpleCursorAdapter mCursorAd;
        GridView gv;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_data);
            databaseHelper = new DatabaseHelper(this);
            List<PhoneState> phoneStates = databaseHelper.getAllPhoneStates();

            databaseHelper.close();
            gv=findViewById(R.id.gridView1);
            mCursor = databaseHelper.getAllItems();

            String[] from = new String[] { databaseHelper.COLUMN_TIME, databaseHelper.COLUMN_MCC, databaseHelper.COLUMN_MNC,
                    databaseHelper.COLUMN_CELL_SIZE, databaseHelper.COLUMN_WIFI_SIZE, databaseHelper.COLUMN_CELL_ID,
                    databaseHelper.COLUMN_LAC,databaseHelper.COLUMN_SIGNAL_STRENGTH,
                    databaseHelper.COLUMN_CELL_INFO, databaseHelper.COLUMN_WIFI_INFO,
                    databaseHelper.COLUMN_RADIO_TYPE};
            int[] to = new int[] { R.id.tv_time,R.id.tv_mcc, R.id.tv_mnc,R.id.tv_cell_size,R.id.tv_wifi_size,
                    R.id.tv_cell_id,R.id.tv_lac,R.id.tv_signal_strength,R.id.tv_cell_info,
                    R.id.tv_wifi_info, R.id.tv_radio_type};

            mCursorAd = new SimpleCursorAdapter(this, R.layout.item_grid_view, mCursor, from, to, 0);

            gv.setAdapter(mCursorAd);

        }

}
