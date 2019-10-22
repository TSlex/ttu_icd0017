package com.tslex.minesweeper;

import android.util.Log;

public enum CellState {

    BOMD(R.drawable.bomb, -1),
    TRIGGERED_BOMD (R.drawable.bomb, -1),
    FLAG(R.drawable.flag, -1),
    NUM_1(R.drawable.num_1, 1),
    NUM_2(R.drawable.num_2, 2),
    NUM_3(R.drawable.num_3, 3),
    NUM_4(R.drawable.num_4, 4),
    NUM_5(R.drawable.num_5, 5),
    NUM_6(R.drawable.num_6, 6),
    NUM_7(R.drawable.num_7, 7),
    NUM_8(R.drawable.num_8, 8),
    UNDEFINED(R.drawable.undefined, -1);

    private int imageSrc;
    private int number;

    CellState(int src, int number) {
        this.imageSrc = src;
        this.number = number;
    }

    public int getImageSrc() {
        Log.d("Request state", String.valueOf(imageSrc));
        return imageSrc;
    }

    public static CellState getStateByNumber(int number) {
        for (CellState value : CellState.values()) {
            if (value.number == number) {
                return value;
            }
        }
        return CellState.UNDEFINED;
    }
}
