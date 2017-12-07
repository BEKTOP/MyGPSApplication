package com.github.a5809909.mygpsapplication.activities;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.github.a5809909.mygpsapplication.R;
import com.github.a5809909.mygpsapplication.model.PhoneState;
import com.github.a5809909.mygpsapplication.sql.DatabaseHelper;

import java.util.ArrayList;
import java.util.Arrays;
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
            for (PhoneState phoneState : phoneStates) {
                Log.d("db", "size:"+phoneStates.size()+" Имя: " + phoneState.getCellId_0() + " email: " + phoneState.getMcc()+"\n");
            }

            databaseHelper.close();
            gv=findViewById(R.id.gridView1);
            mCursor = databaseHelper.getAllItems();

            String[] from = new String[] { databaseHelper.COLUMN_MCC, databaseHelper.COLUMN_LAC };
            int[] to = new int[] { R.id.tv_mcc, R.id.tv_mnc };

            mCursorAd = new SimpleCursorAdapter(this, R.layout.item_grid_view, mCursor, from, to, 0);

            gv.setAdapter(mCursorAd);

        }

}
