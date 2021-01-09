package com.oarunshiv.youtube

import io.mockk.every
import io.mockk.mockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import java.io.File

class OrchestratorTest : AutoCloseKoinTest() {

    private val orchestrator: Orchestrator by inject()

    @BeforeEach
    fun setup() {
        val testOrchestratorModule = module {
            single { getMockYouTube() }
            single { YoutubeApiOperator(get()) }
            single { SongFinder() }
            single { Orchestrator(get(), get()) }
        }
        startKoin { modules(testOrchestratorModule) }
    }

    @AfterEach
    fun cleanup() {
        File(this.javaClass.getResource("/testRoot/SongInfoList.json").file).takeIf { it.exists() }?.deleteOnExit()
        stopKoin()
    }

    @Test
    fun `createSongInfoFile with new folder`() {
        val resourceRoot = "/testRoot"
        setMockForReadLine(resourceRoot)

        orchestrator.createPlaylist()

        verifyFileContainsExpectedText(resourceRoot)
    }

    @Test
    fun `createSongInfoFile with existing SongInfoFile`() {
        val resourceRoot = "/testRoot1"
        setMockForReadLine(resourceRoot)

        orchestrator.createPlaylist()

        verifyFileContainsExpectedText(resourceRoot)
    }

    private fun setMockForReadLine(resource: String) {
        mockkStatic(::readLine)
        every { readLine() } returns this.javaClass.getResource(resource).file
    }

    private fun verifyFileContainsExpectedText(resourceRoot: String) {
        val resource = this.javaClass.getResource("$resourceRoot/SongInfoList.json")
        val file = File(resource.toURI())
        val actualText = file.readText()

        assertEquals(EXPECTED_FILE_CONTENTS, actualText)
    }

    companion object {
        val EXPECTED_FILE_CONTENTS = """
            [
                {
                    "title": "test title",
                    "album": "test album",
                    "composer": "test composer",
                    "genre": "test genre",
                    "youtubeVideoId": "videoId"
                }
            ]
        """.trimIndent()
    }
}