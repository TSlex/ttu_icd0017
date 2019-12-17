package com.tslex.lifetrack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class Menu : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
    }

    fun onSettingsButtonClicked(view: View){
        val intent = Intent(this, Preferences::class.java)
        startActivity(intent)
    }
}
