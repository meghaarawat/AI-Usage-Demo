package com.kitlabs.aiusagedemo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

class WebviewActivity : AppCompatActivity() {

    private enum class PayState { LOADING, READY, PROCESSING, FAILED }

    private lateinit var web: WebView
    private lateinit var progress: ProgressBar
    private lateinit var statusText: TextView
    private lateinit var retryButton: Button
    private val prepayStarted = AtomicBoolean(false)
    private val tenantId = "101954"
    private val locationId = "104429"
    private val siteKey = "DgMMDnN52EWAp44jTOLNzgS2P2VEj8Vcs-QsFNi3IaU"
    private val amount = 20.00
    private val url = "https://d16main.meevodev.com"

    // Booking values for /ob/Prepay. Fill these in before testing.
    private val prepayClientId = "2b0014c2-1d55-4be1-929c-b4f0004e3e8b" // BookRequest.ClientId
    private val prepayClientGlobalId = "2b0014c2-1d55-4be1-929c-b4f0004e3e8b" // ClientGlobalId
    private val prepayServiceId = "76bfe5ad-88a8-42a8-b1d5-ac6100370618" // BookRequest.Services[0].ServiceId
    private val prepayEmployeeId = "36dfdeed-bac3-4819-a03b-ac610037a1ce" // Services[0].EmployeeId
    private val prepayStartTime = "2026-11-19T11:45:00" // Services[0].StartTime
    private val prepayOpeningId = "5519c0ad-ab67-47b1-81fe-f325f840d483" // Services[0].OpeningId
    private val prepayAppointmentDate = "2026-11-19T00:00:00" // BookRequest.AppointmentDate
    private val prepayNote = "hello, test not from test client!" // BookRequest.Note, the "Appointment Notes" text from the review screen

    // Pay deposit button colours. Set these from the branding API later.
    private val payButtonBackground = "#1F58CA"
    private val payButtonText = "#FFFFFF"

    // Called from the page on each payment stage. JS callbacks run off the main thread.
    inner class PaymentBridge {
        @JavascriptInterface
        fun onPageReady() = renderState(PayState.READY)

        @JavascriptInterface
        fun onProcessing() = renderState(PayState.PROCESSING)

        @JavascriptInterface
        fun onPaymentFailed(message: String) = renderState(PayState.FAILED, message)

        @JavascriptInterface
        fun onPaymentAccountCreated(detailJson: String) {
            // The component can emit paymentAccountCreated more than once; only the first may book
            if (!prepayStarted.compareAndSet(false, true)) return
            val detail = JSONObject(detailJson)
            renderState(PayState.PROCESSING)
            CoroutineScope(Dispatchers.IO).launch {
                callPrepay(
                    paymentAccountId = detail.getString("paymentAccountId"),
                    encryptedSecurityCode = detail.optString("encryptedSecurityCode")
                )
            }
        }
    }

