package com.bnao.rotaplay

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var myWebView: WebView
    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    private var mCapturedImageUri: Uri? = null
    private val FILE_CHOOSER_REQUEST_CODE = 100
    // 尝试传感器
    private lateinit var sensorManager: SensorManager
    private var gravitySensor: Sensor? = null
    private var gravityValues = FloatArray(3)

    @SuppressLint("InlinedApi")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 请求所有必要的权限
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_CONNECT
        )
        ActivityCompat.requestPermissions(this, permissions, 1)

        // 初始化传感器管理器
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

        myWebView = findViewById(R.id.webview)
        setupWebView()
        myWebView.loadUrl("file:///android_asset/index.html")
        //添加 JavaScript 接口
        myWebView.addJavascriptInterface(this, "Android")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetJavaScriptEnabled", "ObsoleteSdkInt")
    private fun setupWebView() {
        val webSettings: WebSettings = myWebView.settings
        webSettings.javaScriptEnabled = true
        myWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // 在页面加载完成后调用 JavaScript 方法
                myWebView.evaluateJavascript("javascript:initializeApp()", null)
            }
        }

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

    @JavascriptInterface
    fun getGravityValues(): String {
        return "${gravityValues[0]},${gravityValues[1]},${gravityValues[2]}"
    }

    override fun onResume() {
        super.onResume()
        gravitySensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, 5000,0)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_GRAVITY) {
            gravityValues = event.values.copyOf()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 不需要实现
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
