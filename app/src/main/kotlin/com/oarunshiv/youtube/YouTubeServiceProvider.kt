package com.oarunshiv.youtube

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import java.io.FileInputStream
import java.io.InputStreamReader

/**
 * Provides a [YouTube] instance initialized with the auth token of the user.
 */
class YouTubeServiceProvider {
    private fun authorize(clientSecretsJsonFile: String, httpTransport: NetHttpTransport): Credential {
        val inputStream = InputStreamReader(FileInputStream(clientSecretsJsonFile))
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, inputStream)
        val flow = GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
            .build()
        val localServerReceiver = LocalServerReceiver.Builder().apply { port = 61906 }.build()
        return AuthorizationCodeInstalledApp(flow, localServerReceiver).authorize("user")
    }

    /**
     * Returns the [YouTube] instance initialized with background HTTP server listening to obtain the user's auth token.
     * @param clientSecretsJsonPath The file path for the OAUTH client_secret.json credential file.
     * Refer [https://developers.google.com/youtube/v3/guides/auth/server-side-web-apps] for more info on how to
     * create the credentials.
     *
     * @return instance of [YouTube].
     */
    fun getService(clientSecretsJsonPath: String): YouTube {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val credential = authorize(clientSecretsJsonPath, httpTransport)
        return YouTube.Builder(httpTransport, JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME)
            .build()
    }

    companion object {
        private val SCOPES = listOf("https://www.googleapis.com/auth/youtube.force-ssl")
        private val JSON_FACTORY: JsonFactory = JacksonFactory.getDefaultInstance()
        private const val APPLICATION_NAME = "Mp3YoutubePlaylistCreatorJavaApp"
    }
}
