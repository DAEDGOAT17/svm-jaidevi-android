package org.svmjaidevi.portal

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private var serverUrl: String = ""
    private var backPressedTime: Long = 0

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        serverUrl = intent.getStringExtra("SERVER_URL") ?: ""
        if (serverUrl.isEmpty()) {
            resetServerUrl()
            return
        }

        webView = findViewById(R.id.webView)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        progressBar = findViewById(R.id.progressBar)

        // WebView Configurations - Required for modern Odoo 18 client-side rendering
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.allowFileAccess = true

        // User Agent adjustment (optional, helps identify native app calls)
        settings.userAgentString = settings.userAgentString + " SVM_Jaidevi_Android_App"

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
                swipeRefresh.isRefreshing = false
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                swipeRefresh.isRefreshing = false
                progressBar.visibility = View.GONE
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressBar.progress = newProgress
                if (newProgress == 100) {
                    progressBar.visibility = View.GONE
                } else {
                    progressBar.visibility = View.VISIBLE
                }
            }
        }

        // Standard File Download Handler for marksheets and school docs
        webView.setDownloadListener { url, _, _, _, _ ->
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Failed to download file.", Toast.LENGTH_SHORT).show()
            }
        }

        // Pull to refresh support
        swipeRefresh.setOnRefreshListener {
            webView.reload()
        }

        // Load the portal
        webView.loadUrl(serverUrl)
    }

    // Handles hardware back key to navigate Odoo history instead of quitting the app
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack()
                return true
            } else {
                // Double click back to exit, or triple click to reset connection IP
                val currentTime = System.currentTimeMillis()
                if (currentTime - backPressedTime < 2000) {
                    super.onBackPressed()
                } else {
                    Toast.makeText(this, "Press back again to exit. Hold to reset connection.", Toast.LENGTH_SHORT).show()
                    backPressedTime = currentTime
                }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    // Hold Back Button on the root page to reset connection settings
    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            resetServerUrl()
            return true
        }
        return super.onKeyLongPress(keyCode, event)
    }

    private fun resetServerUrl() {
        val sharedPref = getSharedPreferences("SvmJaideviPrefs", Context.MODE_PRIVATE)
        sharedPref.edit().remove("server_url").apply()
        
        Toast.makeText(this, "Connection reset. Please configure URL again.", Toast.LENGTH_LONG).show()
        val intent = Intent(this, UrlSetupActivity::class.java)
        startActivity(intent)
        finish()
    }
}
