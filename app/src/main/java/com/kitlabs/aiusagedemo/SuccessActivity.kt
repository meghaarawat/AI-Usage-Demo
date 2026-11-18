package com.kitlabs.aiusagedemo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SuccessActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SUMMARY = "summary"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success)

        findViewById<TextView>(R.id.summaryText).text = intent.getStringExtra(EXTRA_SUMMARY).orEmpty()

        // Return to the payment screen, clearing this screen from the stack
        findViewById<Button>(R.id.closeButton).setOnClickListener {
            startActivity(Intent(this, WebviewActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            finish()
        }
    }
}
