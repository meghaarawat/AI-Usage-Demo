package com.kitlabs.aiusagedemo

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.app.AlertDialog
import com.posthog.PostHog

object AppUpdateChecker {

    private const val TAG = "AppUpdateChecker"

    fun checkForUpdate(activity: Activity) {

        try {
            // Fetch all remote configuration values from PostHog
            val latestVersion = getPayload("latest_version_android")
            val isUpdateRequired = getBooleanPayload("is_update_required")
            val alertTitle = getPayload("alert_title")
            val alertMessage = getPayload("alert_message")
            val appLink = getPayload("android_app_link")

            // Nothing configured
            if (latestVersion.isNullOrBlank()) {
                return
            }

            val currentVersion =
                activity.packageManager
                    .getPackageInfo(activity.packageName, 0)
                    .versionName

            if (currentVersion.isNullOrBlank()) {
                return
            }

            // Compare current app version with PostHog version
            if (isNewerVersion(latestVersion, currentVersion)) {

                Log.d(TAG, "latestVersion = $latestVersion")
                Log.d(TAG, "isUpdateRequired = $isUpdateRequired")
                Log.d(TAG, "alertTitle = $alertTitle")
                Log.d(TAG, "alertMessage = $alertMessage")
                Log.d(TAG, "appLink = $appLink")

                showUpdateDialog(
                    activity = activity,
                    title = alertTitle ?: "Update available",
                    message = alertMessage
                        ?: "A new version of the app is available.",
                    appLink = appLink,
                    isUpdateRequired = isUpdateRequired
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to check app update", e)
        }
    }

    private fun getPayload(key: String): String? {
        return try {
            PostHog.getFeatureFlagResult(key)?.payload?.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get PostHog value: $key", e)
            null
        }
    }

    private fun getBooleanPayload(key: String): Boolean {

        val value = getPayload(key)

        return when (value?.lowercase()) {
            "true" -> true
            "false" -> false
            else -> false
        }
    }

    private fun isNewerVersion(
        latestVersion: String,
        currentVersion: String
    ): Boolean {

        val latestParts = latestVersion.split(".")
        val currentParts = currentVersion.split(".")

        val maxLength = maxOf(
            latestParts.size,
            currentParts.size
        )

        for (i in 0 until maxLength) {

            val latest = latestParts
                .getOrNull(i)
                ?.toIntOrNull() ?: 0

            val current = currentParts
                .getOrNull(i)
                ?.toIntOrNull() ?: 0

            if (latest > current) {
                return true
            }

            if (latest < current) {
                return false
            }
        }

        return false
    }

    private fun showUpdateDialog(
        activity: Activity,
        title: String,
        message: String,
        appLink: String?,
        isUpdateRequired: Boolean
    ) {

        val builder = AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Update") { _, _ ->

                if (!appLink.isNullOrBlank()) {

                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(appLink)
                    )

                    activity.startActivity(intent)
                }
            }

        if (!isUpdateRequired) {

            builder.setNegativeButton("Cancel", null)
        }

        val dialog = builder.create()

        // If update is mandatory, prevent dismissing
        if (isUpdateRequired) {
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
        }

        dialog.show()
    }
}