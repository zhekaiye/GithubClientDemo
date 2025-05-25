package com.yzk.githubclient.data

import com.google.gson.annotations.SerializedName

/**
 * @description
 *
 * @author: yezhekai.256
 * @date: 5/25/25
 */
data class Topic(
    @SerializedName("name")
    val name: String,
    @SerializedName("display_name")
    val displayName: String?,
    @SerializedName("short_description")
    val shortDesc: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("score")
    val score: Int,
    @SerializedName("repository_count")
    val repoCount: Int,
    @SerializedName("logo_url")
    val logoUrl: String?
)