    private fun callPrepay(paymentAccountId: String, encryptedSecurityCode: String) {
        val service = JSONObject()
            .put("ServiceId", prepayServiceId)
            .put("EmployeeId", prepayEmployeeId)
            .put("StartTime", prepayStartTime)
            .put("OpeningId", prepayOpeningId)
            .put("NonRequest", true)
            .put("IsGuest", false)
            .put("Answers", JSONArray())

        val bookRequest = JSONObject()
            .put("ClientId", prepayClientId)
            .put("LocationId", locationId.toInt())
            .put("AppointmentDate", prepayAppointmentDate)
            .put("Note", prepayNote)
            .put("Answers", JSONArray())
            .put("Services", JSONArray().put(service))

        val body = JSONObject()
            .put("BookRequest", bookRequest)
            .put("PaymentAccountId", paymentAccountId)
            .put("ClientGlobalId", prepayClientGlobalId)
            .put("EncryptedSecurityCode", encryptedSecurityCode)
            .put("RetainCardOnFile", false)

        val conn = URL("$url/clientapp/ob/Prepay").openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Bearer ${BuildConfig.TOKEN}")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.outputStream.use { it.write(body.toString().toByteArray()) }

            val code = conn.responseCode
            val response = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.readText().orEmpty()
            Log.d("WebviewM", "Prepay HTTP $code: $response")

            // A booked appointment returns no warnings or errors in the body
            val booked = code in 200..299 && !response.contains("\"warnings\"") && !response.contains("\"responseStatus\"")
            if (booked) {
                openSuccess()
            } else {
                renderState(PayState.FAILED, extractErrorMessage(code, response))
            }
        } catch (e: Exception) {
            Log.e("WebviewM", "Prepay failed", e)
            renderState(PayState.FAILED, e.message ?: "Booking failed. Please try again.")
        } finally {
            conn.disconnect()
        }
    }

    // Pulls the server's message out of a Prepay error body, falling back to the HTTP status
    private fun extractErrorMessage(code: Int, body: String): String {
        return try {
            val json = JSONObject(body)
            val warnings = json.optJSONArray("warnings")
            if (warnings != null && warnings.length() > 0) {
                // Booking warnings, e.g. a double booking, are returned with HTTP 200
                return (0 until warnings.length()).joinToString("\n") {
                    warnings.getJSONObject(it).optString("message")
                }
            }
            val status = json.optJSONObject("responseStatus")
            if (status != null) {
                // The inner message is sometimes another JSON document with its own responseStatus
                val message = status.optString("message")
                val inner = runCatching { JSONObject(message).optJSONObject("responseStatus") }.getOrNull()
                return inner?.optString("message")?.takeIf { it.isNotEmpty() }
                    ?: message.takeIf { it.isNotEmpty() }
                    ?: status.optString("errorCode")
            }
            "Booking failed (HTTP $code)"
        } catch (e: Exception) {
            "Booking failed (HTTP $code)"
        }
    }

    // Opens the success summary; its close button returns to this screen with a fresh payment form
    private fun openSuccess() {
        val start = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(prepayStartTime)
        val whenText = SimpleDateFormat("d MMM yyyy, h:mm a", Locale.US).format(start!!)
        val summary = "Date & time: $whenText\nDeposit paid: \$${"%.2f".format(amount)}"
        runOnUiThread {
            startActivity(Intent(this, SuccessActivity::class.java).putExtra(SuccessActivity.EXTRA_SUMMARY, summary))
        }
    }

    // Shows the WebView only when ready; otherwise shows a spinner, a message, or a retry button
    private fun renderState(state: PayState, message: String = "") {
        runOnUiThread {
            web.visibility = if (state == PayState.READY) View.VISIBLE else View.INVISIBLE
            progress.visibility = if (state == PayState.LOADING || state == PayState.PROCESSING) View.VISIBLE else View.GONE
            statusText.text = when (state) {
                PayState.LOADING -> "Loading payment form..."
                PayState.PROCESSING -> "Processing payment..."
                PayState.FAILED -> message
                PayState.READY -> ""
            }
            statusText.visibility = if (statusText.text.isEmpty()) View.GONE else View.VISIBLE
            retryButton.visibility = if (state == PayState.FAILED) View.VISIBLE else View.GONE
        }
    }

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        web = findViewById(R.id.webView)
        progress = findViewById(R.id.progress)
        statusText = findViewById(R.id.statusText)
        retryButton = findViewById(R.id.retryButton)

        retryButton.setOnClickListener {
            prepayStarted.set(false)
            renderState(PayState.LOADING)
            web.reload()
        }
        renderState(PayState.LOADING)

        // Embed the bundled Source Sans Pro so the Pay button renders it without internet access
        val fontBase64 = Base64.encodeToString(
            resources.openRawResource(R.font.source_sans_pro_bold).use { it.readBytes() },
            Base64.NO_WRAP
        )

        val html = """
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Pay deposit</title>
  <script src="$url/lib/meevo-pay-deposit.js"></script>
  <style>
    @font-face {
      font-family: 'Source Sans Pro';
      src: url(data:font/ttf;base64,$fontBase64) format('truetype');
      font-weight: 700;
    }
    body { margin: 0; padding: 16px; font-family: sans-serif; }
    #pay-deposit {
      font-family: 'Source Sans Pro', sans-serif;
      width: 100%;
      margin-top: 16px;
      padding: 14px;
      border: none;
      border-radius: 8px;
      background-color: $payButtonBackground;
      color: $payButtonText;
      font-size: 16px;
      font-weight: 700;
    }
    #pay-deposit:disabled { opacity: 0.5; }
  </style>
</head>
<body>
  <meevo-pay-deposit
    api-base-url="$url/clientapp"
    bearer-token="${BuildConfig.TOKEN}"
    tenant-id="$tenantId"
    location-id="$locationId"
    recaptcha-internal-key="$siteKey"
    transaction-amount="${"%.2f".format(amount)}"
    full-billing-address="true"
  ></meevo-pay-deposit>

  <button id="pay-deposit" disabled>Pay deposit</button>

  <script>
    var el = document.querySelector('meevo-pay-deposit');
    var payBtn = document.getElementById('pay-deposit');
    var required = ['api-base-url', 'bearer-token', 'tenant-id', 'location-id', 'recaptcha-internal-key'];

    el.addEventListener('paymentFailed', function (e) {
      console.error('Payment failed', JSON.stringify(e.detail));
      Android.onPaymentFailed(e.detail && e.detail.message ? e.detail.message : 'Payment failed. Please try again.');
    });

    var pageLoaded = false;
    el.addEventListener('hostedPageLoaded', function () {
      pageLoaded = true;
      console.log('hostedPageLoaded');
      Android.onPageReady();
    });
    el.addEventListener('validityChanged', function (e) { payBtn.disabled = !e.detail; });

    payBtn.addEventListener('click', function () {
      Android.onProcessing();
      el.submit();
    });

    el.addEventListener('paymentAccountCreated', function (e) {
      console.log('paymentAccountCreated', JSON.stringify(e.detail));
      Android.onPaymentAccountCreated(JSON.stringify(e.detail));
    });

    // The component fails silently when a required attribute is missing, so report it here
    var missing = required.filter(function (name) { return !el.getAttribute(name); });
    if (missing.length) {
      console.error('meevo-pay-deposit missing attributes: ' + missing.join(', '));
    }

    setTimeout(function () {
      if (!pageLoaded) {
        console.error('hostedPageLoaded not received after 30s; check /ob/HostedPageData and reCAPTCHA');
      }
    }, 30000);
  </script>
</body>
</html>
"""

        WebView.setWebContentsDebuggingEnabled(true)
        web.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                Log.d("WebviewM", "${message.messageLevel()} ${message.message()} (${message.sourceId()}:${message.lineNumber()})")
                return true
            }
        }
        web.webViewClient = object : WebViewClient() {
            override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
                Log.d("WebviewM", "HTTP ${errorResponse.statusCode} for ${request.url}")
            }
        }
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.addJavascriptInterface(PaymentBridge(), "Android")
        web.loadDataWithBaseURL(
            url,
            html,
            "text/html",
            "utf-8",
            null
        )
    }

}