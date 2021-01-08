package com.oarunshiv.youtube

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class SongFinderTest {

    @Test
    fun listSongs() {
        val testRoot = this.javaClass.getResource("/testRoot").file
        val songs = SongFinder().listSongs(testRoot)
        val expectedSongs = listOf(SongInfo("test title", "test album", "test composer", "test genre"))
        assertEquals(expectedSongs, songs)
    }
}