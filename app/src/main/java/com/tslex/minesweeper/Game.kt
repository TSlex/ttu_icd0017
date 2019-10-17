package com.tslex.minesweeper

import android.annotation.SuppressLint
import android.util.Log

class Game(val instance: GameActivity, val VERTICAL_COUNT: Int, val HORISONTAL_COUNT: Int) {

    var bombsCount = VERTICAL_COUNT * HORISONTAL_COUNT / 7

    private var gameCells: Array<Array<Cell>> =
            Array(VERTICAL_COUNT)
            {
                Array(HORISONTAL_COUNT)
                { instance.layoutInflater.inflate(R.layout.field_cell, null) as Cell }
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

        val upLeft = safetyGetCell(y - 1, x - 1)
        val up = safetyGetCell(y - 1, x)
        val upRight = safetyGetCell(y - 1, x + 1)
        val left = safetyGetCell(y, x - 1)
        val right = safetyGetCell(y, x + 1)
        val downLeft = safetyGetCell(y + 1, x - 1)
        val down = safetyGetCell(y + 1, x)
        val downRight = safetyGetCell(y + 1, x + 1)

        val currentCell = gameCells[y][x]

        val neightbours: Array<Cell?> = arrayOf(upLeft, up, upRight, left, right, downLeft, down, downRight)

        var bombCounter = 0

        for (cell in neightbours) {
            if (cell != null && cell.state == CellState.BOMD)
                bombCounter++
        }

        if (bombCounter == 0 && currentCell.state != CellState.BOMD) {
            for (cell in neightbours) {
                if (cell != null && !cell.isOpened) {
                    cell.openCell()
                }
            }
        } else if (currentCell.state != CellState.BOMD) {
            currentCell.state = CellState.getStateByNumber(bombCounter)
        }

    }

    fun inspy(y: Int, x: Int, bool: Boolean) {

        val upLeft = safetyGetCell(y - 1, x - 1)
        val up = safetyGetCell(y - 1, x)
        val upRight = safetyGetCell(y - 1, x + 1)
        val left = safetyGetCell(y, x - 1)
        val right = safetyGetCell(y, x + 1)
        val downLeft = safetyGetCell(y + 1, x - 1)
        val down = safetyGetCell(y + 1, x)
        val downRight = safetyGetCell(y + 1, x + 1)

        val neightbours: Array<Cell?> = arrayOf(upLeft, up, upRight, left, right, downLeft, down, downRight)

        for (cell in neightbours) {
            if (cell != null && !cell.isOpened)
                cell.inspyingMe(bool)
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

    fun vibrate() {
        instance.vibrate()
    }

    override fun toString(): String {
        return gameCells.toString()
    }

}