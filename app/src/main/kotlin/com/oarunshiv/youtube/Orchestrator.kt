package com.oarunshiv.youtube

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KLogging
import java.io.File

class Orchestrator(private val songFinder: SongFinder) {

    private val json = Json { prettyPrint = true }

    fun createSongInfoFile(): String {
        val rootDir = getUserResponse("Enter root path of the folder to identify songs: ") {
            val file = File(it)
            file.isDirectory && file.canRead()
        }
        val outputFilePath = (rootDir.takeIf { it.endsWith("/") } ?: "$rootDir/")
        val songInfoJsonFile = outputFilePath + SONG_INFO_JSON_FILE
        if (File(songInfoJsonFile).exists()) {
            logger.info { "$songInfoJsonFile already exists" }
            return songInfoJsonFile
        }
        val songs = songFinder.listSongs(rootDir)
        serializeSongsToFile(songInfoJsonFile, songs)
        logger.info { "Created $SONG_INFO_JSON_FILE with information for ${songs.size} songs." }
        return songInfoJsonFile
    }

    private fun serializeSongsToFile(fileName: String, songs: List<SongInfo>) {
        val songInfoListJson = json.encodeToString(songs)
        val file = File(fileName).also {
            if (!it.exists()) {
                it.createNewFile()
            }
        }
        file.writeText(songInfoListJson)
    }

    companion object : KLogging() {
        private const val SONG_INFO_JSON_FILE = """SongInfoList.json"""
    }
}

