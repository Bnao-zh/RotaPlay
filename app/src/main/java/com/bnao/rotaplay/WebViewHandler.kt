package com.bnao.rotaplay

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.webkit.*
import androidx.annotation.RequiresApi

class WebViewHandler(private val activity: Activity) {
    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    private var mCapturedImageUri: Uri? = null
    private val fileChooserRequestCode = 100

    @SuppressLint("SetJavaScriptEnabled")
    fun setupWebView(webView: WebView) {
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                webView.evaluateJavascript("javascript:initializeApp()", null)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webSettings.safeBrowsingEnabled = true
        }

        webView.setWebChromeClient(object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                if (mFilePathCallback != null) {
                    mFilePathCallback!!.onReceiveValue(null)
                }
                mFilePathCallback = filePathCallback
                val intent = fileChooserParams?.createIntent()
                try {
                    if (intent != null) {
                        activity.startActivityForResult(intent, fileChooserRequestCode)
                    }
                } catch (_: ActivityNotFoundException) {
                    mFilePathCallback = null
                    return false
                }
                return true
            }
        })
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            fileChooserRequestCode -> {
                if (mFilePathCallback != null) {
                    mFilePathCallback!!.onReceiveValue(
                        WebChromeClient.FileChooserParams.parseResult(resultCode, data)
                    )
                    mFilePathCallback = null
                }
                if (mCapturedImageUri != null) {
                    mCapturedImageUri = null
                }
            }
        }
    }
}
