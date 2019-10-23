package com.tslex.minesweeper

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class Game(var instance: GameActivity, val VERTICAL_COUNT: Int, val HORISONTAL_COUNT: Int) {

    private var TimerExecutorService: ScheduledExecutorService? = null

    private var bombsCount = calculateBombCount()

    var flagsCount = bombsCount

    var timer = 0

    private var inspectMode: Boolean = false

    private var gameCells: Array<Array<Cell>> =
            Array(VERTICAL_COUNT)
            {
                Array(HORISONTAL_COUNT)
                { instance.layoutInflater.inflate(R.layout.field_cell, null) as Cell }
            }

    private var gameState: GameState = GameState.NOT_STARTED

    private var gameOver: Boolean = false

    fun startGame(){
        gameState = GameState.STARTED
        startTimer()
    }

    fun stopGame(){
        TimerExecutorService?.shutdown()
    }

    @SuppressLint("ClickableViewAccessibility")
    fun initCells() {
        Log.d("Engine", "Init cells")
        for (y in 0 until VERTICAL_COUNT) {
            for (x in 0 until HORISONTAL_COUNT) {

                val cell = gameCells[y][x]

                cell.setyPosition(y)
                cell.setxPosition(x)
                cell.cellGame = this

            }
        }

        fillFieldWithBombs()
    }

    fun getCell(y: Int, x: Int): Cell {
        return gameCells[y][x]
    }

    fun safetyGetCell(y: Int, x: Int): Cell? {
        if (y in 0 until VERTICAL_COUNT && x in 0 until HORISONTAL_COUNT) {
            return gameCells[y][x]
        } else return null
    }

    fun openCell(y: Int, x: Int) {

        val currentCell = gameCells[y][x]

        val neightbours = getNeighbors(y, x)

        var bombCounter = 0

        for (cell in neightbours) {
            if (cell.state == CellState.BOMD)
                bombCounter++
        }

        if (bombCounter == 0 && currentCell.state != CellState.BOMD) {
            openOthersCells(neightbours)

        } else if (currentCell.state != CellState.BOMD) {
            currentCell.state = CellState.getStateByNumber(bombCounter)
        }

    }

    fun inspy(y: Int, x: Int, bool: Boolean) {

        val currentCell = gameCells[y][x]

        val neightbours = getNeighbors(y, x)


        var bombsAround = 0
        var flagsAroung = 0

        for (cell in neightbours) {
            if (!cell.isOpened && !cell.isInspected)
                cell.inspyingMe(bool)

            if (cell.isInspected) {
                flagsAroung++
            }

            if (cell.state == CellState.BOMD) {
                bombsAround++
            }
        }

        Log.d("GAME", "inspy: $bool")
        Log.d("GAME", "bombsAround: $bombsAround")
        Log.d("GAME", "flagsAroung: $flagsAroung")
        Log.d("GAME", "condition: " + (!bool && bombsAround > 0 && bombsAround == flagsAroung))

        if (!bool && bombsAround > 0 && bombsAround == flagsAroung) {
            openOthersCells(neightbours)
        }
    }

    fun getNeighbors(y: Int, x: Int): List<Cell> {
        val upLeft = safetyGetCell(y - 1, x - 1)
        val up = safetyGetCell(y - 1, x)
        val upRight = safetyGetCell(y - 1, x + 1)
        val left = safetyGetCell(y, x - 1)
        val right = safetyGetCell(y, x + 1)
        val downLeft = safetyGetCell(y + 1, x - 1)
        val down = safetyGetCell(y + 1, x)
        val downRight = safetyGetCell(y + 1, x + 1)

        return listOfNotNull(upLeft, up, upRight, left, right, downLeft, down, downRight)
    }

    fun openOthersCells(neightbours: List<Cell>) {
        for (cell in neightbours) {
            if (!cell.isOpened && !cell.isInspected) {
                cell.openCell()
            }
        }
    }

    fun fillFieldWithBombs() {
        val random = java.util.Random()

        while (bombsCount > 0) {
            val randY = random.nextInt(VERTICAL_COUNT)
            val randX = random.nextInt(HORISONTAL_COUNT)

            val cell = gameCells[randY][randX]

            if (cell.state != CellState.BOMD) {
                cell.state = CellState.BOMD
                bombsCount--
            }
        }
    }


    fun openAll() {
        for (y in 0 until VERTICAL_COUNT) {
            for (x in 0 until HORISONTAL_COUNT) {

                val cell = gameCells[y][x]

                cell.openCell()
            }
        }
    }

    fun checkBoard(): Boolean {
        for (y in 0 until VERTICAL_COUNT) {
            for (x in 0 until HORISONTAL_COUNT) {

                val cell = gameCells[y][x]

                if (!cell.isOpened && cell.state != CellState.BOMD) {
                    return false

                }
            }
        }
        return true
    }

    fun gameOver(state: GameState) {
        if (TimerExecutorService != null){
            TimerExecutorService!!.shutdown()
        }
        gameState = state
        gameOver = true
        if (state == GameState.LOSE) {
            openAll()
        }
        updateActivity()
    }

    fun updateActivity() {
        instance.update()
    }

    fun isGameOver(): Boolean {
        return gameOver
    }

    fun vibrate() {
        instance.vibrate()
    }

    fun isInspectMode(): Boolean {
        return inspectMode
    }

    fun changeInspectMode() {
        inspectMode = !inspectMode
    }

    fun getState(): GameState {
        return gameState
    }

    fun calculateBombCount(): Int {

        val bombCount = VERTICAL_COUNT * HORISONTAL_COUNT / 7

        if (bombCount > 0) {
            return bombCount
        } else return 1
    }

    fun increaseFlagCounter() {
        flagsCount++
    }

    fun decreaseFlagCounter() {
        flagsCount--
    }

    fun updateInstance(activity: GameActivity) {
        instance = activity
    }

    fun startTimer() {

//        Timer().schedule(object : TimerTask() {
//            override fun run() {
//                timer++
//                instance.handler.handleMessage(Message())
//            }
//        }, 1000)

        if (TimerExecutorService != null && !TimerExecutorService!!.isShutdown){
            return
        }

        TimerExecutorService = Executors.newScheduledThreadPool(1)
        TimerExecutorService!!.scheduleAtFixedRate(
                {
                    Log.d("TIMER", "HI :) $timer")

                    LocalBroadcastManager.getInstance(instance)
                            .sendBroadcast(Intent("ui.update"))

                    timer++
                },
                0, 1, TimeUnit.SECONDS)
    }

    override fun toString(): String {
        return gameCells.toString()
    }

}
