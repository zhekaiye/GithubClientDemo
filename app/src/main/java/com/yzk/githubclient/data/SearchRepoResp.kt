package com.yzk.githubclient.data

import com.google.gson.annotations.SerializedName

/**
 * @description response of search repository
 *
 * @author: yezhekai.256
 * @date: 5/24/25
 */
data class SearchRepoResp(
    @SerializedName("total_count")
    val totalCount: Int,
    @SerializedName("incomplete_results")
    val incompleteResults: Boolean,
    @SerializedName("items")
    val items: List<Repository>
)
