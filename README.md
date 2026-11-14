# Sakan IPTV

Android IPTV player for phones, Android TV and Google TV. Supports Xtream Codes (live, movies, series), common Stalker/Ministra MAC portals (live and movies), English/Arabic, an RTL sidebar and playback rotation.

## App updates
The app checks this repository's latest **published, stable release** on every foreground opening, without blocking use, and through Settings > App updates. A release must contain a signed APK and `update.json`. It verifies the manifest SHA-256, file size, package ID, newer version code and the existing app's signing certificate before opening the Android installer. Android asks the user to confirm installation and may require allowing installation from Sakan IPTV.

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

Cairo font uses SIL OFL; AndroidX/Media3 use Apache 2.0. License notices are included in the project.

## Version 1.5 — viewing features
- Provider EPG with current/next programme, date browsing and archive availability. Xtream uses `get_short_epg`/`get_simple_data_table`; Stalker uses `itv/get_short_epg` and `epg/get_simple_data_table`.
- Catch-up for past programmes that the provider reports as recorded: Xtream timeshift URLs using server timezone and Stalker `tv_archive/create_link` with the provider programme ID. A missing guide, archive flag or archive entitlement cannot be created by the player. Only dates returned by the provider are available.
- Account-scoped recently watched history and movie/episode resume positions; completed entries leave Continue watching. History is kept in private app storage, cleared through the UI, and never committed or uploaded.
- In-player channel drawer, search and channel switching without returning to the dashboard.
- Smart reconnect with 2/4/8-second delays and a three-attempt limit per unstable period. Auth/permission errors and nonstandard 488/511 refusals are not blindly retried. Stable playback resets the counter. User pause is respected. Archive playback may restart from its provider URL where seeking is unsupported.
- Optional last-channel startup; provider metadata for plot, cast, genre, year, director and duration when supplied.
- Local parental PIN with salted PBKDF2, attempt cooldown, five-minute unlock session, section/category locks and protected account/settings changes. This is a local viewing control, not DRM or protection against clearing app data/root access. Keep your PIN; no cloud recovery service is used.
- Connection testing checks account API access and sample transport responses while idle; during playback it inspects the active stream/buffer/codecs without opening a second concurrent stream. Transport success does not prove codec playback. Diagnostics redact URLs and credential fields.
- Floating playback on supported Android 8+ phones/tablets, including media-session play/pause. TV PiP is intentionally not used for video playback. Rotation, language, existing accounts/favorites and verified app updates are preserved.

### Validation limits
Mock-provider tests cover guide parsing, base64/timezones, archive eligibility, Xtream and MAC archive requests, metadata, watch-history rules, PIN hashing, retry bounds and diagnostic redaction. DOM tests cover history/resume, guide access, metadata escaping, protected-section routing and settings. Compilation and these tests do not establish physical-device PiP, playback, native dialogs, seeking or remote behavior. Those still require device and subscription testing. No real provider credentials were available or used during development.

