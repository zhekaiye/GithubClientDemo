package com.yzk.githubclient.data

import com.google.gson.annotations.SerializedName

/**
 * @description github issue
 *
 * @author: yezhekai.256
 * @date: 5/24/25
 */
data class Issue(
    val id: Long,
    /** The issue number within the repository. */
    val number: Int,
    /** The title of the issue. */
    val title: String,
    /** The state of the issue (e.g., "open", "closed"). */
    val state: String,
    /** The main content/body of the issue (can be null). */
    val body: String?,
    /** Timestamp when the issue was created (ISO 8601 format). */
    @SerializedName("created_at")
    val createdAt: String,
    /** Timestamp when the issue was last updated (ISO 8601 format). */
    @SerializedName("updated_at")
    val updatedAt: String,
    /** The user who created the issue. */
    val user: UserProfile
)
