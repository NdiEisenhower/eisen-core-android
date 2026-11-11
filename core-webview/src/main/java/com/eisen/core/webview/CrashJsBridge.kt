package com.eisen.core.webview

import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.eisen.core.common.bridge.JsBridge

interface JsErrorListener {
    fun onJsError(message: String, sourceUrl: String, lineNumber: Int, columnNumber: Int, stackTrace: String)
}

class CrashJsBridge(
    private val onErrorListener: JsErrorListener? = null
) : JsBridge("AndroidCrash") {

    companion object {
        const val JS_ERROR_INJECTION = """
            (function() {
                if (window.__eisenErrorHooked) return;
                window.__eisenErrorHooked = true;
                
                var origOnError = window.onerror;
                window.onerror = function(msg, url, line, col, err) {
                    try {
                        if (window.AndroidCrash) {
                            window.AndroidCrash.logJsError(String(msg), String(url || ''), line || 0, col || 0, err && err.stack ? String(err.stack) : '');
                        }
                    } catch(e) {}
                    if (origOnError) return origOnError.apply(this, arguments);
                    return false;
                };

                window.addEventListener('unhandledrejection', function(event) {
                    try {
                        if (window.AndroidCrash) {
                            var reason = event.reason;
                            var msg = reason ? (reason.message || String(reason)) : 'Unhandled Promise Rejection';
                            var stack = reason && reason.stack ? String(reason.stack) : '';
                            window.AndroidCrash.logJsError(msg, '', 0, 0, stack);
                        }
                    } catch(e) {}
                });
            })();
        """
    }

    @JavascriptInterface
    fun logJsError(
        message: String,
        sourceUrl: String,
        lineNumber: Int,
        columnNumber: Int,
        stackTrace: String
    ) {
        Log.e("EisenJsError", "JS Error at $sourceUrl:$lineNumber:$columnNumber -> $message\n$stackTrace")
        onErrorListener?.onJsError(message, sourceUrl, lineNumber, columnNumber, stackTrace)
    }

    fun injectErrorHook(webView: WebView?) {
        webView?.evaluateJavascript(JS_ERROR_INJECTION.trimIndent(), null)
    }
}

