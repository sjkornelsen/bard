# Bard Repository Guide

## Product Direction

Bard is a minimalist, text-only audiobook player for Light Phone.

- Do not display or introduce cover art.
- Local audiobook files are the primary source.
- Libby is an optional source configured in Settings.
- RSS feeds are optional sources configured in Settings.
- Present books from every enabled source together in one unified Books screen.
- Use one shared native player UI regardless of the active source.
- Keep the interface focused, readable, and appropriate for a small monochrome-style display.

## Current Architecture

This repository is currently a single-module Kotlin Android application using Jetpack Compose. It is an early Libby-focused prototype rather than the final source-independent architecture.

- `LibbyLightApplication` initializes the application-scoped Libby WebView.
- `LibbyWebPlayer` owns one persistent WebView, its cookies and DOM storage, Libby navigation, login-state detection, and shelf scraping.
- `LibbyBridge` injects JavaScript into Libby/OverDrive player frames and communicates through `JavaScriptReplyProxy`.
- `PlayerDebugScreen` currently combines prototype navigation, Libby shelf presentation, WebView synchronization, and native playback controls.
- `LoanItem` and `PlayerState` are prototype Libby-oriented models.
- The `ui` package contains Light-style Compose primitives that are not yet consistently used by the active screen.

There is not yet a local-file source, RSS source, database, repository layer, playback service, MediaSession, or source-independent player abstraction.

## Architectural Direction

Evolve toward source-independent domain boundaries without rewriting the working Libby integration prematurely.

- Model books with stable, source-qualified identities.
- Define source-neutral library and playback models for native UI consumption.
- Keep local, Libby, and RSS behavior behind source-specific adapters.
- Treat the local folder as the default and primary library provider.
- Merge enabled sources at the repository/domain layer, not independently in the UI.
- Keep source-specific setup, authentication, refresh, and error handling in Settings or source adapters.
- Keep the Books and Player screens unaware of WebView, RSS, and filesystem implementation details.
- Introduce abstractions only when required by an implemented feature; avoid speculative layers and unnecessary dependencies.

For eventual playback architecture, use a shared native player contract with source-specific engines:

- Local and RSS playback may use an appropriate native Android media engine.
- Libby playback must continue to be owned by its persistent hidden WebView.
- The common native UI should project state and send commands through the active engine.

## Libby Integration Invariants

Preserve the existing hidden Libby playback architecture unless a task explicitly requires a carefully scoped change.

- Retain the application-scoped persistent `LibbyWebPlayer` WebView and its session storage.
- Retain document-start injection into the cross-origin Libby/OverDrive audiobook frame.
- Retain the WebMessage listener and reply-proxy communication model.
- Retain direct `BIF.objects.spool` playback controls, including `toggle`, `seekBy`, and `_setPlaybackRate`.
- Preserve the existing DOM-button fallbacks where present.
- Do not replace Libby playback with direct media URL extraction, downloading, proxying, or reimplementation.
- Do not expose the Libby WebView during normal Books or Player use. It may be shown for setup, authentication, consent, diagnostics, or recovery.
- Keep user-facing Libby language source-specific; the application itself is named Bard.
- Treat Libby DOM selectors, routes, ARIA labels, and private BIF methods as a fragile adapter boundary. Contain compatibility work there.

Changes to bridge origins, injected script timing, WebView lifetime, context attachment, cookies, DOM storage, frame selection, or route invalidation are playback-sensitive and require focused verification.

## Naming and Dependencies

- Do not rename existing package names, the application ID, or Kotlin classes yet.
- User-facing application text should say Bard; internal legacy identifiers may remain until a dedicated migration.
- Do not add a dependency when Android or the existing dependency set can reasonably support the feature.
- When a dependency is necessary, keep it narrowly scoped and explain why it is needed.
- Avoid broad framework adoption solely to reorganize the prototype.

## Privacy and Security

Never log, persist in diagnostics, expose in UI error details, or include in test fixtures:

- Credentials or passwords
- Cookies or cookie headers
- Libby setup or verification codes
- Authentication or refresh tokens
- Signed, expiring, or authenticated media URLs
- Authorization headers
- Session storage or local-storage contents
- Personally identifying library account data

Sanitize URLs before logging when they may contain query parameters, fragments, signatures, or tokens. Prefer route names, hostnames, opaque internal IDs, and boolean status fields over raw payloads. Do not add JavaScript diagnostics that dump browser storage, network requests, or authentication state.

Keep bridge origin allowlists as narrow as functionality permits. Do not enable additional WebView capabilities without a concrete requirement and security review.

## UI Rules

- UI is text-only; do not fetch, cache, render, or reserve layout space for cover art.
- Prefer the existing Light-style Compose primitives where they fit the intended interaction.
- Keep navigation shallow and controls usable on a small portrait display.
- Do not fork player presentation by source. Source-specific status or recovery actions may be shown without creating separate player screens.
- Ensure optional sources do not block local-library use when disconnected, expired, offline, or malformed.

## Change Discipline

- Inspect relevant call sites before changing WebView, bridge, library, or playback behavior.
- Keep changes scoped to the requested feature and preserve unrelated work in the working tree.
- Do not commit unless explicitly asked.
- Add tests around source normalization, unified-library merging, playback contracts, and Libby message parsing as those boundaries are introduced.
- For Libby changes, verify login/session persistence, shelf discovery, loan opening, play/pause, 15-second seeking, speed selection, Activity recreation, and background playback as applicable.
- Run the narrowest relevant checks during development and `./gradlew assembleDebug` before handing off an application-code change when the environment permits.
