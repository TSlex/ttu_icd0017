package com.tslex.radio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.tslex.radio.adapter.RadioStationAdapter;
import com.tslex.radio.domain.RadioStation;
import com.tslex.radio.domain.StationHistory;
import com.tslex.radio.repo.HistoryRepo;
import com.tslex.radio.repo.RadioRepo;

import java.lang.reflect.Array;
import java.sql.Time;
import java.util.Arrays;
import java.util.Date;

public class RadioUI extends AppCompatActivity {

    private static String TAG = RadioUI.class.getSimpleName();

    private RadioRepo radioRepository;
    private ArrayAdapter radioAdapter;

    private ActivityBroadcastReceiver localReceiver = new ActivityBroadcastReceiver();
    private IntentFilter intentFilter = new IntentFilter();

    private PlayerStatus status = PlayerStatus.PLAYER_STATUS_STOPPED;

    private WebReqHandler handler;

    private Button playButton;
    private Button statisticButton;
    private ImageView stationImage;
    private Spinner stationSpinner;
    private TextView animeTitle;
    private TextView songTitle;
    private ProgressBar progressBar;

    private Animation pulse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radio_ui);

        pulse = AnimationUtils.loadAnimation(this, R.anim.play_button_anim);

        //UI
        playButton = findViewById(R.id.playButton);
        statisticButton = findViewById(R.id.statisticButton);
        stationImage = findViewById(R.id.stationImage);
        stationSpinner = findViewById(R.id.stationSpinner);
        animeTitle = findViewById(R.id.animeTittle);
        songTitle = findViewById(R.id.songTittle);
        progressBar = findViewById(R.id.progressBar);

        progressBar.setVisibility(View.INVISIBLE);

        //db context
        radioRepository = new RadioRepo(this).open();

//        radioAdapter = new ArrayAdapter(this, R.layout.radio_station_spinner_element, radioRepository.getAll());
        radioAdapter = new RadioStationAdapter(this, R.layout.radio_station_spinner_element, radioRepository.getAll());
        radioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stationSpinner.setAdapter(radioAdapter);

        //filters
        intentFilter.addAction(IntentActions.INTENT_PLAYER_PLAYING.getAction());
        intentFilter.addAction(IntentActions.INTENT_PLAYER_BUFFERING.getAction());
        intentFilter.addAction(IntentActions.INTENT_PLAYER_STOPPED.getAction());
        intentFilter.addAction(IntentActions.INTENT_PLAYER_BUFFERING_PROGRESS.getAction());
        intentFilter.addAction(IntentActions.INTENT_META_UPDATE.getAction());
        intentFilter.addAction(IntentActions.INTENT_ANIM_PLAY.getAction());


        //permission for phone calls
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED) {

                String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE};

                requestPermissions(permissions, 999);
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");

        LocalBroadcastManager.getInstance(this).unregisterReceiver(localReceiver);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        LocalBroadcastManager.getInstance(this).registerReceiver(localReceiver, intentFilter);
        updateUI();
    }

    public void playButtonClick(View view) {
        Log.d(TAG, "playButtonClick");

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

    public void openStationHistory(View view){
        Intent history = new Intent(this, StationHistpryUi.class);
        startActivity(history);
    }

    public void addTestStation(View view) {

        //add test stations
        radioRepository.erase();

        RadioStation anison = new RadioStation(
                "Anison.FM",
                "http://anison.fm/status.php?widget=false",
                "http://pool.anison.fm:9000/AniSonFM(128)"
        );

        anison.convertBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.logo_h));

        RadioStation sky = new RadioStation(
                "SKY",
                "http://anison.fm/status.php?widget=false",
                "http://pool.anison.fm:9000/AniSonFM(128)"
        );

        sky.convertBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.logo_h));


        radioRepository.add(anison);
        radioRepository.add(sky);

        //add test songs
        HistoryRepo history = new HistoryRepo(this).open();
        history.erase();

        history.add(new StationHistory(
                "Holodnoy Popoy Prijmis Ko Mne",
                "Sienduk",
                1,
                23,
                new Time(new Date().getTime())
        ));

        history.add(new StationHistory(
                "A On Tebja Lyubil, Skotina",
                "Hzkto",
                1,
                4,
                new Time(new Date().getTime())
        ));

        history.add(new StationHistory(
                "Holodnoy Popoy Prijmis Ko Mne",
                "Sienduk",
                2,
                23,
                new Time(new Date().getTime())
        ));

        history.add(new StationHistory(
                "A On Tebja Lyubil, Skotina",
                "Hzkto",
                2,
                4,
                new Time(new Date().getTime())
        ));

        history.close();
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
                playButton.setText(R.string.play_button_stop);
                playButton.startAnimation(pulse);

                progressBar.setVisibility(View.INVISIBLE);

                animeTitle.setVisibility(View.VISIBLE);
                songTitle.setVisibility(View.VISIBLE);
                break;

            case PLAYER_STATUS_BUFFERING:
                playButton.setText(R.string.play_button_buffering);
                progressBar.setVisibility(View.VISIBLE);
                break;

            case PLAYER_STATUS_STOPPED:
                playButton.setText(R.string.play_button_play);
                playButton.clearAnimation();

                progressBar.setVisibility(View.INVISIBLE);

                animeTitle.setVisibility(View.INVISIBLE);
                songTitle.setVisibility(View.INVISIBLE);
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

        private String TAG = ActivityBroadcastReceiver.class.getSimpleName();

        public ActivityBroadcastReceiver() {
            super();
            Log.d(TAG, "Created");
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Reseived: " + intent.getAction());

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
