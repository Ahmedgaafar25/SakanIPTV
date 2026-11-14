# Sakan IPTV

Android IPTV player for phones, Android TV and Google TV. Supports Xtream Codes (live, movies, series), common Stalker/Ministra MAC portals (live and movies), English/Arabic, an RTL sidebar and playback rotation.

## App updates
The app checks this repository's latest **published, stable release** on launch, at most once per day, and through Settings > App updates. A release must contain a signed APK and `update.json`. It verifies the manifest SHA-256, file size, package ID, newer version code and the existing app's signing certificate before opening the Android installer. Android asks the user to confirm installation and may require allowing installation from Sakan IPTV.

The repository's releases must be public for anonymous device access. Private repositories and draft releases are not served by the updater. No GitHub token belongs in the APK, source, manifests or repository settings fields.

## Build
JDK 17 or 21, Android SDK platform 35, Gradle wrapper 8.11.1.
```
./gradlew assembleRelease testDebugUnitTest
```
The workflow runs tests and produces an **unsigned** build artifact. It is not a distributable update. Production APKs must be signed with the original private key, which is deliberately excluded from GitHub. Preserve your private signing backup separately.

## Publish an already signed update
After incrementing versionCode/versionName and signing locally with the original key:
```
python3 scripts/publish-release.py --repository Ahmedgaafar25/SakanIPTV --apk SakanIPTV-X.Y.apk --version X.Y --version-code N
```
The script uses your existing `gh` login and creates the checksum manifest. Add `--draft` to stage without publishing. A release is visible to devices only after publication in a public repository.

## Validation and scope
Java protocol, language and update-metadata tests and DOM behavior checks are included. Real provider subscriptions, physical Android/TV installation, download/installer permissions and full OTA operation require device testing. The player has no bundled subscription or channels.

Cairo font uses SIL OFL; AndroidX/Media3 use Apache 2.0. Notices and generated hero provenance are included in the project.
