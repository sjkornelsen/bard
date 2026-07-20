# Bard

*A minimalist audiobook player for the Light Phone III.*

Bard is an audiobook player built specifically for the Light Phone III. It brings local audiobooks, public library loans, and optional RSS audiobook feeds into one calm, text-first interface inspired by the philosophy of Light OS.

Instead of treating audiobooks as another streaming platform, Bard treats them as books. Every supported source appears together in one unified library with one consistent player, allowing you to focus on listening instead of navigating storefronts, recommendations, or media feeds.

There are no recommendations, no cover art, no social features, and no storefronts—just your books.

Bard is currently in **alpha**. While it is already suitable for daily use, features and behavior may continue to evolve before a stable release.

> **Current Status:** Alpha
>
> **Current Version:** 0.1.0-alpha3 (versionCode 3)

---

## Screenshots

<table>
  <tr>
    <td align="center"><img src="docs/screenshots/books.jpg" width="300" alt="Unified Books screen"><br><strong>Books</strong></td>
    <td align="center"><img src="docs/screenshots/player.jpg" width="300" alt="Audiobook Player screen"><br><strong>Player</strong></td>
  </tr>
  <tr>
    <td align="center"><img src="docs/screenshots/settings.jpg" width="300" alt="Bard Settings screen"><br><strong>Settings</strong></td>
    <td align="center"><img src="docs/screenshots/downloads.jpg" width="300" alt="Downloaded audiobooks screen"><br><strong>Downloads</strong></td>
  </tr>
</table>

---

## Highlights

- Unified library for local audiobooks, public library loans, and RSS audiobook feeds
- One consistent player across every supported source
- Native Light-inspired interface
- Background playback with persistent listening progress
- Offline support for local books and downloaded RSS audiobooks
- No cover art, recommendations, advertisements, or storefronts


---

# Features

## Local Audiobooks

Bard supports locally stored audiobooks without requiring a cloud account or subscription.

### Supported Formats

- MP3
- M4B

### Features

- Manual library scanning
- Resume playback
- Persistent listening progress
- Recent-first library ordering
- Unified player interface
- Background playback

Local scanning is intentionally limited to the top level of the shared `Audiobooks` folder. Bard does not scan subfolders or copy books into app-private storage.

---

## Public Library Loans

Bard can optionally connect to your public library audiobook loans so borrowed books appear alongside your local library.

### Features

- Native text-only bookshelf
- Native text-only player
- Loan progress
- Due-date information (when available)
- Play / Pause
- ±15 second skip
- Absolute seeking
- Variable playback speed
- Background playback

Current releases use Libby's **Copy to Another Device** connection process to authorize eligible library loans.

Bard maintains an application-scoped browser session for an authorized library account and uses the provider's official playback system. Bard does not extract protected media files, download library content outside the provider's playback system, bypass DRM or access controls, or replace the provider's playback engine.

Support for public library loans depends on the availability and structure of the provider's web application and may require updates if that service changes.

---

## RSS Audiobooks

Bard also supports standard RSS audiobook feeds.

Each RSS item is treated as an individual audiobook and appears alongside every other supported source.

### Features

- Multiple RSS feeds
- MP3 audio enclosures
- M4B audio enclosures
- Streaming playback
- Optional offline downloads
- Manual refresh
- Persistent listening progress
- Recent-first ordering

Downloads are always initiated by the user.

Removing a downloaded audiobook deletes only Bard's private offline copy while preserving listening progress and feed metadata.

---

## Unified Library

Every supported source appears together in a single Books screen.

There are no:

- Source tabs
- Cover art
- Recommendation feeds
- Storefronts
- Advertisements
- Social features

Books are ordered by most recently played so the titles you're actively listening to are always easiest to reach.

---

## Unified Player

Every audiobook source shares the same player interface.

Features include:

- Play / Pause
- Rewind and forward 15 seconds
- Scrubbable progress line
- Playback speeds:
  - 1×
  - 1.25×
  - 1.5×
  - 1.75×
  - 2×

Bard remembers:

- Listening position
- Playback speed
- Playback duration
- Recently played ordering

After restarting the application or phone, the most recently played audiobook is restored without automatically beginning playback.

Although the interface is identical regardless of source, playback remains source-specific. Local files and RSS audiobooks use Android's native media player, while public library loans continue using the provider's official playback system.

---

# Getting Started

## Local Audiobooks

1. Connect your Light Phone III to your computer.
2. Create an `Audiobooks` folder in shared device storage if it does not already exist.
3. Copy single-file `.mp3` or `.m4b` audiobooks into that folder.
4. In Bard, open:

```
Settings → Local Books → Scan for Books
```

Android may request permission to read your audio library. Bard does **not** request broad "All Files" storage access.

---

## Public Library Loans

1. Open:

```
Settings → Libby → Connect
```

2. On another device already signed into your Libby account, choose **Copy to Another Device**.

3. Enter the setup code displayed by Bard.

Once the authorized session has been established, Bard automatically returns to the Books screen.

Disconnecting clears only Bard's local session and does not disconnect your other authorized devices.

