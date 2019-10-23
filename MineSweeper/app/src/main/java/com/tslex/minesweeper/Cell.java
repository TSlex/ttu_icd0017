package com.tslex.minesweeper;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class Cell extends androidx.appcompat.widget.AppCompatImageView {

    private int yPosition = -1;
    private int xPosition = -1;

    private CellState state = CellState.UNDEFINED;
    private Game game;

    private boolean isOpened = false;
    private boolean isInspected = false;

    public Cell(Context context) {
        super(context);
    }

    public Cell(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Cell(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (game.isGameOver() && event.getAction() == MotionEvent.ACTION_DOWN){
            Log.d("CELL", "GAME OVER: " + game.getState().name());
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            if (isOpened) {
                inspyOthers(true);
                Log.d("CELL", "! I am ispying others");

            } else if (game.isInspectMode() && !isInspected) {
                inspectMe(true);

                game.decreaseFlagCounter();
                Log.d("CELL", "! I am now inspected");

            } else if (game.isInspectMode() && isInspected) {
                inspectMe(false);

                game.increaseFlagCounter();
                Log.d("CELL", "! Not inspected anymore");
            } else if (!game.isInspectMode() && !isInspected) {
                openCell();
                Log.d("CELL", "! I am opened now");
            }

            game.vibrate();
            game.updateActivity();

        } else if (event.getAction() == MotionEvent.ACTION_UP) {

            if (game.getState() == GameState.NOT_STARTED){
                game.startGame();
            }

            if (isOpened) {
                inspyOthers(false);
            }

            if (game.getState() != GameState.LOSE && game.checkBoard()) {
                game.gameOver(GameState.WIN);
            }
        }

        Log.d("CELL", "isOpened: " + isOpened);
        Log.d("CELL", "isInspected: " + isInspected);
        Log.d("CELL", "isInspectMode: " + game.isInspectMode());

        return super.onTouchEvent(event);
    }

    public void setxPosition(int xPosition) {
        this.xPosition = xPosition;
    }

    public void setyPosition(int yPosition) {
        this.yPosition = yPosition;
    }

    public int getxPosition() {
        return xPosition;
    }

    public int getyPosition() {
        return yPosition;
    }

    public void update() {

        if (isInspected) {
            super.setImageResource(CellState.FLAG.getImageSrc());
        } else if (isOpened) {
            super.setImageResource(state.getImageSrc());
        } else super.setImageResource(CellState.UNDEFINED.getImageSrc());

        if (isOpened && state == CellState.BOMD) {
            super.setBackgroundResource(R.drawable.field_bomb_cell_shape);
        }

        if (isOpened && state != CellState.BOMD && state != CellState.UNDEFINED) {
            super.setBackgroundResource(R.drawable.field_closed_cell_shape);
        }

        if (isOpened && state == CellState.UNDEFINED) {
            super.setBackgroundResource(R.drawable.field_empty_cell_shape);
        }

        Log.d("CELL", "MY STATE IS: " + state.name());
    }

    public void setState(CellState state) {
        this.state = state;
        update();
    }

    public CellState getState() {
        return state;
    }

    public Game getCellGame() {
        return game;
    }

    public void setCellGame(Game game) {
        this.game = game;
    }

    public void openCell() {
        if (!isOpened) {
            isOpened = true;
            inspectMe(false);
            game.openCell(yPosition, xPosition);
            update();
        }
        if (game.isGameOver()){
            return;
        }
        if (state == CellState.BOMD) {
            game.gameOver(GameState.LOSE);
        }
    }

    public void inspectMe(boolean inspect) {
        isInspected = inspect;

        update();
    }

    public void inspyOthers(boolean bool) {
        game.inspy(yPosition, xPosition, bool);
    }

    public void inspyingMe(boolean bool) {
        if (bool) {
            super.setBackgroundResource(R.drawable.field_inspy_cell_shape);
        } else {
            super.setBackgroundResource(R.drawable.field_closed_cell_shape);
        }
    }

    public boolean isOpened() {
        return isOpened;
    }

    public boolean isInspected() {
        return isInspected;
    }

    @Override
    public String toString() {
        return yPosition + ":" + xPosition;
    }
}
