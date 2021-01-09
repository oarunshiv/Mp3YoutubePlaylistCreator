package com.oarunshiv.youtube

import com.google.api.services.youtube.model.Playlist
import com.google.api.services.youtube.model.PlaylistItem
import com.google.api.services.youtube.model.ResourceId
import com.google.api.services.youtube.model.SearchResult
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class YoutubeApiOperatorTest {

    @Test
    fun search() {
        val expectedVideoId = "testVideoId"
        val mockSearchResult = listOf(SearchResult().apply { id = ResourceId().apply { videoId = expectedVideoId } })
        val youTube = getMockYouTube(mockSearchResult = mockSearchResult)

        val videoId = YoutubeApiOperator(youTube).search("query")

        assertEquals(expectedVideoId, videoId)
    }

    @Test
    fun createPlaylist() {
        val expectedPlaylistId = "testPlaylistId"
        val mockPlaylistResult = Playlist().apply { id = expectedPlaylistId }
        val slot = slot<Playlist>()
        val youTube = getMockYouTube(slot, mockPlaylistResult)
        val title = "playListTitle"
        val description = "playlistDescription"

        val playlistId = YoutubeApiOperator(youTube).createPlaylist(title, description)

        slot.captured.snippet.let {
            assertEquals(title, it.title)
            assertEquals(description, it.description)
        }
        assertEquals(expectedPlaylistId, playlistId)
    }

    @Test
    fun addSongsToPlaylist() {
        val slot = slot<PlaylistItem>()
        val youTube = getMockYouTube(playlistItemSlot = slot)
        val videoId = "testVideoId"
        val playlistId = "testPlaylistId"
        YoutubeApiOperator(youTube).addSongToPlaylist(videoId, playlistId)
        slot.captured.snippet.let {
            assertEquals(videoId, it.resourceId.videoId)
            assertEquals(playlistId, it.playlistId)
        }
    }
}