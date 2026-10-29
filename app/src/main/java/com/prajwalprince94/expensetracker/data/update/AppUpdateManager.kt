package com.prajwalprince94.expensetracker.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateInfo(
    val isUpdateAvailable: Boolean,
    val latestVersion: String,
    val currentVersion: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val downloadUrl: String?,
    val publishedAt: String
)

object AppUpdateManager {
    private const val GITHUB_REPO = "prajwalprince94-cmd/Antigravity"
    private const val RELEASES_API_URL = "https://api.github.com/repos/$GITHUB_REPO/releases/latest"

    /**
     * Checks GitHub Releases API for the latest published release.
     */
    suspend fun checkForUpdates(currentVersion: String): AppUpdateInfo = withContext(Dispatchers.IO) {
        try {
            val url = URL(RELEASES_API_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "ExpenseTracker-Android-App")
            }

            if (connection.responseCode in 200..299) {
                val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                val release = JSONObject(jsonStr)

                val tagName = release.optString("tag_name", "").trim()
                val releaseTitle = release.optString("name", "New Update")
                val releaseNotes = release.optString("body", "Bug fixes and performance improvements.")
                val publishedAt = release.optString("published_at", "")

                // Find APK asset
                var downloadUrl: String? = null
                val assets = release.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            downloadUrl = asset.optString("browser_download_url", null)
                            break
                        }
                    }
                }

                // Clean version strings for comparison
                val cleanLatest = tagName.removePrefix("v").split("-").firstOrNull() ?: tagName
                val cleanCurrent = currentVersion.removePrefix("v").split("-").firstOrNull() ?: currentVersion

                val isNewer = isVersionNewer(cleanLatest, cleanCurrent) || 
                              (tagName.isNotEmpty() && tagName != currentVersion && !currentVersion.contains(tagName))

                AppUpdateInfo(
                    isUpdateAvailable = isNewer && !downloadUrl.isNullOrBlank(),
                    latestVersion = tagName.ifEmpty { "v2.0" },
                    currentVersion = currentVersion,
                    releaseTitle = releaseTitle,
                    releaseNotes = releaseNotes,
                    downloadUrl = downloadUrl,
                    publishedAt = publishedAt
                )
            } else {
                AppUpdateInfo(
                    isUpdateAvailable = false,
                    latestVersion = currentVersion,
                    currentVersion = currentVersion,
                    releaseTitle = "Up to Date",
                    releaseNotes = "You have the latest version installed.",
                    downloadUrl = null,
                    publishedAt = ""
                )
            }
        } catch (e: Exception) {
            AppUpdateInfo(
                isUpdateAvailable = false,
                latestVersion = currentVersion,
                currentVersion = currentVersion,
                releaseTitle = "Check Failed",
                releaseNotes = "Unable to connect to update server: ${e.localizedMessage ?: "Network error"}",
                downloadUrl = null,
                publishedAt = ""
            )
        }
    }

    /**
     * Downloads the APK file to the internal cache directory and launches the Android installer.
     */
    suspend fun downloadAndInstall(
        context: Context,
        downloadUrl: String,
        onProgress: (Float) -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            // Check unknown sources permission on Android 8.0+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    withContext(Dispatchers.Main) {
                        val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                            data = Uri.parse("package:${context.packageName}")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(settingsIntent)
                    }
                    onError("Please grant 'Install Unknown Apps' permission and tap update again.")
                    return@withContext
                }
            }

            val url = URL(downloadUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = true
                connectTimeout = 15_000
                readTimeout = 20_000
                setRequestProperty("User-Agent", "ExpenseTracker-Android-App")
            }

            // Handle potential HTTP 301/302 GitHub release redirects
            var finalConnection = connection
            var redirect = false
            val status = connection.responseCode
            if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER) {
                redirect = true
            }

            if (redirect) {
                val newUrl = connection.getHeaderField("Location")
                finalConnection = (URL(newUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15_000
                    readTimeout = 20_000
                    setRequestProperty("User-Agent", "ExpenseTracker-Android-App")
                }
            }

            val totalBytes = finalConnection.contentLengthLong.coerceAtLeast(1L)
            val destinationFile = File(context.cacheDir, "ExpenseTracker-update.apk")
            if (destinationFile.exists()) destinationFile.delete()

            finalConnection.inputStream.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var totalRead = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        val progress = (totalRead.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                        withContext(Dispatchers.Main) {
                            onProgress(progress)
                        }
                    }
                    output.flush()
                }
            }

            // Launch package installer using FileProvider
            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                destinationFile
            )

            withContext(Dispatchers.Main) {
                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(apkUri, "application/vnd.android.package-archive")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(installIntent)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError("Download failed: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    private fun isVersionNewer(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }

        val length = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until length) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}
