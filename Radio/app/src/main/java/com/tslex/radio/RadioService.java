package com.tslex.radio;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class RadioService extends Service implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener {

    private static String TAG = RadioUI.class.getSimpleName();

//    private final String streamPath = "http://pool.anison.fm:9000/AniSonFM(320)";
//    private final String streamPath = "http://sky.babahhcdn.com/rrap";
    private final String streamPath = "http://airspectrum.cdnstream1.com:8114/1648_128";

    private ServiceBroadcastReceiver localReceiver = new ServiceBroadcastReceiver();
    private IntentFilter intentFilter = new IntentFilter();

    private MediaPlayer player;
//    private MediaPlayer player = new MediaPlayer();
//    private MediaPlayer player = MediaPlayer.create(this, R.raw.test_song);

    private void initPlayer(){

//        player.reset();
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);


        player.setOnBufferingUpdateListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setOnPreparedListener(this);


        try {
            player.setDataSource(streamPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        WifiManager.WifiLock mWifiLock = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE))
//                .createWifiLock(WifiManager.WIFI_MODE_FULL, "Media Player Wi-Fi Lock");
//        mWifiLock.acquire();


//        player.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
//
//            @Override
//            public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
//                Log.d(TAG, "progress begins");
//
//                Intent progressIntent = new Intent(IntentActions.INTENT_PLAYER_BUFFERING_PROGRESS.getAction());
//                progressIntent.putExtra("progress", mediaPlayer.getCurrentPosition());
//
//                LocalBroadcastManager.getInstance(getApplicationContext())
//                        .sendBroadcast(progressIntent);
//            }
//        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

//        initPlayer();

        intentFilter.addAction(IntentActions.INTENT_UI_STOP.getAction());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(new Intent(IntentActions.INTENT_PLAYER_BUFFERING.getAction()));

        initPlayer();

        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(localReceiver, intentFilter);

        player.prepareAsync();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "on Bind");

        return null;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onCompletion");

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Log.d(TAG, "onError");

        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onPrepared");

        player.start();

        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(new Intent(IntentActions.INTENT_PLAYER_PLAYING.getAction()));
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        Log.d(TAG, "progress begins: " + i);
    }

    private class ServiceBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Reseived: ");

            String action = intent.getAction();

            if (action != null && action.equals(IntentActions.INTENT_UI_STOP.getAction())){
                player.stop();
                LocalBroadcastManager.getInstance(getApplicationContext())
                        .sendBroadcast(new Intent(IntentActions.INTENT_PLAYER_STOPPED.getAction()));
            }
        }
    }
}
