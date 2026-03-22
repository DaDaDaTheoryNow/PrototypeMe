package com.dadadadev.prototype_me.domain.models

enum class FieldType { TEXT, NUMBER, BOOLEAN, DATE }

data class NodeField(
    val id: String,
    val name: String,
    val type: FieldType = FieldType.TEXT,
)
