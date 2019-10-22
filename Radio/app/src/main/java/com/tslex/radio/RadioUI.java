package com.tslex.radio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RadioUI extends AppCompatActivity {

    private static String TAG = RadioUI.class.getSimpleName();

    private ActivityBroadcastReceiver localReceiver = new ActivityBroadcastReceiver();
    private IntentFilter intentFilter = new IntentFilter();

    private PlayerStatus status = PlayerStatus.PLAYER_STATUS_STOPPED;


    private Button playButton;
    private TextView animeTittle;
    private TextView songTittle;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radio_ui);

        playButton = findViewById(R.id.playButton);
        animeTittle = findViewById(R.id.animeTittle);
        songTittle = findViewById(R.id.songTittle);
        progressBar = findViewById(R.id.progressBar);

        intentFilter.addAction(IntentActions.INTENT_PLAYER_PLAYING.getAction());
        intentFilter.addAction(IntentActions.INTENT_PLAYER_BUFFERING.getAction());
        intentFilter.addAction(IntentActions.INTENT_PLAYER_STOPPED.getAction());
        intentFilter.addAction(IntentActions.INTENT_PLAYER_BUFFERING_PROGRESS.getAction());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        LocalBroadcastManager.getInstance(this).registerReceiver(localReceiver, intentFilter);
    }

    public void buttonClick(View view) {
        Log.d(TAG, "buttonClick");

        switch (status) {
            case PLAYER_STATUS_PLAYING:
                LocalBroadcastManager
                        .getInstance(getApplicationContext())
                        .sendBroadcast(new Intent(IntentActions.INTENT_UI_STOP.getAction()));
                break;
            case PLAYER_STATUS_STOPPED:
                startService(new Intent(this, RadioService.class));
                break;
        }
    }

    private void updateUI() {
        switch (status) {
            case PLAYER_STATUS_PLAYING:
                playButton.setText("STOP");
                break;
            case PLAYER_STATUS_BUFFERING:
                playButton.setText("BUFFERING");
                break;
            case PLAYER_STATUS_STOPPED:
                playButton.setText("PLAY");
                break;
        }
    }

    private class ActivityBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
//            Log.d(TAG, "Reseived: ");

            String action = intent.getAction();

            if (action != null) {

                if (action.equals(IntentActions.INTENT_PLAYER_PLAYING.getAction())) {

                    status = PlayerStatus.PLAYER_STATUS_PLAYING;

                } else if (action.equals(IntentActions.INTENT_PLAYER_BUFFERING.getAction())) {

                    status = PlayerStatus.PLAYER_STATUS_BUFFERING;

                } else if (action.equals(IntentActions.INTENT_PLAYER_STOPPED.getAction())) {

                    status = PlayerStatus.PLAYER_STATUS_STOPPED;

                } else if (action.equals(IntentActions.INTENT_PLAYER_BUFFERING_PROGRESS.getAction())) {

//                    progressBar.setProgress(Integer.parseInt(intent.getStringExtra("progress")));
                    Log.d(TAG, "Reseived: " + intent.getStringExtra("progress"));
                    progressBar.setProgress(100);
                }


                updateUI();

            }

        }
    }
}