## Version 1.6 — playback and library controls
- Settings > Buffering (also in the player's options) offers Fast, Balanced and Stable profiles. The new profile applies when the next stream opens; it does not interrupt current playback. Media3 uses respective start/rebuffer thresholds of 0.75/1.5, 2.5/5 and 5/10 seconds, with a 64 MiB buffer target. Actual buffering depends on bitrate, available data and memory; larger buffers may increase live delay and cannot compensate for insufficient bandwidth.
- Player options > Audio track / Subtitles lists supported tracks actually reported by the current stream. Choose a track, automatic selection or subtitles off. Choices apply to the current player; a newly opened stream starts with its defaults. The app does not download external subtitles or manufacture extra audio languages.
- The live player has a Previous channel button (↩), a matching menu action and support for the remote's LAST_CHANNEL key. Only successfully prepared live channels enter this session's two-channel recall; retries, catch-up and failed requests do not replace it. Account switches clear recall. Hidden channels are skipped, and parental checks still apply.
- Settings > Organize channels and categories, or the catalogue's edit button, opens account-specific ordering and visibility controls for live TV, movies and series where supported. Search, move up/down/to top, hide/show, and reset work with touch or the remote. Changes save immediately. Hidden entries remain accessible in the organizer for restoration. Filtering/order also apply to home, favorites, history, the in-player drawer and channel up/down. This is a local display preference, not access control; parental PIN protection remains separate.
- Additional JVM tests cover buffer constraints, confirmed-channel recall, order restoration, provider list changes, visibility persistence and resets. Interface tests cover both organizer entry points and English/Arabic buffer-setting round trips. Physical-device track changes, remote controls and provider playback still require device testing.

## Version 1.7 — automatic checks on every opening
The app now checks for a newer stable release whenever it opens or returns from the background, regardless of a legacy disabled automatic-update preference. No 24-hour cooldown remains. The check runs on a worker; no-update and offline failures stay silent. A newer release shows a Download/Later prompt. A result received while hidden or in PiP is retained and offered when the main app is visible again. Duplicate prompts and prompts during an active update download are suppressed. Empty legacy repository settings fall back to the public Sakan IPTV repository. Download and Android installation still require the user's action.

Devices with older installed builds retain their old check schedule until they install 1.7. They can obtain 1.7 immediately through Settings > App updates > Check now or the signed release APK. JVM tests exercise background-result retention, foreground revisit reminders, duplicate suppression, active downloads, repository reset and already-installed versions. Physical-device lifecycle and installer interaction still require device testing.

## Version 1.8 — minimal interface and direct channel switching
- Home contains exactly three text-only sections: Live TV, Series, Movies (Arabic: مباشر، مسلسلات، أفلام). It uses static CSS gradients, with larger headings and a visible keyboard/remote focus outline. English remains the default; Arabic keeps the sidebar on the right.
- Removed the promotional hero, dashboard shelves, fake progress decorations, posters/logos, blur effects and their unused UI code. Deleted the bundled stadium artwork. No provider artwork is requested; CSP and native WebView interception block image/external subresource requests. Required Android launcher/banner identity assets are retained.
- Each library loads when opened, instead of fetching three catalogues at startup. Favorites fetch the needed libraries on demand; history and resume stay accessible from Recent. Provider text, real resume progress, details, parental controls and update checks remain available.
- The live player now has Previous / Channels / Next buttons. CHANNEL_UP/DOWN and MEDIA_NEXT/PREVIOUS work directly; DPAD up/down changes channels when video has focus and player controls are hidden. With controls visible, arrows retain their normal focus behavior. Hidden/locked channels are skipped and both ends wrap. Live channels can load on demand after starting from history, so switching does not require first returning to the catalogue.
- Validation: JVM navigation tests cover wrapping, unavailable current IDs and empty/single-item lists; DOM checks cover the three-section home in both languages, per-section loading, escaped text and image-free cards/details. Build and mock tests do not establish physical TV remote, playback or visual device behavior.

## Version 1.9 — mobile channel grid and account overview
- Home displays local date/time, three section buttons with individual Refresh content actions, provider subscription expiry, Recent channels and Favorite channels. No promotional art or movie posters were restored.
- Live channels use compact logo tiles: two columns on narrow phones, three on wider mobile/landscape layouts and four on larger screens. Logos load lazily with text fallbacks. Native WebView requests remain restricted to registered provider channel-art URLs; no account cookies or bearer headers are added to image requests.
- Live TV has All / Favorite channels / Recent channels filters, plus a mobile category dropdown and larger-screen category chips with counts from the loaded, visible library. Favorites/history remain local and account-specific. Saved favorites can appear and play before loading the full catalogue; older favorites gain their metadata when Live TV is loaded once.
- Refresh clears all cached categories for that section and requests fresh categories/content, with API cache bypass, loading/error feedback and obsolete-response protection. It also refreshes available subscription metadata. Unsupported MAC series stays clearly unavailable. The app can retrieve only content actually exposed by the provider.
- Subscription expiry comes from authenticated Xtream user info or optional MAC account/profile expiry fields. Unknown and explicit unlimited values are distinguished. No dates are guessed from phone numbers or account credentials. Only the normalized date/status is sent to the interface.
- Tests cover expiry parsing/whitelisting, optional MAC account data, safe logo URLs, actual account refresh requests, home date/time, section refresh/error recovery, stale-response handling, category/filter behavior, logo fallbacks and favorites available before catalogue loading. Mock tests and compilation do not establish real-provider metadata accuracy or physical phone/TV rendering and remote behavior.

## Version 1.9.1 — home cleanup
Removed Recent channels from Home. Recent viewing remains available in the sidebar and the Live TV Recent channels filter. Home favorites, clock/date, subscription expiry and section refresh controls remain available.

## Version 1.9.2 — clean home with outline symbols
Removed Favorite channels from Home. Favorites remain accessible through the sidebar and the Live TV Favorite channels filter. The three home sections now have simple inline SVG outline symbols for Live TV, Movies and Series, with no new image files or image downloads. Clock/date, expiry and section refresh controls are retained.

## Version 1.9.3 — full-screen playback and channel actions
- Playback now uses a match-parent video surface. Title, channel buttons, status and programme information are overlays; they no longer reserve space above the video. Android system bars hide during playback and return on exit. Phones enter landscape when playback first opens; the rotation button remains available. Fill/crop is the initial video resize mode, with Fit available in player options.
- Controls appear briefly when playback opens, then hide after three seconds of inactivity, including while paused. Tap video or press remote OK to reveal them. Back first closes a channel panel or hides visible controls, then returns to the library when controls are already hidden. The explicit close button exits playback directly. PiP keeps all controls hidden.
- Channel cards use compact neutral panels with larger logos and separate corner actions. Directional navigation focuses the playback target; the menu is reachable with a long OK press, Context Menu, Shift+F10, long touch, or the visible three-dot button. A long remote press opens its menu on release so the release does not activate a dialog option accidentally. Dragging/scrolling cancels long touch.
- Options include Watch, favorite toggle, guide/catch-up, open category, move to top, hide with confirmation, and parental controls. Existing parental checks and account-local ordering/visibility apply. Favorites and history remain outside Home.
- Validation includes native back-priority policy tests and DOM input tests for short OK, held OK, long touch, scroll cancellation, trailing-click suppression and quick actions. Compilation/mock tests do not establish physical Android TV immersive layout, timing or remote event behavior; those still require device testing.
