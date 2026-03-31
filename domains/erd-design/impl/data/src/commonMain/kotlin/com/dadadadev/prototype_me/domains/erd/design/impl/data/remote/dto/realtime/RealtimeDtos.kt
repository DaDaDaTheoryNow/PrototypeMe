package com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.realtime

import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.ErdBoardActionDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.BoardPointDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.ErdBoardContextDto
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("type")
sealed class RealtimeClientEnvelopeDto {
    abstract val requestId: String?
    abstract val ts: String?

    @Serializable
    @SerialName("hello")
    data class Hello(
        override val requestId: String? = null,
        override val ts: String? = null,
        val payload: HelloPayloadDto,
    ) : RealtimeClientEnvelopeDto()

    @Serializable
    @SerialName("action.submit")
    data class ActionSubmit(
        override val requestId: String? = null,
        override val ts: String? = null,
        val payload: ActionSubmitPayloadDto,
    ) : RealtimeClientEnvelopeDto()

    @Serializable
    @SerialName("lock.request")
    data class LockRequest(
        override val requestId: String? = null,
        override val ts: String? = null,
        val payload: LockNodePayloadDto,
    ) : RealtimeClientEnvelopeDto()

    @Serializable
    @SerialName("lock.release")
    data class LockRelease(
        override val requestId: String? = null,
        override val ts: String? = null,
        val payload: LockNodePayloadDto,
    ) : RealtimeClientEnvelopeDto()

    @Serializable
    @SerialName("ping")
    data class Ping(
        override val requestId: String? = null,
        override val ts: String? = null,
        val payload: PingPayloadDto,
    ) : RealtimeClientEnvelopeDto()

    @Serializable
    @SerialName("node.drag.update")
    data class NodeDragUpdate(
        override val requestId: String? = null,
        override val ts: String? = null,
        val payload: NodeDragUpdatePayloadDto,
    ) : RealtimeClientEnvelopeDto()
}

@Serializable
@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("type")
sealed class RealtimeServerEnvelopeDto {
    abstract val requestId: String?
    abstract val ts: String?

    @Serializable
    @SerialName("hello.ack")
    data class HelloAck(
        override val requestId: String? = null,
        override val ts: String? = null,
        val payload: HelloAckPayloadDto,
    ) : RealtimeServerEnvelopeDto()

    @Serializable
    @SerialName("snapshot.full")
    data class SnapshotFull(
        override val requestId: String? = null,
        override val ts: String? = null,
        val payload: SnapshotFullPayloadDto,
    ) : RealtimeServerEnvelopeDto()

    @Serializable
    @SerialName("snapshot.patch")
    data class SnapshotPatch(
        override val requestId: String? = null,
        override val ts: String? = null,
        val payload: SnapshotPatchPayloadDto,
    ) : RealtimeServerEnvelopeDto()

    @Serializable
    @SerialName("action.applied")
    data class ActionApplied(
        override val requestId: String? = null,
        override val ts: String? = null,
        val payload: ActionAppliedPayloadDto,
    ) : RealtimeServerEnvelopeDto()

    @Serializable
    @SerialName("action.rejected")
    data class ActionRejected(
        override val requestId: String? = null,
        override val ts: String? = null,
        val payload: ActionRejectedPayloadDto,
    ) : RealtimeServerEnvelopeDto()

    @Serializable
    @SerialName("lock.granted")
    data class LockGranted(
        override val requestId: String? = null,
        override val ts: String? = null,
        val payload: LockGrantedPayloadDto,
    ) : RealtimeServerEnvelopeDto()

    @Serializable
    @SerialName("lock.rejected")
    data class LockRejected(
        override val requestId: String? = null,
        override val ts: String? = null,
        val payload: LockRejectedPayloadDto,
    ) : RealtimeServerEnvelopeDto()

    @Serializable
    @SerialName("lock.released")
    data class LockReleased(
        override val requestId: String? = null,
        override val ts: String? = null,
        val payload: LockReleasedPayloadDto,
    ) : RealtimeServerEnvelopeDto()

    @Serializable
    @SerialName("presence.updated")
    data class PresenceUpdated(
        override val requestId: String? = null,
        override val ts: String? = null,
        val payload: PresenceUpdatedPayloadDto,
    ) : RealtimeServerEnvelopeDto()

    @Serializable
    @SerialName("connection.lost")
    data class ConnectionLost(
        override val requestId: String? = null,
        override val ts: String? = null,
        val payload: ConnectionLostPayloadDto,
    ) : RealtimeServerEnvelopeDto()

    @Serializable
    @SerialName("pong")
    data class Pong(
        override val requestId: String? = null,
        override val ts: String? = null,
        val payload: PongPayloadDto,
    ) : RealtimeServerEnvelopeDto()

    @Serializable
    @SerialName("node.drag.updated")
    data class NodeDragUpdated(
        override val requestId: String? = null,
        override val ts: String? = null,
        val payload: NodeDragUpdatedPayloadDto,
    ) : RealtimeServerEnvelopeDto()
}

@Serializable
data class ClientInfoDto(
    val platform: String,
    val appVersion: String,
)

@Serializable
data class HelloPayloadDto(
    val boardId: String,
    val lastSeenVersion: Long? = null,
    val client: ClientInfoDto,
)

@Serializable
data class ActionSubmitPayloadDto(
    val action: ErdBoardActionDto,
)

@Serializable
data class LockNodePayloadDto(
    val nodeId: String,
)

@Serializable
data class PingPayloadDto(
    val nonce: String,
)

@Serializable
data class NodeDragUpdatePayloadDto(
    val nodeId: String,
    val position: BoardPointDto,
)

@Serializable
data class HelloAckPayloadDto(
    val boardId: String,
    val serverVersion: Long,
    val userId: String,
    val displayName: String,
)

@Serializable
data class SnapshotFullPayloadDto(
    val version: Long,
    val context: ErdBoardContextDto,
)

@Serializable
data class SnapshotPatchPayloadDto(
    val fromVersion: Long,
    val toVersion: Long,
    val applied: List<AppliedActionVersionDto>,
)

@Serializable
data class AppliedActionVersionDto(
    val version: Long,
    val actorUserId: String,
    val action: ErdBoardActionDto,
)

@Serializable
data class ActionAppliedPayloadDto(
    val version: Long,
    val actorUserId: String,
    val action: ErdBoardActionDto,
)

@Serializable
data class ActionRejectedPayloadDto(
    val actionId: String,
    val reason: String,
    val lockedBy: String? = null,
)

@Serializable
data class LockGrantedPayloadDto(
    val nodeId: String,
    val lockedBy: String,
    val expiresAt: String,
)

@Serializable
data class LockRejectedPayloadDto(
    val nodeId: String,
    val lockedBy: String,
)

@Serializable
data class LockReleasedPayloadDto(
    val nodeId: String,
    val releasedBy: String,
)

@Serializable
data class PresenceUpdatedPayloadDto(
    val boardId: String,
    val online: List<PresenceUserDto>,
)

@Serializable
data class PresenceUserDto(
    val userId: String,
    val displayName: String,
)

@Serializable
data class ConnectionLostPayloadDto(
    val reason: String,
)

@Serializable
data class PongPayloadDto(
    val nonce: String,
)

@Serializable
data class NodeDragUpdatedPayloadDto(
    val boardId: String,
    val nodeId: String,
    val position: BoardPointDto,
    val actorUserId: String,
)
