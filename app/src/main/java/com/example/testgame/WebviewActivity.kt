package com.example.testgame

import android.os.Bundle
import android.webkit.CookieManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_webview.*
import java.util.*

class WebviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        CookieManager.getInstance().setAcceptCookie(true)

        var googleUrl = "https://www.google"

        when (Locale.getDefault()) {
            Locale.US, Locale.UK, Locale.ENGLISH -> {
                webView.loadUrl("$googleUrl.com/")
            }
            Locale.JAPANESE, Locale.JAPAN -> {
                webView.loadUrl("$googleUrl.jp/")
            }
            Locale.CANADA -> {
                webView.loadUrl("$googleUrl.ca/")
            }
            Locale.CHINA, Locale.CHINESE, Locale.SIMPLIFIED_CHINESE, Locale.TRADITIONAL_CHINESE -> {
                webView.loadUrl("$googleUrl.cn/")
            }
            Locale("ru", "RU") -> {
                webView.loadUrl("https://yandex.ru/")
            }
            else -> {
                webView.loadUrl("$googleUrl.com/")
            }
        }
    }
}
