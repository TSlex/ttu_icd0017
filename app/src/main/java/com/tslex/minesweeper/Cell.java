package com.tslex.minesweeper;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class Cell extends androidx.appcompat.widget.AppCompatImageView {

    private int yPosition = -1;
    private int xPosition = -1;
    private CellState state = CellState.UNDEFINED;
    private boolean isOpened = false;
    private Game game;

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

        if (event.getAction() == MotionEvent.ACTION_DOWN){
            game.vibrate();
            openCell();
//            Log.d(toString(), "hello");
        }

        else if (event.getAction() == MotionEvent.ACTION_UP){
            inspyOthers(false);
        }

//        Log.d("Touched", "onTouchEvent: " + event.getAction());

//        Log.d("TMP", String.valueOf(tmp));

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
        if (isOpened) {
            super.setImageResource(state.getImageSrc());
        } else super.setImageResource(CellState.UNDEFINED.getImageSrc());

        if (isOpened && state == CellState.BOMD){
            super.setBackgroundResource(R.drawable.field_bomb_cell_shape);
        }

        if (isOpened && state == CellState.UNDEFINED){
            super.setBackgroundResource(R.drawable.field_empty_cell_shape);
        }
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

    public void openCell(){
        if (!isOpened) {
            isOpened = true;
            game.openCell(yPosition, xPosition);
            update();
        }
        else
            inspyOthers(true);
    }

    public void inspyOthers(boolean bool){
        game.inspy(yPosition, xPosition, bool);
    }

    public void inspyingMe(boolean bool){
        if (bool){
            super.setBackgroundResource(R.drawable.field_inspy_cell_shape);
        }
        else {
            super.setBackgroundResource(R.drawable.field_closed_cell_shape);
        }
    }

    public boolean isOpened() {
        return isOpened;
    }

    @Override
    public String toString() {
        return yPosition + ":" + xPosition;
    }
}
