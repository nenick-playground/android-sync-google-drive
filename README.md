# android-sync-google-drive

How to sync Android app data between devices without providing own server.

## How it works

Google Drive provides the functionality **application folder** where your app
can store and receive data. This can be used to sync app data between devices.
The user have only to accept the request to access his Google Drive account for
the application specific folder.

###### - Store application-specific data https://developers.google.com/drive/api/v3/appdata

## What to do

You have to provide your own authentication to use the Google APIs
*(no payment necessary for this setup)*.

Clone this app and proceed with ...

### 1) Create key for app signing

With Android Studio you can generate a new keystore.

* Go to `Build -> Create Signed Bundle / APK` and choose `APK`
* Below `Key store path` choose `Create new ...`
* Fill out the form, use as alias something like `android-debug-key`
* Now generate, it is **not** recommended to put the keystore under source code control in the app repository.

###### - Generate an upload key and keystore https://developer.android.com/studio/publish/app-signing#generate-key

### 2) Configure Google Console Project

It's kind of a role where you can define what this app want to have access permissions.

* Select existing or create new project
  * Enable the Google Drive API.
    * Select Library and search there for the Google Drive API.
  * Create OAuth consent screen
    * Without an organization you have to choose User Type External
    * Type an Application name
    * Add Scope `../auth/drive.appdata`
  * Add credentials
    * Choose OAuth client ID
    * Choose Android
    * Use the hints to fill out the form

###### - Google Developer Console https://console.developers.google.com/
###### - Enable the Google Drive API https://developers.google.com/drive/api/v3/enable-drive-api

### 3) Configure signed APK builds

Your APK have to be signed so it can be successful authenticated with the Google Console project.

* Go to `File -> Project Structure -> Modules -> Signing Configs`
* Insert the keystore details

###### - Configure the build process to automatically sign your app https://developer.android.com/studio/publish/app-signing#sign-auto

Ready, now you can sync app data between devices. Install the app on two devices with
the same Google Account and you be able to "chat" together.

### Overview how all belongs together

<img src="http://yuml.me/efe59d2d.png" width=450>

###### - Authenticate your users https://developers.google.com/drive/api/v3/about-auth

