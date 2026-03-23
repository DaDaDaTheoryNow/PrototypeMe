package com.dadadadev.prototype_me.domains.board.sysdesign.api.model

import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardEntity
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardPoint
import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSize

enum class SystemComponentType {
    CLIENT,
    CDN,
    API_GATEWAY,
    SERVICE,
    CACHE,
    DATABASE,
    QUEUE,
    OBJECT_STORAGE,
}

data class NetworkPort(
    val number: Int,
    val protocol: String,
)

data class SystemDesignNode(
    override val id: String,
    override val position: BoardPoint,
    override val size: BoardSize,
    val title: String,
    val componentType: SystemComponentType,
    val region: String,
    val availabilityZone: String,
    val replicas: Int = 1,
    val ports: List<NetworkPort> = emptyList(),
) : BoardEntity
