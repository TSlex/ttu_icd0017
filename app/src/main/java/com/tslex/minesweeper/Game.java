package com.tslex.minesweeper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.Button;
import android.widget.LinearLayout;

public class Game extends AppCompatActivity {

    //Settings
    private static final int VERTICAL_COUNT = 15;
    private static final int HORISONTAL_COUNT = 10;

    private static final int BUTTON_MARGIN = 2;
    private static final int GAME_FIELD_MARGIN = 10;

    private static final int BOMBS_COUNT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_gui);

//        Initialize game board
        ConstraintLayout gameBoard = findViewById(R.id.gameBoard);

        int orientation = this.getResources().getConfiguration().orientation;

        if (orientation == 1) {

            fillField(true, gameBoard);

        } else {

            fillField(false, gameBoard);
        }
    }

    private void fillField(final boolean isPortraitm, ConstraintLayout gameBoard) {

        gameBoard.post(new Runnable() {

            @Override
            public void run() {
                ConstraintLayout gameBoard = findViewById(R.id.gameBoard);
                LinearLayout gameField = findViewById(R.id.gameField);

                gameField.setOrientation((isPortraitm ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL));

                int buttWidth = (gameBoard.getWidth() - GAME_FIELD_MARGIN * 2) / (isPortraitm ? HORISONTAL_COUNT : VERTICAL_COUNT);
                int buttHeight = (gameBoard.getHeight() - GAME_FIELD_MARGIN * 2) / (isPortraitm ? VERTICAL_COUNT : HORISONTAL_COUNT);

//                int buttSize = Math.min(buttWidth, buttHeight);
                int buttSize = Math.min(buttWidth, buttHeight) - BUTTON_MARGIN * 2;

                Log.d("Main", "height: " + gameBoard.getHeight() + ", width: " + gameBoard.getWidth());

                for (int y = 0; y < VERTICAL_COUNT; y++) {

                    LinearLayout row = new LinearLayout(gameBoard.getContext());
                    row.setOrientation((isPortraitm ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL));

                    for (int x = 0; x < HORISONTAL_COUNT; x++) {

//                        Button button = new Button(new ContextThemeWrapper(gameBoard.getContext(), R.style.FieldCell), null, 0);
                        Button button = (Button) getLayoutInflater().inflate(R.layout.field_cell, null);
                        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(buttSize, buttSize);

                        buttonParams.setMargins(BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN);
                        button.setLayoutParams(buttonParams);
                        row.addView(button);
                    }

                    gameField.addView(row);
                }
            }
        });
    }
}
