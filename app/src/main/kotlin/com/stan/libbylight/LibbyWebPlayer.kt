package com.stan.libbylight

import android.annotation.SuppressLint
import android.content.Context
import android.content.MutableContextWrapper
import android.util.Log
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONArray
import org.json.JSONObject
import com.stan.libbylight.library.LoanItem

private const val TAG = "LibbyWebPlayer"
private const val LIBBY_ROOT = "https://libbyapp.com/"
private const val LIBBY_SHELF = "https://libbyapp.com/shelf"

/**
 * Application-scoped Libby browser session.
 *
 * Step 1 intentionally exposes Libby's real UI. Later steps can add a
 * JavaScript bridge and native Light-style shelf/player screens without
 * replacing this persistent WebView or its cookies/local storage.
 */
object LibbyWebPlayer {

    private var webView: WebView? = null
    private var applicationContext: Context? = null

    @SuppressLint("SetJavaScriptEnabled")
    fun init(context: Context) {
        if (webView != null) return
        applicationContext = context.applicationContext

        val cookies = CookieManager.getInstance()
        cookies.setAcceptCookie(true)

        val contextWrapper = MutableContextWrapper(context.applicationContext)
        WebView.setWebContentsDebuggingEnabled(true)

        webView = WebView(contextWrapper).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.loadsImagesAutomatically = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = false
            settings.setSupportZoom(false)
            settings.builtInZoomControls = false
            settings.displayZoomControls = false
            isVerticalScrollBarEnabled = true
            isHorizontalScrollBarEnabled = false
            isFocusable = true
            isFocusableInTouchMode = true

            cookies.setAcceptThirdPartyCookies(this, true)

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    Log.d(TAG, "onPageFinished: $url")
                    CookieManager.getInstance().flush()
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                    Log.d(
                        TAG,
                        "JS ${consoleMessage.messageLevel()}: ${consoleMessage.message()} " +
                            "(${consoleMessage.sourceId()}:${consoleMessage.lineNumber()})"
                    )
                    return true
                }
            }

            // Install the cross-origin player-frame bridge before any Libby URL loads.
            LibbyBridge.install(this)
        }
    }

    fun attachToActivity(activityContext: Context): WebView {
        val current = requireWebView()
        (current.context as? MutableContextWrapper)?.baseContext = activityContext
        current.requestFocus()
        return current
    }

    fun requireWebView(): WebView =
        webView ?: error("LibbyWebPlayer.init(context) must be called before use")

    fun detachFromCurrentParent() {
        val current = webView ?: return
        (current.parent as? ViewGroup)?.removeView(current)
        applicationContext?.let { appContext ->
            (current.context as? MutableContextWrapper)?.baseContext = appContext
        }
    }

    fun loadLoginPage() {
        requireWebView().loadUrl(LIBBY_ROOT)
    }

    fun loadLibraryPage() {
        requireWebView().loadUrl(LIBBY_SHELF)
    }

    fun openLoan(loanUrl: String) {
        require(loanUrl.startsWith("https://libbyapp.com/open/loan/")) {
            "Expected a Libby loan URL"
        }
        requireWebView().loadUrl(loanUrl)
    }


    fun scrapeAudiobookLoans(onResult: (List<LoanItem>) -> Unit) {
        val script = """
            (() => {
                const tiles = Array.from(document.querySelectorAll(
                    '.title-tile.data-tile-class_loan.data-title-tile-format_audiobook'
                ));

                const loans = tiles.map(tile => {
                    const classText = tile.className || '';
                    const titleIdMatch = classText.match(/data-title_(\d+)/);
                    const titleId = titleIdMatch ? titleIdMatch[1] : '';
                    const returnLink = tile.querySelector(
                        'a[href*="/shelf/loans/"][href$="/return"]'
                    );
                    const returnHref = returnLink ? returnLink.getAttribute('href') || '' : '';
                    const loanMatch = returnHref.match(/\/shelf\/loans\/(\d+)-(\d+)\/return/);
                    const cardId = loanMatch ? loanMatch[1] : '';
                    const resolvedTitleId = loanMatch ? loanMatch[2] : titleId;
                    const title = (tile.querySelector('.title-tile-title')?.textContent || '').trim();
                    const author = (tile.querySelector('.title-tile-author')?.textContent || '').trim();
                    const cover = tile.querySelector('img[data-cover-id]')?.src || '';
                    const due = (tile.querySelector('a[href$="/return"] .title-tile-journey-link')?.textContent || '').trim();
                    const progress = (tile.querySelector('.title-tile-journey-percent')?.textContent || '').trim();
                    const loanUrl = cardId && resolvedTitleId
                        ? 'https://libbyapp.com/open/loan/' + cardId + '/' + resolvedTitleId
                        : '';

                    return { title, author, loanUrl, coverUrl: cover, dueText: due, progressText: progress };
                }).filter(item => item.title && item.loanUrl);

                return JSON.stringify(loans);
            })();
        """.trimIndent()

        requireWebView().evaluateJavascript(script) { raw ->
            try {
                val decoded = decodeJavascriptString(raw) ?: return@evaluateJavascript onResult(emptyList())
                val array = JSONArray(decoded)
                val items = buildList {
                    for (index in 0 until array.length()) {
                        val json = array.getJSONObject(index)
                        add(
                            LoanItem(
                                title = json.optString("title"),
                                author = json.optString("author"),
                                loanUrl = json.optString("loanUrl"),
                                coverUrl = json.optString("coverUrl"),
                                dueText = json.optString("dueText"),
                                progressText = json.optString("progressText"),
                            )
                        )
                    }
                }
                Log.d(TAG, "scrapeAudiobookLoans: ${items.size} loans")
                onResult(items)
            } catch (error: Exception) {
                Log.e(TAG, "scrapeAudiobookLoans failed", error)
                onResult(emptyList())
            }
        }
    }

    /** Basic Step-1 bridge diagnostic for Logcat. */
    fun ping(onResult: (String?) -> Unit) {
        val script = """
            (() => JSON.stringify({
                title: document.title,
                url: window.location.href,
                readyState: document.readyState
            }))();
        """.trimIndent()
        requireWebView().evaluateJavascript(script) { raw ->
            onResult(decodeJavascriptString(raw))
        }
    }

    /**
     * Libby does not expose a single stable login cookie. For this prototype,
     * reaching a normal Libby app route after initial setup counts as an
     * authenticated/initialized session. The actual page remains visible, so
     * the user can finish any card or verification step normally.
     */
    fun checkIsLoggedIn(onResult: (Boolean) -> Unit) {
        val script = """
            (() => JSON.stringify({
                url: window.location.href,
                title: document.title,
                ready: document.documentElement.classList.contains('data-client_ready'),
                realm: Array.from(document.documentElement.classList)
                    .some(c => c === 'data-realm_shelf' || c === 'data-realm_open')
            }))();
        """.trimIndent()

        requireWebView().evaluateJavascript(script) { raw ->
            try {
                val decoded = decodeJavascriptString(raw) ?: return@evaluateJavascript onResult(false)
                val json = JSONObject(decoded)
                val url = json.optString("url")
                val initialized = json.optBoolean("ready") &&
                    (json.optBoolean("realm") || url.contains("/shelf") || url.contains("/open/loan/"))
                Log.d(TAG, "checkIsLoggedIn: url=$url initialized=$initialized")
                onResult(initialized)
            } catch (error: Exception) {
                Log.e(TAG, "checkIsLoggedIn failed", error)
                onResult(false)
            }
        }
    }

    suspend fun checkIsLoggedInSuspend(): Boolean =
        kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            checkIsLoggedIn { result -> continuation.resume(result) { } }
        }

    private fun decodeJavascriptString(raw: String?): String? {
        if (raw == null || raw == "null") return null
        return try {
            // evaluateJavascript returns a JSON-encoded JavaScript string.
            org.json.JSONTokener(raw).nextValue() as? String
        } catch (_: Exception) {
            raw.removeSurrounding("\"").replace("\\\"", "\"")
        }
    }
}
