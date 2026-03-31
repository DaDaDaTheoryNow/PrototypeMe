package com.dadadadev.prototype_me.domains.auth.impl.data.local

import kotlin.js.ExperimentalWasmJsInterop

internal actual class PlatformSecureTokenVault actual constructor() {
    actual suspend fun write(value: String) {
        saveToken(value)
    }

    actual suspend fun read(): String? = readToken()

    actual suspend fun clear() {
        clearToken()
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """(value) => {
        localStorage.setItem('prototype_me.auth.token', value);
    }"""
)
private external fun saveToken(value: String)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """() => {
        return localStorage.getItem('prototype_me.auth.token');
    }"""
)
private external fun readToken(): String?

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """() => {
        localStorage.removeItem('prototype_me.auth.token');
    }"""
)
private external fun clearToken()
