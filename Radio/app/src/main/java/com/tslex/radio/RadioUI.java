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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class RadioUI extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static String TAG = RadioUI.class.getSimpleName();

    private RadioRepo radioRepository;
    private ArrayAdapter radioAdapter;
    private RadioStation currentStation;

    private String currentSongTittle;
    private String currentArtistTitle;

    private ActivityBroadcastReceiver localReceiver = new ActivityBroadcastReceiver();
    private IntentFilter intentFilter = new IntentFilter();

    private PlayerStatus status = PlayerStatus.PLAYER_STATUS_STOPPED;

    private WebReqHandler handler;

    private Button playButton;
    private Button statisticButton;
    private ImageView stationImage;
    private Spinner stationSpinner;
    private TextView artistTitle;
    private TextView songTitle;
    private ProgressBar progressBar;

    private Animation pulse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radio_ui);

        Log.d(TAG, "onCreate: connect animation");
        pulse = AnimationUtils.loadAnimation(this, R.anim.play_button_anim);

        //UI
        Log.d(TAG, "onCreate: connect objects");
        playButton = findViewById(R.id.playButton);
        statisticButton = findViewById(R.id.statisticButton);
        stationImage = findViewById(R.id.stationImage);
        stationSpinner = findViewById(R.id.stationSpinner);
        artistTitle = findViewById(R.id.animeTittle);
        songTitle = findViewById(R.id.songTittle);
        progressBar = findViewById(R.id.progressBar);

        progressBar.setVisibility(View.INVISIBLE);

        //db context
        Log.d(TAG, "onCreate: open radio repo");
        radioRepository = new RadioRepo(this).open();

        Log.d(TAG, "onCreate: create radio station spinner");
        radioAdapter = new RadioStationAdapter(this, R.layout.radio_station_spinner_element, radioRepository.getAll());
        radioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stationSpinner.setAdapter(radioAdapter);
        stationSpinner.setOnItemSelectedListener(this);

        Log.d(TAG, "onCreate: get current station");
        if (currentStation == null) {
            currentStation = ((RadioStation) stationSpinner.getSelectedItem());
        } else {
            stationSpinner.setSelection(currentStation.getId() - 1);
        }

//        //station may still be null
//        if (currentStation != null) {
//            Log.d(TAG, "onCreate: get last song for this station");
//            getLastSong();
//        }

        if (stationSpinner.getSelectedItem() != null) {
            stationImage.setImageBitmap(((RadioStation) stationSpinner.getSelectedItem()).getImage());
        }

        //filters
        Log.d(TAG, "onCreate: add intent filters");
        intentFilter.addAction(IntentActions.INTENT_PLAYER_PLAYING.getAction());
        intentFilter.addAction(IntentActions.INTENT_PLAYER_BUFFERING.getAction());
        intentFilter.addAction(IntentActions.INTENT_PLAYER_STOPPED.getAction());
        intentFilter.addAction(IntentActions.INTENT_PLAYER_BUFFERING_PROGRESS.getAction());
        intentFilter.addAction(IntentActions.INTENT_META_UPDATE.getAction());
        intentFilter.addAction(IntentActions.INTENT_ANIM_PLAY.getAction());


        //permission for phone calls
        Log.d(TAG, "onCreate: request permission if needed");
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
        Log.d(TAG, "onDestroy: unregister broadcast manager");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localReceiver);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: unregister broadcast manager");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: send player update request");
        LocalBroadcastManager
                .getInstance(getApplicationContext())
                .sendBroadcast(new Intent(IntentActions.INTENT_PLAYER_UPDATE.getAction()));

        Log.d(TAG, "onResume: register local broadcast manager");
        LocalBroadcastManager.getInstance(this).registerReceiver(localReceiver, intentFilter);
        updateUI();
    }

    public void playButtonClick(View view) {
        switch (status) {
            case PLAYER_STATUS_PLAYING:
                Log.d(TAG, "playButtonClick: require player to stop");
                LocalBroadcastManager
                        .getInstance(getApplicationContext())
                        .sendBroadcast(new Intent(IntentActions.INTENT_UI_STOP.getAction()));
                break;
            case PLAYER_STATUS_STOPPED:
                //start stream
                Log.d(TAG, "playButtonClick: require player to start");
                if (currentStation == null) return;
                getLastSong();
                Intent service = new Intent(this, RadioService.class);
                service.putExtra("station_id", currentStation.getId());
                startService(service);
                break;
        }
    }

    public void openStationHistory(View view) {
        Log.d(TAG, "openStationHistory: open statistics");
        Intent history = new Intent(this, StationHistpryUi.class);
        history.putExtra("station_id", currentStation.getId());
        startActivity(history);
    }

    public void addTestStation(View view) {

        //add test stations
        radioRepository.erase();

        RadioStation anison = new RadioStation(
                "Anison.FM",
                "http://anison.fm/status.php?widget=false",
                "http://sky.babahhcdn.com/SKY",
//                "http://pool.anison.fm:9000/AniSonFM(128)",
                "\\{.+:\\s+|<[^<>]*>( — )?( &#151; )?(\"\\})?"
        );

        anison.convertBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.anison_fm_logo));

        RadioStation sky = new RadioStation(
                "SKY",
                "http://dad.akaver.com/api/SongTitles/SKY",
                "http://sky.babahhcdn.com/SKY",
                ".+\"Count\":0,\"Artist\":\"|\",\"Title\":\"|\",\"IsSkippable\".+"
        );

        sky.convertBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.sky_radio_logo));


        radioRepository.add(anison);
        radioRepository.add(sky);

        //add test songs
