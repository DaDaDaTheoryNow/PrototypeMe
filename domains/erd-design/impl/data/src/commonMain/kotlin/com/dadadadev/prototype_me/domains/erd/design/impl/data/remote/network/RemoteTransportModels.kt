package com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.network

internal sealed interface ActionSubmitOutcome {
    data class Accepted(
        val actionId: String,
        val serverVersion: Long,
    ) : ActionSubmitOutcome

    data class Rejected(
        val actionId: String,
        val reason: String,
        val lockedBy: String?,
    ) : ActionSubmitOutcome
}

internal sealed interface LockRequestOutcome {
    data class Granted(
        val nodeId: String,
        val lockedBy: String,
    ) : LockRequestOutcome

    data class Rejected(
        val nodeId: String,
        val lockedBy: String,
    ) : LockRequestOutcome
}

internal data class AppliedBoardAction(
    val version: Long,
    val actorUserId: String,
    val action: com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardAction,
)

internal sealed interface RealtimeTransportEvent {
    val requestId: String?
    val ts: String?

    data class SnapshotFull(
        override val requestId: String?,
        override val ts: String?,
        val version: Long,
        val context: com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardContext,
    ) : RealtimeTransportEvent

    data class SnapshotPatch(
        override val requestId: String?,
        override val ts: String?,
        val fromVersion: Long,
        val toVersion: Long,
        val applied: List<AppliedBoardAction>,
    ) : RealtimeTransportEvent

    data class ActionApplied(
        override val requestId: String?,
        override val ts: String?,
        val payload: AppliedBoardAction,
    ) : RealtimeTransportEvent

    data class ActionRejected(
        override val requestId: String?,
        override val ts: String?,
        val actionId: String,
        val reason: String,
        val lockedBy: String?,
    ) : RealtimeTransportEvent

    data class LockGranted(
        override val requestId: String?,
        override val ts: String?,
        val nodeId: String,
        val lockedBy: String,
    ) : RealtimeTransportEvent

    data class LockRejected(
        override val requestId: String?,
        override val ts: String?,
        val nodeId: String,
        val lockedBy: String,
    ) : RealtimeTransportEvent

    data class LockReleased(
        override val requestId: String?,
        override val ts: String?,
        val nodeId: String,
        val releasedBy: String,
    ) : RealtimeTransportEvent

    data class PresenceUpdated(
        override val requestId: String?,
        override val ts: String?,
        val boardId: String,
        val onlineCount: Int,
    ) : RealtimeTransportEvent

    data class NodeDragUpdated(
        override val requestId: String?,
        override val ts: String?,
        val boardId: String,
        val nodeId: String,
        val position: com.dadadadev.prototype_me.domains.erd.design.api.domain.model.Position,
        val actorUserId: String,
    ) : RealtimeTransportEvent

    data class ConnectionLost(
        override val requestId: String?,
        override val ts: String?,
        val reason: String,
    ) : RealtimeTransportEvent

    data class Unknown(
        override val requestId: String?,
        override val ts: String?,
        val type: String,
        val rawPayload: String,
    ) : RealtimeTransportEvent
}
