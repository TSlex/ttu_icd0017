package com.tslex.minesweeper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;

public class Game extends AppCompatActivity {

    //Settings
    private static final int VERTICAL_COUNT = 15;
    private static final int HORISONTAL_COUNT = 10;

    private static final int BOMBS_COUNT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_gui);

//        Initialize game board
        ConstraintLayout gameBoard = findViewById(R.id.gameBoard);

        gameBoard.post(new Runnable() {

            @Override
            public void run() {
                ConstraintLayout gameBoard = findViewById(R.id.gameBoard);
                LinearLayout gameField = findViewById(R.id.gameField);

                int buttWidth = gameBoard.getWidth() / HORISONTAL_COUNT;
                int buttHeight = gameBoard.getHeight() / VERTICAL_COUNT;
                int buttSize = Math.min(buttWidth, buttHeight);

                Log.d("Main", "height: " + gameBoard.getHeight() + ", width: " + gameBoard.getWidth());

                for (int y = 0; y < VERTICAL_COUNT; y++) {

                    LinearLayout row = new LinearLayout(gameBoard.getContext());
                    row.setOrientation(LinearLayout.HORIZONTAL);

                    for (int x = 0; x < HORISONTAL_COUNT; x++) {

                        Button button = new Button(new ContextThemeWrapper(gameBoard.getContext(), R.style.FieldCell), null, 0);
                        button.setLayoutParams(new LinearLayout.LayoutParams(buttSize, buttSize));
                        row.addView(button);
                    }

                    gameField.addView(row);
                }
            }
        });

    }
}
