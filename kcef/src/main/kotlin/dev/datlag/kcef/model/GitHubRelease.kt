package dev.datlag.kcef.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GitHubRelease(
    val body: String,
    val assets: List<Asset> = emptyList()
) {
    @Serializable
    internal data class Asset(
        val name: String,
        @SerialName("browser_download_url") val downloadUrl: String
    )
}
