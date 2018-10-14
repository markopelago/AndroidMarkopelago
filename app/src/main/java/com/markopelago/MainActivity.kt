package com.markopelago

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import kotlin.system.exitProcess

fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    var activeNetworkInfo: NetworkInfo? = null
    activeNetworkInfo = cm.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
}

class MainActivity : AppCompatActivity() {
    private val SERVER_HOST = "https://www.markopelago.com/"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val webview: WebView = findViewById(R.id.webview) as WebView
        webview.getSettings().setJavaScriptEnabled(true)
        webview!!.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url)
                return true
            }
        }

        if (isNetworkAvailable(this@MainActivity)) {
            webview!!.loadUrl(SERVER_HOST + "kanari/")
        } else {
            Toast.makeText(this@MainActivity,"Please check your internet connection, then restart this App",Toast.LENGTH_SHORT).show()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
        System.exit(-1)
    }
}
