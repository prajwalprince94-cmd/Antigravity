package com.prajwalprince94.expensetracker.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
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
    val publishedAt: String,
    val fileSize: Long = 0L
)

object AppUpdateManager {
    private const val GITHUB_REPO = "prajwalprince94-cmd/Antigravity"
    private const val RELEASES_API_URL = "https://api.github.com/repos/$GITHUB_REPO/releases/latest"
    private const val APK_FILE_NAME = "ExpenseTracker-update.apk"

    /**
     * Checks GitHub Releases API for the latest published release.
     */
    suspend fun checkForUpdates(currentVersion: String): AppUpdateInfo = withContext(Dispatchers.IO) {
        try {
            val url = URL(RELEASES_API_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 12_000
                readTimeout = 12_000
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

                // Find APK asset (prefer masterpiece/release APK, fallback to any apk)
                var downloadUrl: String? = null
                var fileSize = 0L
                val assets = release.optJSONArray("assets")
                if (assets != null) {
                    var fallbackUrl: String? = null
                    var fallbackSize = 0L

                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            val urlStr = asset.optString("browser_download_url", "")
                            val size = asset.optLong("size", 0L)
                            if (name.contains("masterpiece", ignoreCase = true) || name.contains("stride", ignoreCase = true)) {
                                downloadUrl = urlStr
                                fileSize = size
                                break
                            } else if (fallbackUrl == null) {
                                fallbackUrl = urlStr
                                fallbackSize = size
                            }
                        }
                    }
                    if (downloadUrl == null) {
                        downloadUrl = fallbackUrl
                        fileSize = fallbackSize
                    }
                }

                val isNewer = isVersionNewer(tagName, currentVersion)

                AppUpdateInfo(
                    isUpdateAvailable = isNewer && !downloadUrl.isNullOrBlank(),
                    latestVersion = tagName.ifEmpty { "v2.1.0" },
                    currentVersion = currentVersion,
                    releaseTitle = releaseTitle,
                    releaseNotes = releaseNotes,
                    downloadUrl = downloadUrl,
                    publishedAt = publishedAt,
                    fileSize = fileSize
                )
            } else {
                AppUpdateInfo(
                    isUpdateAvailable = false,
                    latestVersion = currentVersion,
                    currentVersion = currentVersion,
                    releaseTitle = "Up to Date",
                    releaseNotes = "You are currently running the latest version.",
                    downloadUrl = null,
                    publishedAt = "",
                    fileSize = 0L
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
                publishedAt = "",
                fileSize = 0L
            )
        }
    }

    /**
     * Downloads the APK file to external files directory and launches Android package installer.
     */
    suspend fun downloadAndInstall(
        context: Context,
        downloadUrl: String,
        onProgress: (Float, Long, Long) -> Unit,
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
                    onError("Please grant 'Install Unknown Apps' permission in Settings, then tap Install.")
                    return@withContext
                }
            }

            // Multi-hop redirect follower handling 301, 302, 303, 307, 308 redirects from GitHub releases to CDN
            var currentUrl = downloadUrl
            var connection: HttpURLConnection? = null
            var redirectCount = 0

            while (redirectCount < 6) {
                val u = URL(currentUrl)
                val conn = (u.openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = true
                    connectTimeout = 20_000
                    readTimeout = 30_000
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                    setRequestProperty("Accept", "*/*")
                }
                val code = conn.responseCode
                if (code in 300..399) {
                    val location = conn.getHeaderField("Location")
                    conn.disconnect()
                    if (location.isNullOrBlank()) break
                    currentUrl = location
                    redirectCount++
                } else {
                    connection = conn
                    break
                }
            }

            val finalConn = connection ?: throw IllegalStateException("Could not establish download connection")
            if (finalConn.responseCode !in 200..299) {
                throw IllegalStateException("Server returned HTTP ${finalConn.responseCode}")
            }

            val totalBytes = finalConn.contentLengthLong

            // Store in external files dir (Download) so system Package Installer can read without SELinux denial
            val targetDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: context.externalCacheDir
                ?: context.cacheDir

            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            val destinationFile = File(targetDir, APK_FILE_NAME)
            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            var lastReportedTime = 0L
            var lastProgress = 0f

            finalConn.inputStream.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    val buffer = ByteArray(16 * 1024)
                    var bytesRead: Int
                    var totalRead = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead

                        val now = System.currentTimeMillis()
                        if (totalBytes > 0) {
                            val progress = (totalRead.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                            if (progress - lastProgress >= 0.015f || (now - lastReportedTime > 150)) {
                                lastProgress = progress
                                lastReportedTime = now
                                withContext(Dispatchers.Main) {
                                    onProgress(progress, totalRead, totalBytes)
                                }
                            }
                        }
                    }
                    output.flush()
                }
            }

            withContext(Dispatchers.Main) {
                onProgress(1f, destinationFile.length(), destinationFile.length())
            }

            // Launch package installer
            launchApkInstaller(context, destinationFile)

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError("Download failed: ${e.localizedMessage ?: "Unknown network error"}")
            }
        }
    }

    /**
     * Checks if a previously downloaded update APK exists and is ready for install.
     */
    fun getDownloadedUpdateFile(context: Context): File? {
        val targetDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: context.externalCacheDir
            ?: context.cacheDir
        val file = File(targetDir, APK_FILE_NAME)
        return if (file.exists() && file.length() > 1_000_000L) file else null
    }

    /**
     * Triggers Android system package installer with FileProvider URI.
     */
    fun launchApkInstaller(context: Context, apkFile: File) {
        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            apkFile
        )

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        // Explicitly grant URI permission to installer activities
        val queryIntentActivities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.queryIntentActivities(
                installIntent,
                android.content.pm.PackageManager.ResolveInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.queryIntentActivities(installIntent, 0)
        }

        for (resolveInfo in queryIntentActivities) {
            val pkg = resolveInfo.activityInfo.packageName
            context.grantUriPermission(pkg, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(installIntent)
    }

    /**
     * Parses numeric parts from version strings.
     * e.g., "v2.1.0-masterpiece" -> [2, 1, 0]
     */
    fun parseVersion(versionStr: String): List<Int> {
        val clean = versionStr.removePrefix("v").removePrefix("V")
        val regex = Regex("""\d+""")
        return regex.findAll(clean).map { it.value.toInt() }.toList()
    }

    /**
     * Compares two semantic version strings safely.
     */
    fun isVersionNewer(latestStr: String, currentStr: String): Boolean {
        val latestParts = parseVersion(latestStr)
        val currentParts = parseVersion(currentStr)

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
