package org.svmjaidevi.portal

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout

class UrlSetupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("SvmJaideviPrefs", Context.MODE_PRIVATE)
        val savedUrl = sharedPref.getString("server_url", null)

        // If URL is already configured, proceed straight to WebView Portal
        if (savedUrl != null) {
            launchMainActivity(savedUrl)
            return
        }

        setContentView(R.layout.activity_url_setup)

        val etUrl = findViewById<EditText>(R.id.etUrl)
        val tilUrl = findViewById<TextInputLayout>(R.id.tilUrl)
        val btnConnect = findViewById<Button>(R.id.btnConnect)

        btnConnect.setOnClickListener {
            var urlInput = etUrl.text.toString().trim()

            if (!urlInput.startsWith("http://") && !urlInput.startsWith("https://")) {
                urlInput = "http://$urlInput"
            }

            if (isValidUrl(urlInput)) {
                // Save the validated server URL
                sharedPref.edit().putString("server_url", urlInput).apply()
                launchMainActivity(urlInput)
            } else {
                tilUrl.error = getString(R.string.error_invalid_url)
            }
        }
    }

    private fun isValidUrl(url: String): Boolean {
        return Patterns.WEB_URL.matcher(url).matches()
    }

    private fun launchMainActivity(url: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("SERVER_URL", url)
        }
        startActivity(intent)
        finish() // Closes setup screen so back-press doesn't return here
    }
}
