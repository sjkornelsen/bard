# Bard

A minimalist audiobook player for Light Phone III.

Bard combines borrowed and owned audiobooks into a single, distraction-free library while preserving the simple, text-first design language of Light OS.

Current supported sources:

- Libby
- Local MP3 audiobooks
- Local M4B audiobooks

---

## Philosophy

Bard is designed around the same principles as the Light Phone:

- Text-first interface
- No cover art
- No recommendations
- No storefront
- No feeds
- No unnecessary settings
- Fast, calm, and predictable

Your library is simply a list of books.

Tap one.

Listen.

---

## Features

### Library

- Native Light-style bookshelf
- Unified library across multiple sources
- Books sorted by most recently opened
- Remembers reading progress
- Persistent "Now Playing"

### Libby

- Native Connect to Libby using the 8-digit setup code
- Hidden Libby WebView
- Native browsing and playback UI
- Automatic shelf synchronization
- Persistent login
- Background playback

### Local Books

Place audiobook files inside:

```
Light Phone III/Audiobooks
```

Supported formats:

- MP3
- M4B

Features:

- Automatic scanning
- Metadata extraction
- Persistent listening progress
- Background playback

---

## Player

- Native Light-style player
- Scrubbable progress bar
- ±15 second skip
- Variable playback speed
- Resume from last position
- Remembers playback speed
- Works with both Libby and local books

---

## Current Status

Bard is currently in **Alpha**.

It is already suitable for daily use, but bugs and breaking changes should be expected while development continues.

---

## Planned Features

- RSS audiobook feeds
- Multi-file audiobook folders
- Improved library management
- Additional playback polish

---

## Building

Requirements:

- Android Studio
- JDK 21
- Android SDK
- Light Phone III SDK (for Light UI components)

Clone the repository:

```bash
git clone https://github.com/sjkornelsen/bard.git
```

Build:

```bash
./gradlew assembleDebug
```

---

## Contributing

Bard is currently a personal project and is evolving quickly.

Bug reports are always welcome.

When reporting an issue, please include:

- Bard version
- Light Phone III software version
- Steps to reproduce
- Expected behavior
- Actual behavior

---

## Disclaimer

Bard is an independent project.

It is not affiliated with, endorsed by, or sponsored by:

- Light
- OverDrive
- Libby

Libby is a trademark of OverDrive, Inc.
