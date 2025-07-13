package com.bnao.rotaplay

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var myWebView: WebView
    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    private var mCapturedImageUri: Uri? = null
    private val FILE_CHOOSER_REQUEST_CODE = 100

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1)
        }

        myWebView = findViewById(R.id.webview)
        setupWebView()
        myWebView.loadUrl("file:///android_asset/index.html")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetJavaScriptEnabled", "ObsoleteSdkInt")
    private fun setupWebView() {
        val webSettings: WebSettings = myWebView.settings
        webSettings.javaScriptEnabled = true
        myWebView.webViewClient = WebViewClient()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.safeBrowsingEnabled = true
        }

        myWebView.setWebChromeClient(object : WebChromeClient() {
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
                        startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE)
                    }
                } catch (e: ActivityNotFoundException) {
                    mFilePathCallback = null
                    return false
                }
                return true
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FILE_CHOOSER_REQUEST_CODE -> {
                if (mFilePathCallback != null) {
                    mFilePathCallback!!.onReceiveValue(
                        WebChromeClient.FileChooserParams.parseResult(resultCode, data)
                    )
                    mFilePathCallback = null
                }
                if (mCapturedImageUri != null) {
                    if (resultCode == Activity.RESULT_OK) {
                        // 处理拍照结果
                    }
                    mCapturedImageUri = null
                }
            }
        }
    }
}
