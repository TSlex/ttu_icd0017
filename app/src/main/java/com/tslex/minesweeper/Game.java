package com.tslex.minesweeper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class Game extends AppCompatActivity {

    //Settings
    private static final int VERTICAL_COUNT = 15;
    private static final int HORISONTAL_COUNT = 10;

    private static final int BUTTON_MARGIN = 2;
    private static final int GAME_FIELD_MARGIN = 10;

    private static final int BOMBS_COUNT = 3;

    private static Cell[][] gameCells = new Cell[VERTICAL_COUNT][HORISONTAL_COUNT];

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
                LinearLayout gameField = (LinearLayout) findViewById(R.id.gameField);

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
//                        Cell cell = new Cell(gameBoard.getContext());
                        Cell cell = (Cell) getLayoutInflater().inflate(R.layout.field_cell, null);
                        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(buttSize, buttSize);

                        buttonParams.setMargins(BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN);
                        cell.setLayoutParams(buttonParams);
//                        button.setLayoutParams(buttonParams);

                        cell.setxPosition(x);
                        cell.setyPosition(y);

                        gameCells[y][x] = cell;

                        cell.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d(v.toString(), "hello");
                            }
                        });
                        row.addView(cell);
                    }

                    gameField.addView(row);
                }
            }
        });
    }
}
