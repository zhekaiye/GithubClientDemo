package com.yzk.githubclient.data

/**
 * @description data of submit issue
 *
 * @author: yezhekai.256
 * @date: 5/24/25
 */
data class SubmitIssueData(
    val title: String,
    /** The optional body content of the issue. Defaults to null. */
    val body: String? = null
)
