package com.dadadadev.prototype_me.domains.erd.design.api.domain.model

enum class FieldType { TEXT, NUMBER, BOOLEAN, DATE }

data class ErdNodeField(
    val id: String,
    val name: String,
    val type: FieldType = FieldType.TEXT,
)

