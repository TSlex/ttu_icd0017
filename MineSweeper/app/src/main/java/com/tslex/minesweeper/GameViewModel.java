package com.tslex.minesweeper;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class GameViewModel extends ViewModel {

    private final GameActivity activity;
    private final int verticalCount;
    private final int horisontalCount;
    private Game game;

    public GameViewModel(GameActivity activity, int verticalCount, int horisontalCount) {
        this.activity = activity;
        this.verticalCount = verticalCount;
        this.horisontalCount = horisontalCount;

        this.game = new Game(activity, verticalCount, horisontalCount);
        game.initCells();
    }

    public Game getGame() {
        return game;
    }

    public Game restartGame() {
        if (game != null) {
            game = new Game(activity, verticalCount, horisontalCount);
            game.initCells();
        }
        return game;
    }
}

class ModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final GameActivity activity;
    private final int verticalCount;
    private final int horisontalCount;

    public ModelFactory(GameActivity activity, int verticalCount, int horisontalCount) {
        super();
        this.activity = activity;
        this.verticalCount = verticalCount;
        this.horisontalCount = horisontalCount;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass == GameViewModel.class) {
            return (T) new GameViewModel(activity, verticalCount, horisontalCount);
        }
        return null;
    }
}
