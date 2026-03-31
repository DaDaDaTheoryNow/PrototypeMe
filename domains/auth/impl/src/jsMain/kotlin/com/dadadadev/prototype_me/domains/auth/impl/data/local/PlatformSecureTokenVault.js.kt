package com.dadadadev.prototype_me.domains.auth.impl.data.local

import kotlinx.browser.window

internal actual class PlatformSecureTokenVault actual constructor() {
    actual suspend fun write(value: String) {
        window.localStorage.setItem(TOKEN_KEY, value)
    }

    actual suspend fun read(): String? {
        return window.localStorage.getItem(TOKEN_KEY)
    }

    actual suspend fun clear() {
        window.localStorage.removeItem(TOKEN_KEY)
    }

    private companion object {
        const val TOKEN_KEY = "prototype_me.auth.token"
    }
}
