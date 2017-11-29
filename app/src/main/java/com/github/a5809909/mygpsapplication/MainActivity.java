package com.github.a5809909.mygpsapplication;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Random;

public class MainActivity extends Activity
{
    LocationEstimator myEstimator = new LocationEstimator(this);

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        final Random mRand = new Random();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button btn = (Button) findViewById(R.id.button1);

        btn.setOnClickListener(
            new Button.OnClickListener() {
                @Override public void onClick(View arg0) {
//                    btn.setText(String.valueOf(mRand.nextInt(200)));
                    btn.setText(myEstimator.toString());
                }
            }
        );
    }
}
