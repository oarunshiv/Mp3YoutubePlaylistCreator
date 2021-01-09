package com.oarunshiv.youtube

import com.google.api.client.googleapis.json.GoogleJsonResponseException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KLogging
import java.io.File

class Orchestrator(
    private val youTubeOperator: YoutubeApiOperator,
    private val songFinder: SongFinder
) {

    private val json = Json { prettyPrint = true }

    fun createPlaylist() {
        val songInfoFilePath = createSongInfoFile()
        addYoutubeLink(songInfoFilePath)

        val playlistId = getPlaylistId()
        addToPlaylist(songInfoFilePath, playlistId)
    }

    private fun serializeSongsToFile(fileName: String, songs: List<SongInfo>) {
        val songInfoListJson = json.encodeToString(songs)
        val file = File(fileName).also {
            if (!it.exists()) { it.createNewFile() }
        }
        file.writeText(songInfoListJson)
    }

    private fun createSongInfoFile(): String {
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

    private fun SongInfo.asQueryString(): String {
        val sb = StringBuilder(title)
        album.takeUnless { it.isNullOrEmpty() }?.let { sb.append(" + $it") }
        composer.takeUnless { it.isNullOrEmpty() }?.let { sb.append(" + $it") }
        return sb.toString()
    }

    private fun addYoutubeLink(songInfoJsonFilePath: String) {
        val jsonString = File(songInfoJsonFilePath).readText()
        val songs = json.decodeFromString<List<SongInfo>>(jsonString)
        var count = 0
        run songIteration@{
            songs
                .filter { it.youtubeVideoId.isNullOrEmpty() }
                .forEach { song ->
                    try {
                        song.youtubeVideoId =
                            executeYoutubeRequest { youTubeOperator.search(song.asQueryString()) }?.also { count++ }
                                ?: "NOT_FOUND"
                    } catch (e: Exception) {
                        if (e is GoogleJsonResponseException &&
                            e.statusCode == 403 && e.content.contains(Regex(""""reason" : "quotaExceeded""""))
                        ) {
                            logger.warn { "Quota exceeded with apiKey. Please try again tomorrow." }
                            return@songIteration
                        } else {
                            logger.error(e) { "Failed to obtain youtube video id for $song" }
                        }
                    }
                }
        }
        serializeSongsToFile(songInfoJsonFilePath, songs)
        logger.info { "Found YouTube videoIds for $count songs" }
    }

    private fun <T> executeYoutubeRequest(action: () -> T?): T? {
        var result: Result<T?>
        do {
            result = runCatching(action)
        } while (result.isFailure && result.isRetriable())
        if (result.isFailure) throw result.exceptionOrNull()!!
        return result.getOrNull()
    }

    private fun addToPlaylist(
        songInfoJsonFilePath: String,
        playlistId: String,
        filteringCondition: (SongInfo) -> Boolean = { true }
    ) {
        val jsonString = File(songInfoJsonFilePath).readText()
        val songs = json.decodeFromString<List<SongInfo>>(jsonString)
        var count = 0
        run songIteration@{
            songs
                .filter(filteringCondition)
                .filter { playlistId !in it.playlistIds && it.youtubeVideoId != null }
                .groupBy { it.album }
                .forEach { (_, songs) ->
                    songs.forEach { song ->
                        try {
                            executeYoutubeRequest {
                                youTubeOperator.addSongToPlaylist(song.youtubeVideoId!!, playlistId)
                            }
                            song.playlistIds.add(playlistId)
                            count++
                        } catch (e: Exception) {
                            if (e is GoogleJsonResponseException &&
                                e.statusCode == 403 && e.content.contains(Regex(""""reason" : "quotaExceeded""""))
                            ) {
                                logger.warn { "Quota exceeded with apiKey. Please try again tomorrow." }
                                return@songIteration
                            } else {
                                logger.error(e) { "Failed to obtain add $song to playlist" }
                            }
                        }
                    }
                }
        }
        serializeSongsToFile(songInfoJsonFilePath, songs)
        logger.info { "Updated $count songs" }
    }

    private fun getPlaylistId(): String {
        val response = getUserResponse("Do you want to create a new playlist? (Y/N): ") { it in YES_OR_NO_RESPONSE }
        return if (response in YES_RESPONSE) {
            val playlistTitle = getUserResponse("Enter playlist title: ")
            val playlistDescription = getUserResponse("Enter playlist description: ")
            youTubeOperator.createPlaylist(playlistTitle, playlistDescription)
        } else {
            getUserResponse("Enter playlistId to update: ")
        }
    }

    private fun <T> Result<T>.isRetriable(): Boolean {
        val exception = exceptionOrNull() ?: return false
        return (exception as? GoogleJsonResponseException)?.statusCode == 500
    }

    companion object : KLogging() {
        private val YES_RESPONSE = setOf("Y", "y")
        private val NO_RESPONSE = setOf("n", "N")
        private val YES_OR_NO_RESPONSE = YES_RESPONSE + NO_RESPONSE
        private const val SONG_INFO_JSON_FILE = """SongInfoList.json"""
    }
}

