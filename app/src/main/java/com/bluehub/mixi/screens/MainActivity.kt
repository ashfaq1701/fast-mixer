package com.bluehub.mixi.screens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bluehub.mixi.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.actionBar))

        supportActionBar?.apply {

            setDisplayUseLogoEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
    }
}
