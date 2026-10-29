package com.kitlabs.aiusagedemo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.kitlabs.aiusagedemo.R.string.sentry_id
import com.posthog.PostHog
import io.sentry.ProfileLifecycle
import io.sentry.SentryLevel
import io.sentry.SentryOptions
import io.sentry.android.core.SentryAndroid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.pushy.sdk.Pushy
import java.net.URI
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var text: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start the Pushy listening service (per Pushy docs: call after super.onCreate)
        Pushy.listen(this)

        setContentView(R.layout.activity_main)

        if (!Pushy.isRegistered(this)) {
            registerForPushNotifications(this@MainActivity)
        }

        text = findViewById(R.id.text)

        text.setOnClickListener {
            PostHog.capture(event = "test_event")

            val flagResult = PostHog.getFeatureFlagResult("feature-flag-key")
            if (flagResult?.enabled == true) {
                Log.d("FeatureFlag", "Feature enabled with variant: ${flagResult.variant}")
            }

            startActivity(Intent(this, HomeActivity::class.java))
        }

        //UrlValidator.isValid("https://blog.coolapp.org/products")

        AppUpdateChecker.checkForUpdate(this)
    }

    fun registerForPushNotifications(activity: Activity) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    // Register the device for notifications
                    val deviceToken = Pushy.register(activity)

                    // Log token to logcat
                    Log.d("Pushy", "Pushy device token: $deviceToken")

                    // Subscribe to topic(s) only after the device is registered
                    Pushy.subscribe("news", activity)

                    // Send the token to your backend server via an HTTP GET request
                    //URL("https://{YOUR_API_HOSTNAME}/register/device?token=$deviceToken").openConnection()

                    // Return the token as the result
                    deviceToken
                } catch (exc: Exception) {
                    // Return the exception as the result
                    exc
                }
            }

            // Show the result on the main thread
            val message = when (result) {
                is Exception -> {
                    Log.e("Pushy", result.message ?: "Error registering")
                    result.message ?: "Unknown error occurred"
                }
                else -> "Pushy device token: $result\n\n(copy from logcat)"
            }

            // Display dialog
            android.app.AlertDialog.Builder(activity)
                .setTitle("Pushy")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

}
