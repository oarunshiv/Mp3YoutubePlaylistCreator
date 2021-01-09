package com.oarunshiv.youtube

import kotlinx.serialization.Serializable
import org.apache.tika.metadata.Metadata

@Serializable
data class SongInfo(
    val title: String,
    val album: String?,
    val composer: String?,
    val genre: String?,
    var youtubeVideoId: String? = null,
    var playlistIds: MutableSet<String> = mutableSetOf()
) {
    constructor(metadata: Metadata) : this(
        metadata.get("title"),
        metadata.get("xmpDM:album"),
        metadata.get("xmpDM:composer"),
        metadata.get("xmpDM:genre")
    )
}
