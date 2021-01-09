package com.oarunshiv.youtube

import mu.KLogging
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.ParseContext
import org.apache.tika.parser.mp3.Mp3Parser
import org.xml.sax.ContentHandler
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import java.io.FileInputStream

/**
 * Class responsible for identifying song information from MP3 files.
 */
class SongFinder {
    /**
     * Identifies song information for all the songs in the [dir].
     * @param dir the directory to search for MP3 files.
     *
     * @return [SongInfo] for MP3 files found in [dir].
     */
    fun listSongs(dir: String): List<SongInfo> {
        return File(dir)
            .walkBottomUp()
            .filter { it.isFile && it.name.endsWith(".mp3") }
            .mapNotNull { getSongInfo(it) }
            .toList()
    }

    private fun getSongInfo(file: File): SongInfo? {
        val handler: ContentHandler = DefaultHandler()
        val metadata = Metadata()
        val parser = Mp3Parser()
        val parseContext = ParseContext()
        FileInputStream(file).use {
            kotlin.runCatching {
                parser.parse(it, handler, metadata, parseContext)
            }
            if(metadata.get("title").isNullOrBlank()) {
                logger.warn { "Couldn't obtain useful metadata for ${file.name}" }
                return null
            }
        }
        return SongInfo(metadata)
    }

    companion object : KLogging()
}
