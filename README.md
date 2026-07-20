# Bard

Bard is a minimalist, text-only audiobook player for the Light Phone III. It
combines local MP3/M4B audiobooks, optional Libby loans, and optional RSS
audiobooks in one Books screen with a shared native player.

## Features

- One calm, text-only Books screen across every enabled source.
- One native player with play/pause, ±15 seconds, scrubbing, speed control, and
  durable listening progress.
- Single-file local MP3 and M4B audiobooks from the device's `Audiobooks`
  folder.
- Optional RSS audiobook streaming and explicit offline downloads.
- Optional Libby loans connected through an existing account's eight-digit
  Copy To Another Device setup code.
- Background playback and source-independent Now Playing restoration.

Libby uses one application-scoped persistent WebView hidden behind Bard's native
interface. Connect it from Settings using Libby's eight-digit Copy To Another
Device setup code. Local books belong directly in the device's `Audiobooks`
folder. RSS feeds are configured in Settings and stream unless explicitly
downloaded.

While Libby is playing, Android displays a private foreground playback
notification. This keeps the hidden WebView playback process active when the
screen is off.

## Alpha Status

Bard is early alpha software intended for private testing. Local books support
one MP3 or M4B file per audiobook. Multi-file books, chapter navigation,
authenticated RSS feeds, and automatic RSS downloads are not supported yet.

## Important

Bard is an independent, unofficial open-source project.

Bard is **not affiliated with, endorsed by, sponsored by, or approved by
Light, OverDrive, or Libby.**

Bard does not include or redistribute the Libby Android application, Libby
source code, audiobook content, DRM-protected media, or user credentials.

When using library loans, Bard relies on the user's own authorized Libby
account and the official Libby web service. Bard does not provide access to
books that a user has not legitimately borrowed.

Bard does not bypass or remove DRM, proxy audiobook content, or host
copyrighted library materials.

Users are responsible for ensuring that their use of Bard complies with the
terms and conditions governing any third-party services they choose to access
through the application.

"Libby" and "OverDrive" are trademarks of OverDrive, Inc. "Light" and "Light
Phone" are trademarks of their respective owners. All trademarks are used
solely for identification and compatibility purposes and remain the property
of their respective owners.

## Third-Party Components

Bard includes user interface components derived from the Light SDK.

Portions of the user interface and associated resources are licensed under the
MIT License.

The complete copyright notices and license text for those components are in
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

## License

Original source code written specifically for Bard is licensed under the MIT
License. See [LICENSE](LICENSE).

Third-party components remain subject to their respective licenses.

## Development

The Android project requires JDK 17. Build a debug APK with:

```sh
./gradlew assembleDebug
```

Private release signing and installation instructions are in `RELEASE.md`.
