package com.tslex.radio;

import androidx.annotation.NonNull;
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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.Arrays;

public class RadioUI extends AppCompatActivity {

    private static String TAG = RadioUI.class.getSimpleName();

    private ActivityBroadcastReceiver localReceiver = new ActivityBroadcastReceiver();
    private IntentFilter intentFilter = new IntentFilter();

    private PlayerStatus status = PlayerStatus.PLAYER_STATUS_STOPPED;

    private WebReqHandler handler;

    private Button playButton;
    private TextView animeTitle;
    private TextView songTitle;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radio_ui);

        playButton = findViewById(R.id.playButton);
        animeTitle = findViewById(R.id.animeTittle);
        songTitle = findViewById(R.id.songTittle);
        progressBar = findViewById(R.id.progressBar);

        progressBar.setVisibility(View.INVISIBLE);

        intentFilter.addAction(IntentActions.INTENT_PLAYER_PLAYING.getAction());
        intentFilter.addAction(IntentActions.INTENT_PLAYER_BUFFERING.getAction());
        intentFilter.addAction(IntentActions.INTENT_PLAYER_STOPPED.getAction());
        intentFilter.addAction(IntentActions.INTENT_PLAYER_BUFFERING_PROGRESS.getAction());
        intentFilter.addAction(IntentActions.INTENT_META_UPDATE.getAction());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        LocalBroadcastManager.getInstance(this).registerReceiver(localReceiver, intentFilter);
        updateUI();
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
                //start stream
                startService(new Intent(this, RadioService.class));
            break;
        }
    }

    private void doMetaRequest(){
        handler = WebReqHandler.getInstance(this);
        handler.getRequestQueue();

        StringRequest request = new StringRequest(
                Request.Method.GET,
                URLS.ANISON_META.getUrl(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        String[] raw = response.split("<[^<>]*>");
                        String animeTitleText = raw[raw.length - 3].trim();
                        String songTitleText = raw[raw.length - 2].split(";")[1].trim();

                        Log.d(TAG, Arrays.toString(raw));
                        Log.d(TAG, "Anime Title:" + animeTitleText);
                        Log.d(TAG, "Song Title:" + songTitleText);

                        animeTitle.setText(animeTitleText);
                        songTitle.setText(songTitleText);
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );

        handler.addToRequestQueue(request);
    }

    private void updateUI() {
        switch (status) {
            case PLAYER_STATUS_PLAYING:
                playButton.setText("STOP");
                progressBar.setVisibility(View.INVISIBLE);
                break;
            case PLAYER_STATUS_BUFFERING:
                playButton.setText("BUFFERING");
                progressBar.setVisibility(View.VISIBLE);
                break;
            case PLAYER_STATUS_STOPPED:
                playButton.setText("PLAY");
                progressBar.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

//        Log.d(TAG, "onRestoreInstanceState");
//        outState.putInt("test", 1);

        outState.putSerializable("status", status);

        outState.putString("animeTitle", String.valueOf(animeTitle.getText()));
        outState.putSerializable("songTitle", String.valueOf(songTitle.getText()));


        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {

//        Log.d(TAG, "onRestoreInstanceState: " + savedInstanceState.containsKey("test"));

        status = (PlayerStatus) savedInstanceState.getSerializable("status");

        animeTitle.setText(savedInstanceState.getString("animeTitle"));
        songTitle.setText(savedInstanceState.getString("songTitle"));

        super.onRestoreInstanceState(savedInstanceState);
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

                } else if (action.equals(IntentActions.INTENT_META_UPDATE.getAction())) {

                    doMetaRequest();
                }

                updateUI();
            }
        }
    }
}
