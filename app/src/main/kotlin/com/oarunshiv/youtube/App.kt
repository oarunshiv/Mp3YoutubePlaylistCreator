package com.oarunshiv.youtube

import org.koin.core.context.startKoin
import org.koin.dsl.module

fun main() {
    val clientSecretJsonFile = getUserResponse("Enter the file path for the client_secret.json file: ")
    val applicationModule = module {
        factory { SongFinder() }
        single { YouTubeServiceProvider().getService(clientSecretJsonFile) }
        single { YoutubeApiOperator(get()) }
        single { Orchestrator(get(), get()) }
    }

    val koin = startKoin {
        printLogger()
        modules(applicationModule)
    }.koin

    val orchestrator = koin.get<Orchestrator>()
    orchestrator.createPlaylist()
}
