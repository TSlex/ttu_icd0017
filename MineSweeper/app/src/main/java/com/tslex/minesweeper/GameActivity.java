package com.tslex.minesweeper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GameActivity extends AppCompatActivity {

    //Settings
    private static final int VERTICAL_COUNT = 15;
    private static final int HORISONTAL_COUNT = 10;

    private static final int BUTTON_MARGIN = 2;
    private static final int GAME_FIELD_MARGIN = 10;

    private static final int BOMBS_COUNT = 3;

    private UIBroadcastReceiver localReceiver = new UIBroadcastReceiver();
    private IntentFilter intentFilter = new IntentFilter();

//    private static Cell[][] gameCells = new Cell[VERTICAL_COUNT][HORISONTAL_COUNT];

    Handler handler;

    private GameViewModel gameViewModel;
    private Game game = null;

    private boolean isPortrait;

    private ConstraintLayout gameBoard;

    private ImageButton restartButton;
    private ImageButton inspectButton;
    private ImageButton settingsButton;

    private TextView flagsCount;
    private TextView timer;

    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_gui);

        Log.d("main", "onCreate");

        intentFilter.addAction("ui.update");

        restartButton = findViewById(R.id.restartBtn);
        inspectButton = findViewById(R.id.inspectButton);
        settingsButton = findViewById(R.id.settingsButton);

        flagsCount = findViewById(R.id.flagsCount);
        timer = findViewById(R.id.timer);

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                update();
                return true;
            }
        });


        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                game.startTimer();
            }
        });


        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                game = gameViewModel.restartGame();

                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

        inspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                game.changeInspectMode();
                inspectButton.setImageResource(game.isInspectMode() ? R.drawable.flag : R.drawable.bomb);
            }
        });


//        Intent intent = new Intent(this, GameService.class);
//
//        startService();

        this.vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        this.gameViewModel = ViewModelProviders.of(this, new ModelFactory(this, VERTICAL_COUNT, HORISONTAL_COUNT)).get(GameViewModel.class);

        this.game = gameViewModel.getGame();

//        ImageButton imageButton = (ImageButton) findViewById(R.id.inspectButton);
//        imageButton.setImageResource(game.isInspectMode() ? R.drawable.flag : R.drawable.bomb);



//        if (game == null && savedInstanceState != null && savedInstanceState.containsKey("game")){
//
////            Log.d("loaded", ((Game) Objects.requireNonNull(savedInstanceState.getParcelable("game")).getCell());
////            game = savedInstanceState.getParcelable("game");
//        }
//        else {
//            game = new Game(this, VERTICAL_COUNT, HORISONTAL_COUNT);
//            game.initCells();
//        }

//        Initialize game board
        this.gameBoard = findViewById(R.id.gameBoard);

        // Remove test field for examination
        findViewById(R.id.testField).setVisibility(View.GONE);

        int orientation = this.getResources().getConfiguration().orientation;

        this.isPortrait = orientation == 1;

        fillField(isPortrait, gameBoard);
        update();

    }

    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(localReceiver, intentFilter);
        super.onResume();
    }

    public void update(){

        game.updateInstance(this);

        restartButton.setImageResource(game.getState().getImageSrc());

        inspectButton.setImageResource(game.isInspectMode() ? R.drawable.flag : R.drawable.bomb);

        flagsCount.setText(formatFlagsCounter(game.getFlagsCount()));

        timer.setText(formatTimer(game.getTimer()));

    }

    private void fillField(final boolean isPortrait, ConstraintLayout gameBoard) {

        gameBoard.post(new Runnable() {

            @Override
            public void run() {
                ConstraintLayout gameBoard = findViewById(R.id.gameBoard);
                LinearLayout gameField = findViewById(R.id.gameField);

                gameField.setOrientation((isPortrait ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL));

                int buttWidth = (gameBoard.getWidth() - GAME_FIELD_MARGIN * 2) / (isPortrait ? HORISONTAL_COUNT : VERTICAL_COUNT);
                int buttHeight = (gameBoard.getHeight() - GAME_FIELD_MARGIN * 2) / (isPortrait ? VERTICAL_COUNT : HORISONTAL_COUNT);

//                int buttSize = Math.min(buttWidth, buttHeight);
                int buttSize = Math.min(buttWidth, buttHeight) - BUTTON_MARGIN * 2;

                int xMin = isPortrait ? 0 : HORISONTAL_COUNT - 1;
                int xMax = isPortrait ? HORISONTAL_COUNT : 0 - 1;
                int xChanger = isPortrait ? 1 : -1;


                Log.d("Main", "height: " + gameBoard.getHeight() + ", width: " + gameBoard.getWidth());

                for (int y = 0; y < VERTICAL_COUNT; y++) {

                    LinearLayout row = new LinearLayout(gameBoard.getContext());

                    Log.d("Main", "Created row: " + y);

                    row.setOrientation((isPortrait ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL));

                    Log.d("Main", "Row: " + y + " orientation is now - " + row.getOrientation());

                    for (int x = xMin; compare(isPortrait, x, xMax); x+=xChanger) {
//                    for (int x = 0; x < HORISONTAL_COUNT; x++) {

//                        Cell cell = (Cell) getLayoutInflater().inflate(R.layout.field_cell, null);
                        Cell cell = game.getCell(y, x);

                        Log.d("Main", "Assigned cell (" + y + ", " + x + ")");

                        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(buttSize, buttSize);

                        buttonParams.setMargins(BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN);
                        cell.setLayoutParams(buttonParams);

                        Log.d("Main", "Assigned cell params");
//                        button.setLayoutParams(buttonParams);

//                        cell.setxPosition(x);
//                        cell.setyPosition(y);
//
//                        cell.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                Log.d(v.toString(), "hello");
//                            }
//                        });
                        if(cell.getParent() != null){
                            ((ViewGroup) cell.getParent()).removeView(cell);
                        }

                        row.addView(cell);

                        Log.d("Main", "added cell to row");
                    }

                    gameField.addView(row);

                    Log.d("Main", "added row to gameField");
                }
            }
        });
    }

    public void vibrate(){
        vibrator.vibrate(20);
    }

    public boolean compare(boolean isPortrait,int a, int b){
        return isPortrait ? a < b : a > b;
    }

    public String formatFlagsCounter(int count){
        String raw = String.valueOf(Math.abs(count) <= 99 ? count : count < 0 ? "=99" : "^99");
        String result = "";
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < 3 - raw.length(); i++){
            builder.append("0");
        }

        builder.append(raw);

        result = builder.toString();

        if (!isPortrait){
            return TextUtils.join("\n", TextUtils.split(result.trim(), "")).trim();
        }

        return result;
    }

    public String formatTimer(int timer){
        int raw = Math.abs(timer) <= 5999 ? timer : 5999;

        int seconds = raw % 60;
        int minuts = (raw - seconds) / 60;

        String minutsPart = minuts == 0 ? "00" : minuts > 9 ? String.valueOf(minuts) : "0" + minuts;
        String secondsPart = seconds == 0 ? "00" : seconds > 9 ? String.valueOf(seconds) : "0" + seconds;

        return minutsPart + ":" + secondsPart;
    }

    public class UIBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (action != null) {

                if (action.equals("ui.update")) {
                    update();
                }
            }
        }
    }
}
