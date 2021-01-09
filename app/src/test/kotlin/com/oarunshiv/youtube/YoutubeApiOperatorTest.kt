package com.oarunshiv.youtube

import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.Playlist
import com.google.api.services.youtube.model.PlaylistItem
import com.google.api.services.youtube.model.ResourceId
import com.google.api.services.youtube.model.SearchResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class YoutubeApiOperatorTest {

    @Test
    fun search() {
        val expectedVideoId = "testVideoId"
        val mockSearchResult = listOf(SearchResult().apply { id = ResourceId().apply { videoId = expectedVideoId } })
        val youTube = mockk<YouTube> {
            every { search() } returns mockk {
                every { list(any()) } returns mockk {
                    every { setQ("query") } returns this
                    every { setMaxResults(1L) } returns this
                    every { setType(any()) } returns this
                    every { setVideoDefinition(any()) } returns this
                    every { execute() } returns mockk {
                        every { items } returns mockSearchResult
                    }
                }
            }
        }

        val videoId = YoutubeApiOperator(youTube).search("query")

        assertEquals(expectedVideoId, videoId)
    }

    @Test
    fun createPlaylist() {
        val expectedPlaylistId = "testPlaylistId"
        val mockPlaylistResult = Playlist().apply { id = expectedPlaylistId }
        val slot = slot<Playlist>()
        val youTube = mockk<YouTube> {
            every { playlists() } returns mockk {
                every { insert(any(), capture(slot)) } returns mockk {
                    every { execute() } returns mockPlaylistResult
                }
            }
        }
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
        val youTube = mockk<YouTube> {
            every { playlistItems() } returns mockk {
                every { insert(any(), capture(slot)) } returns mockk {
                    every { execute() } returns mockk()
                }
            }
        }
        val videoId = "testVideoId"
        val playlistId = "testPlaylistId"
        YoutubeApiOperator(youTube).addSongToPlaylist(videoId, playlistId)
        slot.captured.snippet.let {
            assertEquals(videoId, it.resourceId.videoId)
            assertEquals(playlistId, it.playlistId)
        }
    }
}