package com.tslex.minesweeper

import android.util.Log
import android.view.MotionEvent
import android.view.View

class TouchListener : View.OnTouchListener {
    override fun onTouch(view: View, event: MotionEvent): Boolean {

        if (view is Cell) {

            if (event.action == MotionEvent.ACTION_DOWN) {
                Log.d(view.toString(), "my state is:" + view.state)
                view.openCell()
            } else if (event.action == MotionEvent.ACTION_UP) {
                view.inspyOthers(false)
            }
        }

        return true
    }
}
