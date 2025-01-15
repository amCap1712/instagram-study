package com.rutgers.smdr.webview.client;

import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient

class LoggingWebChromeClient: WebChromeClient() {

    override fun onConsoleMessage(msg: ConsoleMessage): Boolean {
        val text = "${msg.message()} -- From line " + "${msg.lineNumber()} of ${msg.sourceId()}"
        Log.i("RequestBridge", text)
        return true
    }

}
