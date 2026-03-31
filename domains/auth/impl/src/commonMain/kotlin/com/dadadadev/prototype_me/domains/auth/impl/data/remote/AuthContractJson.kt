package com.dadadadev.prototype_me.domains.auth.impl.data.remote

import kotlinx.serialization.json.Json

internal object AuthContractJson {
    val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
}
