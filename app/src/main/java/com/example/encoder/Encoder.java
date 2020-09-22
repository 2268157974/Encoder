package com.example.encoder;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class Encoder {
    private String TAG = "MY_TEST " + this.getClass().getSimpleName();
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private MediaCodec mMediaCodec;
    private DataSource mDataSource;
    private int count = 0;
    private HandlerThread mTimerHandlerThread;
    private Handler mTimer;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public Encoder(String name, DataSource dataSource, int width, int height, int key) {
        mHandlerThread = new HandlerThread("name");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mDataSource = dataSource;
        mTimerHandlerThread = new HandlerThread("Timer");
        mTimerHandlerThread.start();
        mTimer = new Handler(mTimerHandlerThread.getLooper());
        TAG = TAG + name;
        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 1000);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, key);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

        try {
            mMediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Encoder: " + mHandlerThread.getName() + " createEncoderByType error");
        }
        mMediaCodec.setCallback(mCallback, mHandler);
        mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec.start();
        Log.d(TAG, "Encoder: Start ");
    }

    private MediaCodec.Callback mCallback = new MediaCodec.Callback() {
        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
           try{
               ByteBuffer inputBuffer = codec.getInputBuffer(index);
               byte[] data = mDataSource.getData();
               int len = 0;
               if (data != null) {
                   inputBuffer.clear();
                   inputBuffer.put(data, 0, len);
                   len = data.length;
//                Log.d(TAG, "onInputBufferAvailable: " + len);
               }
               codec.queueInputBuffer(index, 0, len, System.nanoTime() / 1000L, 0);
           }catch (Exception e){
           }
        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
           try{
               count++;
               startTimer();
               codec.releaseOutputBuffer(index, false);
           }catch (Exception e){
           }
        }

        @Override
        public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
            e.printStackTrace();
            Log.d(TAG, "Codec Error: " + e.getMessage());
        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

        }
    };

    private boolean mTimerIsStart;

    private synchronized void startTimer() {
        if (mTimerIsStart) return;
        mTimerIsStart = true;
        Log.d(TAG, "startTimer: " + dateTime());
        mTimer.post(timer);
    }

    private static SimpleDateFormat FORMAT = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    private String dateTime() {
        return FORMAT.format(new Date(System.currentTimeMillis()));
    }

    public void release() {
        mMediaCodec.flush();
        mMediaCodec.stop();
        mHandlerThread.quitSafely();
        mMediaCodec.release();
        mTimer.removeCallbacks(timer);
        mTimerHandlerThread.quitSafely();
        mMediaCodec = null;
    }

    private Runnable timer = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "time " + dateTime() + "______ count " + count);
            count = 0;
            mTimer.postDelayed(this, 1000);
        }
    };
}
