# LibbyLight

Step-1 prototype of a Light Phone-styled Libby client.

This build intentionally shows Libby's real web app in one application-scoped
persistent Android WebView. It enables JavaScript, DOM storage, cookies, and
background media playback, and logs a small JavaScript `ping` result every five
seconds.

## Test

1. Install and open LibbyLight.
2. Complete Libby setup/sign-in in the visible WebView.
3. Open an audiobook loan and start playback.
4. Press Home and confirm playback continues.
5. Reopen the app and confirm the Libby session remains available.
6. In Logcat, filter for `LibbyLight` or `LibbyWebPlayer` to see page URLs and
   JavaScript bridge pings.

The old Audible-specific library/player implementation was removed from this
copy so the Step-1 project has no unresolved Audible bridge calls. The original
uploaded project remains unchanged.