//        HistoryRepo history = new HistoryRepo(this).open();
//        history.erase();
//
//        history.add(new StationHistory(
//                "Holodnoy Popoy Prijmis Ko Mne",
//                "Sienduk",
//                1,
//                23,
//                new Time(new Date().getTime())
//        ));
//
//        history.add(new StationHistory(
//                "A On Tebja Lyubil, Skotina",
//                "Hzkto",
//                1,
//                4,
//                new Time(new Date().getTime())
//        ));
//
//        history.add(new StationHistory(
//                "Wtf? Go To HELL!!",
//                "Akaver",
//                2,
//                65,
//                new Time(new Date().getTime())
//        ));
//
//        history.add(new StationHistory(
//                "V Bratstve Sila",
//                "Alkeze_xXx_Alkoze",
//                2,
//                4,
//                new Time(new Date().getTime())
//        ));
//
//        history.close();
    }

    private void doMetaRequest() {
        Log.d(TAG, "doMetaRequest: collecting meta");
        handler = WebReqHandler.getInstance(this);
        handler.getRequestQueue();

        Log.d(TAG, "doMetaRequest: create request");
        StringRequest request = new StringRequest(
                Request.Method.GET,
                currentStation.getStationMeta(),
//                URLS.ANISON_META.getUrl(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        response = UnicEncoder.encode(response);
                        Log.e(TAG, "onResponse: got response" + response);

//                        String[] raw = response.split("\\{.+:\\s+|<[^<>]*>( — )?( &#151; )?(\"\\})?");
                        String[] raw = response.split(currentStation.getStationMetaRegex());
                        List<String> formated = new ArrayList<>();

                        for (String element : raw) {
                            if (!element.equals("")) {
                                formated.add(element);
                            }
                        }

                        String artist = "";
                        String song = "";

                        if (formated.size() > 1) {
                            artist = formated.get(0);
                            song = formated.get(1);
                        }

                        Log.d(TAG, Arrays.toString(raw));
                        System.out.println("Artist:" + artist);
                        System.out.println("Song:" + song);
                        Log.d(TAG, "last artist title: " + currentArtistTitle);
                        Log.d(TAG, "last song title: " + currentSongTittle);

                        if (currentArtistTitle == null | currentSongTittle == null) {
                            Log.d(TAG, "onResponseU: if there's no song playing before, add new one");
                            currentArtistTitle = artist;
                            currentSongTittle = song;
                            updateHistory();
                        } else if (!currentArtistTitle.equals(artist) | !currentSongTittle.equals(song)) {
                            Log.d(TAG, "onResponseU: if song changes -> update");
                            currentArtistTitle = artist;
                            currentSongTittle = song;
                            updateHistory();
                        }
//
                        Log.d(TAG, "onResponse: update UI");
                        artistTitle.setText(artist.replaceAll("\\s+", " "));
                        songTitle.setText(song.replaceAll("\\s+", " "));
                    }
                },
                new Response.ErrorListener() {
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
                Log.d(TAG, "updateUI: update to PLAYING-state");
                playButton.setText(R.string.play_button_stop);
                playButton.startAnimation(pulse);

                progressBar.setVisibility(View.INVISIBLE);

                artistTitle.setVisibility(View.VISIBLE);
                songTitle.setVisibility(View.VISIBLE);
                break;

            case PLAYER_STATUS_BUFFERING:
                Log.d(TAG, "updateUI: update to BUFFERING-state");
                playButton.setText(R.string.play_button_buffering);
                progressBar.setVisibility(View.VISIBLE);
                break;

            case PLAYER_STATUS_STOPPED:
                Log.d(TAG, "updateUI: update to PLAY-READY-state");
                playButton.setText(R.string.play_button_play);
                playButton.clearAnimation();

                progressBar.setVisibility(View.INVISIBLE);

                artistTitle.setText("");
                songTitle.setText("");
                currentArtistTitle = "";
                currentSongTittle = "";

                artistTitle.setVisibility(View.INVISIBLE);
                songTitle.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void updateHistory() {

        Log.d(TAG, "updateHistory: open repo");
        HistoryRepo historyRepository = new HistoryRepo(this).open();

        Log.d(TAG, "updateHistory: get song if exist");
        StationHistory history = historyRepository.getOne(currentStation.getId(), currentSongTittle, currentArtistTitle);

        if (history == null) {
            Log.d(TAG, "updateHistory: no found song, add new one to history");
            historyRepository.add(new StationHistory(
                    currentSongTittle,
                    currentArtistTitle,
                    currentStation.getId(),
                    1,
                    new java.sql.Timestamp(new Date().getTime())
            ));
        } else {
            Log.d(TAG, "updateHistory: song was found, increasing play count");
            Log.d(TAG, "updateHistory: initial count -> " + history.getPlayedCount());
            history.setPlayedCount(history.getPlayedCount() + 1);
            Log.d(TAG, "updateHistory: new  count -> " + history.getPlayedCount());
            history.setLastPlayedTime(new java.sql.Timestamp(new Date().getTime()));
            historyRepository.updateRecord(history);
        }

        Log.d(TAG, "updateHistory: closing repo");
        historyRepository.close();
    }

    private void getLastSong() {
        Log.d(TAG, "getLastSong: open repo");
        HistoryRepo historyRepository = new HistoryRepo(this).open();
        Log.d(TAG, "getLastSong: get last one if exist");
        StationHistory lastSong = historyRepository.getLast(currentStation.getId());

        if (lastSong == null) {
            Log.d(TAG, "getLastSong: no song found in history for this station");
        } else {
            Log.d(TAG, "getLastSong: found last");
            currentArtistTitle = lastSong.getArtistName();
            currentSongTittle = lastSong.getSongName();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");

        outState.putSerializable("status", status);
        outState.putString("artistTitle", String.valueOf(artistTitle.getText()));
        outState.putString("songTitle", String.valueOf(songTitle.getText()));
        outState.putInt("current_station_index", stationSpinner.getSelectedItemPosition());

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {

        Log.d(TAG, "onRestoreInstanceState");

        status = (PlayerStatus) savedInstanceState.getSerializable("status");

        artistTitle.setText(savedInstanceState.getString("artistTitle"));
        songTitle.setText(savedInstanceState.getString("songTitle"));
        stationSpinner.setSelection(savedInstanceState.getInt("current_station_index"));

        super.onRestoreInstanceState(savedInstanceState);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemSelected");
        LocalBroadcastManager
                .getInstance(getApplicationContext())
                .sendBroadcast(new Intent(IntentActions.INTENT_UI_STOP.getAction()));
        currentStation = ((RadioStation) parent.getItemAtPosition(position));
        stationImage.setImageBitmap(currentStation.getStationBitmap());
//        getLastSong();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        parent.setSelection(0);
    }

    private class ActivityBroadcastReceiver extends BroadcastReceiver {

        private String TAG = ActivityBroadcastReceiver.class.getSimpleName();

        public ActivityBroadcastReceiver() {
            super();
            Log.d(TAG, "ActivityBroadcastReceiver: created");
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received: " + intent.getAction());

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

                Log.d(TAG, "onReceive: updating UI");
                updateUI();
            }
        }
    }
}
