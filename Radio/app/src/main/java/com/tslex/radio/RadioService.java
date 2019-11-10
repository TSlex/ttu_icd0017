package com.tslex.radio;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RadioService extends Service implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener {

    private static String TAG = RadioService.class.getSimpleName();

//    private final String streamPath = "http://pool.anison.fm:9000/AniSonFM(320)";
    private final String streamPath = URLS.ANISON_STREAM_128.getUrl();
//    private final String streamPath = "http://sky.babahhcdn.com/rrap";
//    private final String streamPath = "http://airspectrum.cdnstream1.com:8114/1648_128";

    private ScheduledExecutorService metaExecutorService;
    private ScheduledExecutorService animationExecutorService;

    private IntentFilter intentFilter = new IntentFilter();
    private ServiceBroadcastReceiver localReceiver = new ServiceBroadcastReceiver();

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
        intentFilter.addAction(IntentActions.INTENT_PLAYER_MUTE.getAction());
        intentFilter.addAction(IntentActions.INTENT_PLAYER_UNMUTE.getAction());
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

//        metaExecutorService.shutdown();
//        animationExecutorService.shutdown();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(localReceiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(new Intent(IntentActions.INTENT_PLAYER_BUFFERING.getAction()));

        initPlayer();

        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(localReceiver, intentFilter);

        startMetaUpdate(); //TODO: for testing only!!!


        player.prepareAsync();

        return START_STICKY;
    }

    private void startAnimationPlayer(){
        animationExecutorService = Executors.newScheduledThreadPool(1);

        metaExecutorService.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        LocalBroadcastManager.getInstance(getApplicationContext())
                                .sendBroadcast(new Intent(IntentActions.INTENT_ANIM_PLAY.getAction()));
                    }
                },
                0,
                500,
                TimeUnit.MILLISECONDS
        );
    }

    private void startMetaUpdate(){
        metaExecutorService = Executors.newScheduledThreadPool(1);

        metaExecutorService.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Meta is Updating");
                        LocalBroadcastManager.getInstance(getApplicationContext())
                                .sendBroadcast(new Intent(IntentActions.INTENT_META_UPDATE.getAction()));
                    }
                },
                0,
                2,
                TimeUnit.SECONDS
        );
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

//        startMetaUpdate();
        startAnimationPlayer();

        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(new Intent(IntentActions.INTENT_PLAYER_PLAYING.getAction()));
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        Log.d(TAG, "progress begins: " + i);
    }

    private class ServiceBroadcastReceiver extends BroadcastReceiver {

        private String TAG = ServiceBroadcastReceiver.class.getSimpleName();

        public ServiceBroadcastReceiver() {
            super();
            Log.d(TAG, "Created");
        }


        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Reseived: " + intent.getAction());

            String action = intent.getAction();

            if (action != null) {
                if (action.equals(IntentActions.INTENT_UI_STOP.getAction())){
                    Log.d(TAG, "STOP");
                    player.stop();
                    metaExecutorService.shutdown();
                    LocalBroadcastManager.getInstance(getApplicationContext())
                            .sendBroadcast(new Intent(IntentActions.INTENT_PLAYER_STOPPED.getAction()));
                    onDestroy();
                }
                else if (action.equals(IntentActions.INTENT_PLAYER_MUTE.getAction())){
                    Log.d(TAG, "MUTE");
                    player.setVolume(0,0);
                }
                else if (action.equals(IntentActions.INTENT_PLAYER_UNMUTE.getAction())){
                    Log.d(TAG, "UNMUTE");

                    new Runnable(){
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            player.setVolume(1,1);
                        }
                    }.run();
//                    player.setVolume(1,1);
                }
            }
        }
    }
}
