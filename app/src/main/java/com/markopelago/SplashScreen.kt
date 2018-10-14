package com.markopelago

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.TextView


class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val txtVersion: TextView = findViewById(R.id.txtVersion) as TextView
        txtVersion.text = "V." + this.packageManager.getPackageInfo(packageName, 0).versionName

        Handler().postDelayed(object : Runnable {
            override fun run() {
                val i =  Intent(this@SplashScreen,  MainActivity::class.java)
                startActivity(i)
                this.finish()
            }

            private fun finish() {
                // TODO Auto-generated method stub
            }
        }, 3000.toLong())

    }
}
