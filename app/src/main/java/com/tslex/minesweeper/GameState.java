package com.tslex.minesweeper;

public enum GameState {
    NOT_STARTED(R.drawable.state_default),
    STARTED(R.drawable.state_default),
    WIN(R.drawable.state_win),
    LOSE(R.drawable.state_lose);

    private int imageSrc;

    GameState(int src) {
        this.imageSrc = src;
    }

    public int getImageSrc() {
        return imageSrc;
    }
}
