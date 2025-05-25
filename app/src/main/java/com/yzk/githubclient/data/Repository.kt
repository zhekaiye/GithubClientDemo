package com.yzk.githubclient.data

import com.google.gson.annotations.SerializedName

/**
 * @description github repository
 *
 * @author: yezhekai.256
 * @date: 5/24/25
 */
data class Repository(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("owner")
    val owner: UserProfile,
    @SerializedName("html_url")
    val htmlUrl: String,
    @SerializedName("description")
    val description: String?,
    // fork, url, created_at, updated_at, pushed_at, homepage, size
    @SerializedName("stargazers_count")
    val stargazersCount: Int,
    @SerializedName("language")
    val language: String?,
    @SerializedName("forks_count")
    val forksCount: Int
)
