package ru.bluedrive.launcher

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.content.Intent
import android.net.Uri
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var web: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        )
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.allowFileAccess = true
        web.webViewClient = WebViewClient()
        web.webChromeClient = WebChromeClient()
        web.addJavascriptInterface(Bridge(), "Android")
        setContentView(web)
        web.loadUrl("file:///android_asset/index.html")
    }

    inner class Bridge {
        @JavascriptInterface fun launch(which: String) {
            runOnUiThread {
                val packages = when (which) {
                    "nav" -> listOf("ru.yandex.yandexnavi", "com.google.android.apps.maps")
                    "music" -> listOf("ru.yandex.music")
                    "radio" -> listOf("com.microntek.radio", "com.ts.MainUI", "com.android.fmradio")
                    "video" -> listOf("com.mxtech.videoplayer.ad", "org.videolan.vlc")
                    "phone" -> listOf("com.android.dialer", "com.google.android.dialer")
                    "bluetooth" -> listOf("com.android.settings")
                    "apps", "settings" -> listOf("com.android.settings")
                    else -> emptyList()
                }
                var opened = false
                for (pkg in packages) {
                    val intent = packageManager.getLaunchIntentForPackage(pkg)
                    if (intent != null) {
                        try { startActivity(intent); opened = true; break } catch (_: Exception) {}
                    }
                }
                if (!opened) {
                    if (which == "nav") {
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com")))
                            opened = true
                        } catch (_: Exception) {}
                    }
                }
                if (!opened) Toast.makeText(this@MainActivity, "Приложение не найдено на магнитоле", Toast.LENGTH_SHORT).show()
            }
        }

        @JavascriptInterface fun message(text: String) {
            runOnUiThread { Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT).show() }
        }
    }

    override fun onBackPressed() {
        if (::web.isInitialized) web.evaluateJavascript("showHome()", null) else super.onBackPressed()
    }
}
