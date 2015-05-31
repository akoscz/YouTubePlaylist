YouTubePlaylist
===============

A sample Android application which demonstrates the use of the [YouTube Data API v3](https://developers.google.com/youtube/v3/)
This sample app makes use of [Picasso](https://github.com/square/picasso) for downloading and caching the video thumbnails.
And lastly it uses [OkHttp](http://square.github.io/okhttp/) for HTTP response caching to avoid the network completely for repeat requests.

![](screenshot.png)

## Setup
  
  * Register your application with the [Google Developer Console](https://developers.google.com/youtube/registering_an_application)
  * Edit ApiKey.java and enter your applications "Browser Key"

*NOTE:* You MUST have a valid API key for this sample application to work. When you register your application with the Google Developer Console you need to enable the YouTube Data API.  Also, you need to register a Web Application NOT an Android application because the API key that this sample app uses is the "Browser Key".
  
## Dependencies

  * [com.android.support:support-v4:21.0.0](https://developer.android.com/tools/support-library/features.html#v4)
  * [com.android.support:appcompat-v7:21.0.0](https://developer.android.com/tools/support-library/features.html#v7-appcompat)
  * [com.squareup.picasso:picasso:2.5.2](https://github.com/square/picasso)
  * [com.squareup.okhttp:okhttp:2.4.0](http://square.github.io/okhttp/)
  * [com.google.code.gson:gson:2.3.1](https://code.google.com/p/google-gson)

## License

  * [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)
  
