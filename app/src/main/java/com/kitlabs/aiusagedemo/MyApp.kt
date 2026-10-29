package com.kitlabs.aiusagedemo

import android.app.Application
import android.util.Log
import com.posthog.PostHog
import com.posthog.PostHogOnFeatureFlags
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig
import io.sentry.ProfileLifecycle
import io.sentry.SentryLevel
import io.sentry.SentryOptions
import io.sentry.android.core.SentryAndroid

class MyApp : Application() {

    companion object {
        const val POSTHOG_API_KEY = "phc_CXugiveKKghJM7zKvdjb4mKrhC5SGP7QgLvXk7f7EGxH"
        // usually 'https://us.i.posthog.com' or 'https://eu.i.posthog.com'
        const val POSTHOG_HOST = "https://us.i.posthog.com"
    }

    override fun onCreate() {
        super.onCreate()

        val config = PostHogAndroidConfig(
            apiKey = POSTHOG_API_KEY,
            host = POSTHOG_HOST // host is optional if you use https://us.i.posthog.com
        )
        PostHogAndroid.setup(this, config)

        /*val config = PostHogAndroidConfig(
            apiKey = POSTHOG_API_KEY,
            host = POSTHOG_HOST // host is optional if you use https://us.i.posthog.com
        ).apply {
            sendFeatureFlagEvent = true
            preloadFeatureFlags = true
            onFeatureFlags = PostHogOnFeatureFlags {
                // Feature flags are now safely available
                PostHog.getAllFeatureFlags()?.forEach { flag ->
                    Log.d(
                        "PostHog",
                        "Flag: ${flag.key} = ${flag.enabled}, variant: ${flag.variant}"
                    )
                }
            }
        }*/

        SentryAndroid.init(this) { options ->
            // Required: set your sentry.io project identifier (DSN)
            options.dsn = "https://40f793a74297e8538f8745a021113e0a@o4512101323571200.ingest.us.sentry.io/4512123552268288"
            // Add data like request headers, user ip address and device name, see https://docs.sentry.io/platforms/android/data-management/data-collected/ for more info
            options.isSendDefaultPii = true
            // enable automatic traces for user interactions (clicks, swipes, scrolls)
            options.isEnableUserInteractionTracing = true
            // enable screenshot for crashes
            options.isAttachScreenshot = true
            // enable view hierarchy for crashes
            options.isAttachViewHierarchy = true
            // enable the performance API by setting a sample-rate, adjust in production env
            options.tracesSampleRate = 1.0
            // enable UI profiling, adjust in production env. This is eval
            //
            // uated only once per session
            options.profileSessionSampleRate = 1.0
            // set profiling mode. For more info see https://docs.sentry.io/platforms/android/profiling/#enabling-ui-profiling
            options.profileLifecycle = ProfileLifecycle.TRACE
            // record session replays for 100% of errors and 10% of sessions
            options.sessionReplay.sessionSampleRate = 0.1
            options.sessionReplay.onErrorSampleRate = 1.0
            // If your application has strict PII requirements we recommend using the Canvas screenshot strategy.
            // See the Android Session Replay documentation for details: https://docs.sentry.io/platforms/android/session-replay/#screenshot-strategy
            // options.sessionReplay.screenshotStrategy = ScreenshotStrategyType.CANVAS
            // enable logs to be sent to Sentry. Use Sentry.logger() to capture logs or check the available integrations that capture logs automatically: https://docs.sentry.io/platforms/android/logs/#integrations
            options.logs.isEnabled = true;
            // enable tombstone support for richer Native crashes context. See more at https://docs.sentry.io/platforms/android/configuration/tombstones/
            options.isTombstoneEnabled = true;
            // Add a callback that will be used before the event is sent to Sentry.
            // With this callback, you can modify the event or, when returning null, also discard the event.
            options.beforeSend =
                SentryOptions.BeforeSendCallback { event, hint ->
                    if (SentryLevel.DEBUG == event.level) {
                        null
                    } else {
                        event
                    }
                }
        }
    }
}