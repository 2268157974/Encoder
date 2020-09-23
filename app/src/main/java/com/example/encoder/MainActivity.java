package com.example.encoder;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MY_TEST ";
    private final String mPath = "/dev/random";
    private List<Encoder> mList;
    private int height = 1080;
    private int width = 1920;
    private int num = 1;
    private int key = 120;
    private DataSource dataSource;
    private final String ACTION_SETTING_DATA = "ACTION_SETTING_DATA";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SETTING_DATA);
        registerReceiver(mReceiver, filter);
        Log.d(TAG, "默认参数：  Height = " + height + " Width = " + width + " Encoder num = " + num + " KEY_FRAME_RATE = " + key);
        mList = new ArrayList<>(num);
        dataSource = new DataSource();
//        dataSource.setData(width, height);
//        startEncoder();

    }


    @Override
    protected void onPause() {
        release();
        Log.d(TAG, "onPause: stop all");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startEncoder() {
        for (int i = 0; i < num; i++) {
            mList.add(new Encoder("Encoder_" + i, dataSource, width, height, key));
        }
    }

    private void release() {
        if (mList != null && mList.size() > 0) {
            for (Encoder encoder : mList) {
                encoder.release();
            }
            mList.clear();
        }

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_SETTING_DATA.equals(intent.getAction())) {
                width = intent.getIntExtra("width", 1920);
                height = intent.getIntExtra("height", 1080);
                key = intent.getIntExtra("key", 120);
                num = intent.getIntExtra("num", 1);

                Log.d(TAG, "Height = " + height + " Width = " + width + " Encoder num = " + num + " KEY_FRAME_RATE = " + key);

                release();
                dataSource.setData(width, height);
                startEncoder();
            }
        }
    };

}