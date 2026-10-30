package com.prajwalprince94.expensetracker.data.sync

/**
 * Lifecycle state of the cloud sync engine.
 */
enum class SyncState {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR,
    OFFLINE
}

/**
 * Conflict resolution strategy when records collide across devices.
 */
enum class ConflictResolutionStrategy(val displayName: String, val description: String) {
    LATEST_TIMESTAMP("Latest Timestamp (LWW)", "Preserves the most recent edit based on timestamp"),
    LOCAL_WINS("Local Device Wins", "Always preserves changes made on this device"),
    SERVER_WINS("Cloud Server Wins", "Always overwrites with records from Supabase")
}

/**
 * Result details from a sync cycle.
 */
data class SyncResult(
    val success: Boolean,
    val recordsUploaded: Int = 0,
    val recordsDownloaded: Int = 0,
    val conflictsResolved: Int = 0,
    val message: String = "",
    val timestampMillis: Long = System.currentTimeMillis()
)

/**
 * Historical audit log item for cloud sync activity.
 */
data class SyncLogItem(
    val id: String,
    val title: String,
    val details: String,
    val isSuccess: Boolean,
    val timestampMillis: Long = System.currentTimeMillis()
)
