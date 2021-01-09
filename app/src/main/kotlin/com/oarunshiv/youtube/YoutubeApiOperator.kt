package com.oarunshiv.youtube

import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.Playlist
import com.google.api.services.youtube.model.PlaylistItem
import com.google.api.services.youtube.model.PlaylistItemSnippet
import com.google.api.services.youtube.model.PlaylistSnippet
import com.google.api.services.youtube.model.PlaylistStatus
import com.google.api.services.youtube.model.ResourceId
import com.google.api.services.youtube.model.SearchListResponse
import com.google.api.services.youtube.model.SearchResult
import mu.KLogging

/**
 * A wrapper class around [YouTube] with provides search, create and update playlist capabilities.
 */
class YoutubeApiOperator(private val youTube: YouTube) {

    /**
     * Searches High definition YouTube videos with the specified query and returns the top result.
     * @param query The string to query YouTube.
     * @return the YouTube id for the video for a successful search attempt. Else returns null.
     */
    fun search(query: String): String? {
        return youTube
            .search()
            .list(SNIPPET)
            .setMaxResults(1L)
            .setQ(query)
            .setType(RESULT_TYPE_VIDEO)
            .setVideoDefinition(VIDEO_DEFINITION_HIGH)
            .execute()
            .items
            .firstOrNull()
            ?.id
            ?.videoId
    }


    /**
     * Creates YouTube playlist with the specified title and description.
     * @param playlistTitle Title of the playlist to be created.
     * @param playlistDescription Description for the playlist.
     *
     * @return ID for the created YouTube Playlist.
     */
    fun createPlaylist(playlistTitle: String, playlistDescription: String): String {
        val playlist = Playlist().apply {
            snippet = PlaylistSnippet().apply {
                defaultLanguage = LANGUAGE_CODE_EN
                description = playlistDescription
                title = playlistTitle
            }
            status = PlaylistStatus().apply {
                privacyStatus = PRIVATE_VIDEO
            }
        }

        return youTube.playlists().insert("$SNIPPET,$STATUS", playlist).execute().id
    }

    /**
     * Adds songs to the YouTube playlist.
     * @param youTubeVideoId ID of the YouTube video to be inserted into the playlist.
     * @param youTubePlaylistId ID of the playlist to the video to.
     */
    fun addSongToPlaylist(youTubeVideoId: String, youTubePlaylistId: String) {
        val playlistItem = PlaylistItem().apply {
            snippet = PlaylistItemSnippet().apply {
                playlistId = youTubePlaylistId
                resourceId = ResourceId().apply {
                    kind = PLAYLIST_ITEM_TYPE
                    videoId = youTubeVideoId
                }
            }
        }

        youTube.playlistItems().insert(SNIPPET, playlistItem).execute()
    }

    companion object {
        private const val PRIVATE_VIDEO = "private"
        private const val LANGUAGE_CODE_EN = "en"
        private const val PLAYLIST_ITEM_TYPE = "youtube#video"
        private const val SNIPPET = "snippet"
        private const val STATUS = "status"
        private const val RESULT_TYPE_VIDEO = "video"
        private const val VIDEO_DEFINITION_HIGH = "high"
    }
}