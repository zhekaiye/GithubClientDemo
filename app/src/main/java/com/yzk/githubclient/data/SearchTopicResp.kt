package com.yzk.githubclient.data

import com.google.gson.annotations.SerializedName

/**
 * @description
 *
 * @author: yezhekai.256
 * @date: 5/25/25
 */
data class SearchTopicResp(
    @SerializedName("total_count")
    val totalCount: Int,
    @SerializedName("incomplete_results")
    val incompleteResults: Boolean,
    @SerializedName("items")
    val items: List<Topic>
)
