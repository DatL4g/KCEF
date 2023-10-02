package dev.datlag.kcef.model

import kotlinx.serialization.Serializable

@Serializable
internal data class GitHubRelease(
    val body: String
)