---

## RSS Audiobooks

1. Open:

```
Settings → RSS Feeds → Add Feed
```

2. Enter an HTTP or HTTPS RSS feed URL.

3. Stream immediately or download individual audiobooks for offline listening.

Private RSS feed URLs are stored only in Bard's private application storage. Username/password authenticated feeds are not currently supported.

# Current Limitations

Bard is currently designed for the Light Phone III and Android 13 or newer.

Current limitations include:

- Local audiobooks must be single MP3 or M4B files (multi-file books are not yet supported).
- Chapter navigation is not currently available.
- Cover art is intentionally omitted throughout the interface.
- RSS feeds support one audio enclosure per item.
- Authenticated RSS feeds are not supported.
- RSS feeds do not refresh automatically and never download content without user action.
- Public library setup currently requires another authorized device during initial connection.
- Public library integration depends on the provider's web application and may require updates if that service changes.
- Ebook reading, cloud synchronization, metadata editing, and podcast-specific features are not currently supported.

---

# Architecture

Bard is a native Android application written in Kotlin using Jetpack Compose.

Its architecture is intentionally simple, with separate components responsible for library management, playback, RSS feeds, local audiobook discovery, and persistent user data.

Regardless of source, every audiobook is presented through the same unified library and player interface, providing a consistent listening experience throughout the application.

Bard incorporates selected user-interface resources derived from the Light SDK.

---

# Development

## Requirements

- JDK 17
- Android Studio
- Android SDK (API 36)
- Android device or emulator running Android 13 (API 33) or newer

## Building

Clone the repository:

```bash
git clone https://github.com/sjkornelsen/bard.git
cd bard
```

Build the debug APK:

```bash
./gradlew assembleDebug
```

The generated APK can be found at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Release signing instructions are documented in `RELEASE.md`.

Signing keys, keystores, and `keystore.properties` should never be committed to source control.

---

# Privacy & Security

Bard is designed to keep your audiobook library on your device.

The application:

- does not include analytics or advertising;
- does not create a Bard account;
- does not collect telemetry;
- does not synchronize user data with external servers;
- stores RSS downloads only in Bard's private application storage.

Release builds disable WebView debugging.

Local audiobook files remain in their original location within the shared `Audiobooks` folder and are never copied into Bard's private storage.

When using public library loans, authentication and media delivery continue to be handled by the provider's own systems. Bard is designed not to intentionally collect authentication credentials or protected media URLs.

Android may display a foreground media playback notification while audio is playing. This notification is used solely to keep playback active while the screen is off.

---

# Roadmap

Planned improvements include:

- Multi-file audiobook support
- Chapter navigation
- Improved Bluetooth controls
- Expanded RSS capabilities
- Additional playback refinements
- Improved download management
- Additional audiobook sources
- Performance and stability improvements

---

# Contributing

Contributions, bug reports, feature requests, and suggestions are welcome.

If you encounter a bug, please include:

- Bard version
- Light Phone III software version
- Steps to reproduce
- Expected behavior
- Actual behavior

Before opening an issue, please check whether the problem has already been reported.

---

# Frequently Asked Questions

### Does Bard require an account?

No.

Local audiobooks work entirely offline and do not require an account.

Public library loans require an account with a supported library lending service. RSS feeds do not require a Bard account.

---

### Does Bard collect analytics or usage data?

No.

Bard does not include analytics, advertising, telemetry, or user tracking.

---

### Does Bard support offline listening?

Yes.

Local audiobooks are always available offline. RSS audiobooks may be downloaded for offline playback. Public library loans are streamed only to respect library licensing and content restrictions.

---

# Important

Bard is an independent, unofficial project.

Bard is not affiliated with, endorsed by, sponsored by, supported by, or approved by The Light Phone, Inc., OverDrive, Inc., or any public library system.

Bard does not include or redistribute the Libby Android application, source code, audiobook content, DRM-protected media, user credentials, or authentication data.

Bard's optional public-library functionality requires the user's own authorized library account and uses the provider's official services for authentication and playback. Bard does not provide access to books that have not been legitimately borrowed.

Bard is not designed to extract or host audiobook files, remove or bypass DRM, expose protected media URLs, or provide access to books outside the provider's authorized playback system.

Compatibility with third-party services is not guaranteed and may change over time as those services evolve.

Users are responsible for ensuring that their use of Bard complies with the terms governing any third-party services they choose to access through the application.

Libby and OverDrive are trademarks of OverDrive, Inc.

Light Phone and Light OS are trademarks of The Light Phone, Inc.

Other trademarks and product names belong to their respective owners and are used solely to identify compatibility with third-party products and services.

---

# License

Copyright © 2026 Stan Kornelsen.

The original Bard source code is licensed under the MIT License.

See [LICENSE](LICENSE) for the complete license text.

Bard also incorporates user-interface components and resources derived from the Light SDK. Applicable copyright notices and license terms are reproduced in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

Third-party components remain subject to their own licenses. The Bard license does not grant rights to any third-party software, services, trademarks, or copyrighted content.

---

Built with ❤️ for the Light Phone community.
