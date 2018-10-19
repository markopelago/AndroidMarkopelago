package com.markopelago

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import okhttp3.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import android.graphics.Color
import android.net.Uri
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat.startActivity



var TOKEN = ""

fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    var activeNetworkInfo: NetworkInfo? = null
    activeNetworkInfo = cm.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
}

fun readWebView(context: Context,webview: WebView) {
    val currentUrl = webview.originalUrl
    if(currentUrl != null) {
        if (currentUrl.toString().contains("set_apps_token=")) {
            val temp = currentUrl.split("set_apps_token=")
            TOKEN = temp[1]
            if(TOKEN!="") {
                val path = context.getFilesDir()
                val filename = File(path.toString() + "/bWFya29wZWxhZ28=.dat")
                filename.writeText(TOKEN)
                webview!!.loadUrl(currentUrl.replace("set_apps_token=" + TOKEN,""))
            }
        }
        if (currentUrl.toString().contains("logout_success=1")) {
            val path = context.getFilesDir()
            val filename = File(path.toString() + "/bWFya29wZWxhZ28=.dat")
            TOKEN = ""
            filename.writeText(TOKEN )
            webview!!.loadUrl(currentUrl.replace("logout_success=1",""))
        }
    }
    Handler().postDelayed({ readWebView(context,webview) }, 1000)
}

fun readNotification(context: Context){
    if(TOKEN != "") {
        val url = context.getResources().getString(R.string.SERVER_HOST) + "read_notification.php?token="+TOKEN
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }
            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseGET = response.body()!!.string()
                    if (responseGET != "") {
                        val responseGETs = responseGET.split("]]]")
                        if (responseGETs.size > 0) {
                            responseGETs.forEach {
                                val responses = it.split("|||")
                                if (responses.size > 1) {
                                    showNotification(context.applicationContext, context.getResources().getString(R.string.app_name), responses[1].toString(), responses[0].toInt())
                                }
                            }
                        }
                    }
                }
            }
        })
    }
    Handler().postDelayed({ readNotification(context) }, 3000)
}

fun readVersion(context: Context){
    val url = context.getResources().getString(R.string.SERVER_HOST) + "get_version.php"
    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
        }
        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                val getVersion = response.body()!!.string().toLong()
                if (getVersion  > context.packageManager.getPackageInfo("com.markopelago", 0).versionCode) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("https://play.google.com/store/apps/details?id=com.markopelago")
                    context.startActivity(intent)
                }
            }
        }
    })
}

fun showNotification(context: Context,title:String,message:String,mNotificationId: Int = 1000){
    lateinit var mNotification: Notification
    var notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notifyIntent = Intent(context, MainActivity::class.java)

    notifyIntent.putExtra("mNotified", true)
    notifyIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

    val pendingIntent = PendingIntent.getActivity(context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    val res = context.resources
    val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val CHANNEL_ID = "Markopelago"
        val name = "Markopelago"
        val Description = "Markopelago Channel"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
        mChannel.description = Description
        mChannel.enableLights(true)
        mChannel.lightColor = Color.RED
        mChannel.enableVibration(true)
        mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        mChannel.setShowBadge(false)
        notificationManager.createNotificationChannel(mChannel)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)

        val resultIntent = Intent(context, MainActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(resultIntent)
        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        builder.setContentIntent(resultPendingIntent)

        notificationManager.notify(mNotificationId, builder.build())
    } else {
        mNotification = Notification.Builder(context)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(title)
                .setStyle(Notification.BigTextStyle()
                        .bigText(message))
                .setSound(uri)
                .setContentText(message).build()
        notificationManager.notify(mNotificationId, mNotification)
    }

}

class MainActivity : AppCompatActivity() {
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
            val path = this@MainActivity.getFilesDir()
            val filename = File(path.toString() + "/bWFya29wZWxhZ28=.dat")
            if(filename.exists()){
                TOKEN = FileInputStream(filename).bufferedReader().use { it.readText() }
            }
            webview!!.loadUrl(this@MainActivity.getResources().getString(R.string.SERVER_HOST) + "android_apps.php?token=" + TOKEN)
            readWebView(this@MainActivity,webview)
            readVersion(this@MainActivity)
            readNotification(this@MainActivity)
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