package com.example.encoder;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;

public class DataSource {
    private final String TAG = "MY_TEST " + this.getClass().getSimpleName();
    HandlerThread mHandlerThread;
    Handler mHandler;
    private final String mPath = "/dev/random";
    private int mData ;
    private ArrayBlockingQueue<byte[]> mDataSource = new ArrayBlockingQueue<byte[]>(1024);

    public DataSource() {
        mHandlerThread = new HandlerThread("Data source");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        Log.d(TAG, "DataSource: data num " + mData);
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                readData();
//            }
//        });
    }

    private void readData() {
        String command = "cat " + mPath;
        try {
            Process process = Runtime.getRuntime().exec(command);
            InputStream ips = new BufferedInputStream(process.getInputStream());
            byte[] bytes = new byte[mData];
            int len = ips.read(bytes);
            Log.d(TAG, "readData: Start");
            while (len > 0) {
                mDataSource.put(bytes);
                len = ips.read(bytes);
            }
            ips.close();
            process.waitFor();
            Log.d(TAG, "readData: Finish");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Log.d(TAG, "readData: error " + e.getMessage());
        }
    }

    public void setData(int width,int height){
        mData = (int) (width * height * 8 + width * height * 0.25 * 8 * 2) / 8;
    }

    public synchronized byte[] getData() {
        return new byte[mData];
    }

}
