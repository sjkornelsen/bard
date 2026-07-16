# Bard

A minimalist audiobook player for Light Phone III.

Bard is designed around the philosophy of Light OS: a calm, text-first interface focused on listening instead of browsing.

It combines library loans and personal audiobooks into a single, distraction-free bookshelf while preserving a native Light Phone experience.

---

## Features

### Library

- Unified audiobook library
- Library loans
- Local MP3 audiobooks
- Local M4B audiobooks
- Books sorted by most recently opened
- Remembers listening progress
- Persistent Now Playing

### Player

- Native Light-style interface
- Scrubbable progress bar
- ±15 second skip
- Variable playback speed
- Resume from last position
- Background playback
- Shared player for all supported sources

### Local Books

Place audiobook files directly into:

```
Light Phone III/Audiobooks
```

Supported formats:

- MP3
- M4B

---

## Philosophy

Bard intentionally avoids the conventions of modern media apps.

There are:

- No cover art
- No recommendations
- No storefront
- No feeds
- No notifications
- No unnecessary settings

Just your books.

---

## Current Status

Bard is currently in **Alpha**.

It is already suitable for daily use, but bugs and breaking changes should be expected while development continues.

---

## Planned

- RSS audiobook feeds
- Multi-file audiobook folders
- Additional playback polish
- Expanded audiobook sources

---

## Building

Requirements:

- Android Studio
- JDK 21
- Android SDK

Clone:

```bash
git clone https://github.com/sjkornelsen/bard.git
```

Build:

```bash
./gradlew assembleDebug
```

---

## Contributing

Bug reports are welcome.

Please include:

- Bard version
- Light Phone III software version
- Steps to reproduce
- Expected behavior
- Actual behavior

---

## Important

Bard is an independent, unofficial project.

Bard currently supports accessing library loans through Libby using a user's own authorized library account. It does not bypass DRM, redistribute library content, proxy Libby traffic, or provide access to books a user has not legitimately borrowed.

Bard is not affiliated with, endorsed by, sponsored by, or supported by Light, OverDrive, or Libby.

Users are responsible for complying with the terms governing any third-party services they choose to use with Bard. OverDrive's terms place restrictions on reverse engineering, modifying, and creating derivative software around its services and software.:contentReference[oaicite:0]{index=0}

Libby is a trademark of OverDrive, Inc. All other trademarks belong to their respective owners.

---

## License

MIT License
