package com.yzk.githubclient.data

import com.google.gson.annotations.SerializedName

/**
 * @description response of exchange access token by code
 *
 * @author: yezhekai.256
 * @date: 5/24/25
 */
data class AccessTokenResp(
    @SerializedName("access_token")
    val accessToken: String,
    /** The scope of permissions granted by the token (e.g., "repo,user"). Can be null. */
    val scope: String?,
    /** The type of the token (e.g., "bearer"). Can be null. */
    @SerializedName("token_type")
    val tokenType: String?
)
