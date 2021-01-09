package com.oarunshiv.youtube

import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.Playlist
import com.google.api.services.youtube.model.PlaylistItem
import com.google.api.services.youtube.model.SearchResult
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

fun getMockYouTube(
    slot: CapturingSlot<Playlist> = slot(),
    mockPlaylistResult: Playlist = mockk(),
    playlistItemSlot: CapturingSlot<PlaylistItem> = slot(),
    mockSearchResult: List<SearchResult> = getMockSearchResult()
): YouTube {
    return mockk {
        every { search() } returns mockk {
            every { list(any()) } returns mockk {
                every { setQ(any()) } returns this
                every { setMaxResults(1L) } returns this
                every { setType(any()) } returns this
                every { setVideoDefinition(any()) } returns this
                every { execute() } returns mockk {
                    every { items } returns mockSearchResult
                }
            }
            every { playlists() } returns mockk {
                every { insert(any(), capture(slot)) } returns mockk {
                    every { execute() } returns mockPlaylistResult
                }
            }

            every { playlistItems() } returns mockk {
                every { insert(any(), capture(playlistItemSlot)) } returns mockk {
                    every { execute() } returns mockk()
                }
            }
        }
    }
}

private fun getMockSearchResult() = listOf(mockk<SearchResult>() {
    every { id } returns mockk {
        every { videoId } returns "videoId"
    }
})