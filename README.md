YouTubePlaylist
===============

A sample Android application which demonstrates the use of the [YouTube Data API v3](https://developers.google.com/youtube/v3/).

This sample app makes use of the [YouTube Data API v3 classes] (https://developers.google.com/resources/api-libraries/documentation/youtube/v3/java/latest/) to fetch a YouTube [Playlist](https://developers.google.com/resources/api-libraries/documentation/youtube/v3/java/latest/com/google/api/services/youtube/model/Playlist.html) using the [GetPlaylistAsyncTask](app/src/main/java/com/akoscz/youtube/GetPlaylistAsyncTask.java) which then extracts the list of [Video](https://developers.google.com/resources/api-libraries/documentation/youtube/v3/java/latest/com/google/api/services/youtube/model/Video.html)'s the playlist contains.  The list of video's are then presented using a [RecyclerView](https://developer.android.com/reference/android/support/v7/widget/RecyclerView.html) of [CardView](https://developer.android.com/reference/android/support/v7/widget/CardView.html)'s in the [YouTubeRecyclerViewFragment](app/src/main/java/com/akoscz/youtube/YouTubeRecyclerViewFragment.java).  The data binding of video details to the [Video Card](app/src/main/res/layout/youtube_databinding_video_card.xml) is handled by the [PlaylistDatabindingCardAdapter](app/src/main/java/com/akoscz/youtube/PlaylistDatabindingCardAdapter.java).

[Picasso](https://github.com/square/picasso) is used for downloading and caching the video thumbnail images.
And lastly a [retained fragment](http://developer.android.com/guide/topics/resources/runtime-changes.html#RetainingAnObject) is used to persist the [Playlist](app/src/main/java/com/akoscz/youtube/model/Playlist.java) datamodel across orientation changes.

## Setup
  
  * Register your application with the [Google Developer Console](https://developers.google.com/youtube/registering_an_application).
  * Create an ["Api Key"](https://developers.google.com/youtube/registering_an_application#Create_API_Keys)
  * Edit [ApiKey.java](app/src/main/java/com/akoscz/youtube/ApiKey.java) and update `YOUTUBE_API_KEY` with your applications "Api Key"
  * Edit [YouTubeActivity.java](app/src/main/java/com/akoscz/youtube/YouTubeActivity.java) and update `YOUTUBE_PLAYLIST` to point to your YouTube [playlist](https://www.youtube.com/playlist?list=PLWz5rJ2EKKc_XOgcRukSoKKjewFJZrKV0).

*NOTE:* You MUST have a valid API key for this sample application to work. Remember, when you register your application with the Google Developer Console you need to enable the YouTube Data API.
  
## Application Dependencies

  * [com.android.support:cardview-v7:23.0.3](https://developer.android.com/tools/support-library/features.html#v7-cardview)
  * [com.android.support:recyclerview-v7:23.0.3](https://developer.android.com/tools/support-library/features.html#v7-recyclerview)
  * [com.android.support:appcompat-v7:23.0.3](https://developer.android.com/tools/support-library/features.html#v7-appcompat)
  * [com.squareup.picasso:picasso:2.5.2](https://github.com/square/picasso)
  * [com.google.apis:google-api-services-youtube:v3-rev149-1.20.0](https://developers.google.com/api-client-library/java/apis/youtube/v3)
  * [com.google.http-client:google-http-client-android:1.20.0](https://github.com/google/google-http-java-client)
  * [com.google.api-client:google-api-client-android:1.20.0](https://github.com/google/google-api-java-client)
  * [com.google.api-client:google-api-client-gson:1.20.0](https://github.com/google/google-api-java-client)

## Build Script Dependencies
  * [com.android.tools.build:gradle:1.3.1](https://developer.android.com/tools/revisions/gradle-plugin.html)
  * [com.android.databinding:dataBinder:1.0-rc1](http://developer.android.com/tools/data-binding/guide.html)

## Screenshots
__Phone__: Single Column Portrait and Landscape

![](screenshots/phone-port.png)

__Tablet__: 7" and 9" (sw600) 2 Columns Portrait, 3 Columns Landscape

![](screenshots/tablet_7_9-port.png)
![](screenshots/tablet_7_9-land.png)

__Tablet__: 10" (sw800) 3 Columns Portrait and Landscape

![](screenshots/tablet_10-port.png)
![](screenshots/tablet_10-land.png)

## License

  * [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)
  
