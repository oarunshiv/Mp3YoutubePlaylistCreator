# Mp3YoutubePlaylistCreator
Creates Youtube playlists based on local songs (stored as mp3).

# Instructions
* Checkout the code locally.
* Refer [YouTube documentation](https://developers.google.com/youtube/v3/guides/auth/server-side-web-apps) for more info on how to create necessary OAuth credentials. For quick step by step info, refer below.        
* Download the `client_secret.json` file. 
* run `./gradlew run`
* When prompted, provide the full path to the folder containing the songs to be identified.
* When prompted, provide the full path to the `client_secret.json` file.
* If you need to create a new playlist, 
  a. Specify the playlist title and the description.
  b. If you are adding songs to already created playlist, specify the playlistId instead.
* Please note, YouTube has a default quota of 10,000 units/day. Each search costs 100units and playlist operations
 cost 50units.
 
## Instructions to Create OAuth Token 
1. Go to https://console.developers.google.com/apis/dashboard
1. Click on select a project -> New project.
1. Use project name something like `Mp3YoutubePlaylistCreator`. Leave location as 'No Organization'.
1. When you click on notifications tab, you should see the new project created. Click on the project.
1. Configure Consent Screen -> UserType = External -> Create.
1. AppName -> Mp3YoutubePlaylistCreator. User Support email -> `<your mail id>`. Developer contact information -> `<your mail id>`.
1. Save and Continue.
1. Add Or Remove Scopes -> Select -> `.../auth/youtube.force-ssl`. Save and Continue.
1. Test Users -> Add Users -> `<mail id of the user creating the playlist>`. Save and Continue.
1. Click on "Create Credentials" -> "OAuth ClientID".
1. Select ApplicationType as `Web Application`, Name -> `Mp3YoutubePlaylistCreatorJavaApp`. URIs -> `http://localhost:61906/Callback`. Press Create.
1. Click on the created OAuth Client Id 'Mp3YoutubePlaylistCreatorJavaApp'. Download JSON.

# Project Details
## Execution details
The application executes the logic in multiple phases:
1. Song Identification - The App prompts for the full directory of the folder to search for. It then recursively identifies all the MP3 files in the directory and creates a `SongInfoList.json` file in the specified directory containing information (like title, album, composer and genre) for all the identified songs.
1. YouTubeId identification - In this phase, the App queries YouTube to get the videoId. At this step, the App opens the Google OAuth authentication screen in your default browser. Authenticate with the same test user you have added in `step 5` above. Once that is done, it queries YouTube, identifies the videoId and updates `SongInfoList.json` with the youtubeVideoId details.
1. Playlist creation - 
  1. If the option to create a new playlist is selected, the App prompts to provide the playlist title and playlist description. It then creates a playlist with the specified details.
  1. If the option to create a new playlist was rejected, the App prompts to provide the YouTubePlaylistId of the playlist to update.
1. Song Insertion - App then proceeds to add each of the song to the selected playlist. It updates `SongInfoList.json` with the IDs of the playlists to which the songs were added. The App provides the capbility to be executed multiple times and add same songs to different playlists.

Because of the low daily throttling limits imposed by Google, it is possible that the App might not be able to perform all the operations in one go. It saves the progress to `SongInfoList.json` and continues the execution for pending songs on the subsequent runs. 

Possible workaround to increase the throughput: 
* Execute the task as a cron job daily.
* Use multiple different developer accounts.
* [Request Google](https://support.google.com/youtube/contact/yt_api_form) for quota increase.

## Dependencies
* This project uses Kotlin- 1.4 with [Kotlix Serialization](https://kotlinlang.org/docs/reference/serialization.html).
* Standard [YouTube Data APIs](https://developers.google.com/youtube/v3/docs) for  searching, playlist creation and playlist updation operations.
* [Apache-Tika](https://tika.apache.org/1.25/gettingstarted.html) for mp3 file parsing.
* [Koin](https://start.insert-koin.io/) for dependency injection.
* Junit-5 for testing and [Mockk](https://mockk.io/) for mocking.
