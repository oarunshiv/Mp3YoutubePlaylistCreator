package com.oarunshiv.youtube

import io.mockk.every
import io.mockk.mockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class OrchestratorTest {

    private val orchestrator = Orchestrator(SongFinder())

    @AfterEach
    fun cleanup() {
        File(this.javaClass.getResource("/testRoot/SongInfoList.json").file).takeIf { it.exists() }?.deleteOnExit()
    }

    @Test
    fun `createSongInfoFile with new folder`() {
        setMockForReadLine("/testRoot")

        val songInfoJsonFilePath = orchestrator.createSongInfoFile()

        verifyFileContainsExpectedText(songInfoJsonFilePath)
    }

    @Test
    fun `createSongInfoFile with existing SongInfoFile`() {
        setMockForReadLine("/testRoot1")

        val songInfoJsonFilePath = orchestrator.createSongInfoFile()

        verifyFileContainsExpectedText(songInfoJsonFilePath)
    }

    private fun setMockForReadLine(resource: String) {
        mockkStatic(::readLine)
        every { readLine() } returns this.javaClass.getResource(resource).file
    }

    private fun verifyFileContainsExpectedText(songInfoJsonFilePath: String) {
        val file = File(songInfoJsonFilePath)
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
                    "genre": "test genre"
                }
            ]
        """.trimIndent()
    }
}