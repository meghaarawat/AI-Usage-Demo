package com.kitlabs.aiusagedemo

import android.os.Bundle
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class WebviewActivity : AppCompatActivity() {

    private lateinit var web: WebView
    private val tenantId = "101954"
    private val locationId = "104429"
    private val siteKey = "DgMMDnN52EWAp44jTOLNzgS2P2VEj8Vcs-QsFNi3IaU"
    private val amount = 25.00
    private val url = "https://d16main.meevodev.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        web = findViewById(R.id.webView)

        val html = """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <script src="$url/lib/meevo-pay-deposit.js"></script>
    </head>

    <body>
        <h2>Complete Payment</h2>

        <meevo-pay-deposit
            id="pay"
            api-base-url="$url"
            bearer-token="${BuildConfig.TOKEN}"
            tenant-id="$tenantId"
            location-id="$locationId"
            recaptcha-internal-key="$siteKey"
            transaction-amount="${"%.2f".format(Locale.US, amount)}"
            show-pay-button="true">
        </meevo-pay-deposit>

        <script>
            customElements.whenDefined('meevo-pay-deposit').then(function () {
                const el = document.getElementById('pay');

                console.log('Component initialized');
                console.log('Submit method:', typeof el.submit);

                [
                    'hostedPageLoaded',
                    'validityChanged',
                    'cardMetadataChanged',
                    'paymentAccountCreated',
                    'paymentFailed'
                ].forEach(function (name) {
                    el.addEventListener(name, function (event) {
                        console.log('Payment event:', name, event.detail);
                    });
                });
            });
        </script>
    </body>
    </html>
""".trimIndent()

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
        web.loadDataWithBaseURL(
            url,
            html,
            "text/html",
            "utf-8",
            null
        )
    }

}