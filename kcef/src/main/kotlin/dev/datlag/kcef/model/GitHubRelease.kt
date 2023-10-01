package dev.datlag.kcef.model

import kotlinx.serialization.Serializable

@Serializable
data class GitHubRelease(
    val body: String
)
