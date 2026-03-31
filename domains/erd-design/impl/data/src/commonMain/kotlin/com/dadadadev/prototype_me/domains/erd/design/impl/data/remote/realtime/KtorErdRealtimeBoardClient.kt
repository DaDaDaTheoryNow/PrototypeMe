package com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.realtime

import com.dadadadev.prototype_me.core.common.error.NetworkException
import com.dadadadev.prototype_me.core.common.result.AppResult
import com.dadadadev.prototype_me.core.network.client.safeNetworkCall
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.BoardContractJson
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.realtime.ActionSubmitPayloadDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.realtime.ClientInfoDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.realtime.HelloPayloadDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.realtime.LockNodePayloadDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.realtime.NodeDragUpdatePayloadDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.realtime.PingPayloadDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.realtime.RealtimeClientEnvelopeDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.realtime.RealtimeServerEnvelopeDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.mapper.toDomain
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.mapper.toDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.network.AppliedBoardAction
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.network.ErdBoardRemoteConfig
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.network.RealtimeTransportEvent
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardAction
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.Position
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.time.Clock

@OptIn(ExperimentalUuidApi::class)
internal class KtorErdRealtimeBoardClient(
    private val client: HttpClient,
    private val json: Json = BoardContractJson.json,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : ErdRealtimeBoardClient {

    private val sendMutex = Mutex()
    private val _events = MutableSharedFlow<RealtimeTransportEvent>(
        replay = 8,
        extraBufferCapacity = 64,
    )

    override val events: SharedFlow<RealtimeTransportEvent> = _events.asSharedFlow()

    private var session: DefaultClientWebSocketSession? = null
    private var observeJob: Job? = null

    override suspend fun connect(
        boardId: String,
        sessionToken: String,
        lastSeenVersion: Long?,
    ): AppResult<Unit, NetworkException> = safeNetworkCall {
        log("connect:start boardId=$boardId lastSeenVersion=$lastSeenVersion sessionToken=${sessionToken.masked()}")
        disconnect()
        val realtimeUrl = ErdBoardRemoteConfig.realtimeWebSocketUrl(
            boardId = boardId,
            sessionToken = sessionToken,
        )
        log("connect:url $realtimeUrl")

        val newSession = client.webSocketSession {
            url(realtimeUrl)
        }
        log("connect:ws-open boardId=$boardId")

        session = newSession
        observeJob = scope.launch {
            log("observe:start boardId=$boardId")
            observeIncomingFrames(newSession)
        }

        sendEnvelope(
            RealtimeClientEnvelopeDto.Hello(
                payload = HelloPayloadDto(
                    boardId = boardId,
                    lastSeenVersion = lastSeenVersion,
                    client = ClientInfoDto(
                        platform = "kmp",
                        appVersion = "0.1.0",
                    ),
                ),
            ),
        )
        log("connect:hello-sent boardId=$boardId lastSeenVersion=$lastSeenVersion")
    }

    override suspend fun disconnect() {
        log("disconnect:start")
        observeJob?.cancelAndJoin()
        observeJob = null

        session?.close(CloseReason(CloseReason.Codes.NORMAL, "Client disconnect"))
        session = null
        log("disconnect:done")
    }

    override suspend fun sendAction(
        action: ErdBoardAction,
        requestId: String?,
    ): AppResult<Unit, NetworkException> = safeNetworkCall {
        log("send:action requestId=$requestId action=${action::class.simpleName}")
        sendEnvelope(
            RealtimeClientEnvelopeDto.ActionSubmit(
                requestId = requestId,
                payload = ActionSubmitPayloadDto(action = action.toDto()),
            ),
        )
    }

    override suspend fun requestLock(
        nodeId: String,
        requestId: String?,
    ): AppResult<Unit, NetworkException> = safeNetworkCall {
        log("send:lock-request requestId=$requestId nodeId=$nodeId")
        sendEnvelope(
            RealtimeClientEnvelopeDto.LockRequest(
                requestId = requestId,
                payload = LockNodePayloadDto(nodeId = nodeId),
            ),
        )
    }

    override suspend fun releaseLock(
        nodeId: String,
        requestId: String?,
    ): AppResult<Unit, NetworkException> = safeNetworkCall {
        log("send:lock-release requestId=$requestId nodeId=$nodeId")
        sendEnvelope(
            RealtimeClientEnvelopeDto.LockRelease(
                requestId = requestId,
                payload = LockNodePayloadDto(nodeId = nodeId),
            ),
        )
    }

    override suspend fun ping(
        nonce: String,
        requestId: String?,
    ): AppResult<Unit, NetworkException> = safeNetworkCall {
        log("send:ping requestId=$requestId nonce=$nonce")
        sendEnvelope(
            RealtimeClientEnvelopeDto.Ping(
                requestId = requestId,
                payload = PingPayloadDto(nonce = nonce),
            ),
        )
    }

    override suspend fun sendNodeDragUpdate(
        nodeId: String,
        position: Position,
        requestId: String?,
    ): AppResult<Unit, NetworkException> = safeNetworkCall {
        log("send:node-drag-update requestId=$requestId nodeId=$nodeId position=$position")
        sendEnvelope(
            RealtimeClientEnvelopeDto.NodeDragUpdate(
                requestId = requestId,
                payload = NodeDragUpdatePayloadDto(
                    nodeId = nodeId,
                    position = position.toDto(),
                ),
            ),
        )
    }

    private suspend fun observeIncomingFrames(activeSession: DefaultClientWebSocketSession) {
        try {
            for (frame in activeSession.incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val rawText = frame.readText()
                        log("receive:text ${rawText.compactForLog()}")
                        consumeServerFrame(rawText)
                    }
                    is Frame.Close -> {
                        log("receive:close")
                        _events.emit(
                            RealtimeTransportEvent.ConnectionLost(
                                requestId = null,
                                ts = null,
                                reason = "WS_CLOSED",
                            ),
                        )
                    }

                    else -> log("receive:frame type=${frame.frameType.name}")
                }
            }
        } catch (_: CancellationException) {
            log("observe:cancelled")
        } catch (throwable: Throwable) {
            log(
                buildString {
                    append("observe:error type=${throwable::class.simpleName} message=${throwable.message}")
                    val stack = throwable.stackTraceToString().lineSequence().take(8).joinToString(" | ")
                    if (stack.isNotBlank()) {
                        append(" stack=$stack")
                    }
                },
            )
            _events.emit(
                RealtimeTransportEvent.ConnectionLost(
                    requestId = null,
                    ts = null,
                    reason = throwable::class.simpleName ?: throwable.message ?: "WS_FAILURE",
                ),
            )
        }
    }

    private suspend fun consumeServerFrame(frameText: String) {
        val event = try {
            json.decodeFromString(RealtimeServerEnvelopeDto.serializer(), frameText).toTransportEvent()
        } catch (_: SerializationException) {
            log("receive:unparsed serialization-error")
            parseUnknownEvent(frameText)
        } catch (_: IllegalArgumentException) {
            log("receive:unparsed illegal-argument")
            parseUnknownEvent(frameText)
        }

        log("receive:event ${event.describe()}")
        _events.emit(event)
    }

    private fun parseUnknownEvent(rawFrame: String): RealtimeTransportEvent.Unknown {
        val jsonElement = runCatching { json.decodeFromString<JsonElement>(rawFrame).jsonObject }.getOrNull()
        val type = jsonElement?.get("type")?.jsonPrimitive?.contentOrNull ?: "unknown"
        val requestId = jsonElement?.get("requestId")?.jsonPrimitive?.contentOrNull
        val ts = jsonElement?.get("ts")?.jsonPrimitive?.contentOrNull
        val rawPayload = jsonElement?.get("payload")?.toString() ?: rawFrame

        return RealtimeTransportEvent.Unknown(
            requestId = requestId,
            ts = ts,
            type = type,
            rawPayload = rawPayload,
        )
    }

    private suspend fun sendEnvelope(envelope: RealtimeClientEnvelopeDto) {
        val activeSession = session ?: throw NetworkException.Unknown(
            unknownCause = IllegalStateException("Realtime session is not connected"),
        )

        val payload = json.encodeToString(RealtimeClientEnvelopeDto.serializer(), envelope.withTransportMetadata())
        sendMutex.withLock {
            log("send:frame ${payload.compactForLog()}")
            activeSession.send(Frame.Text(payload))
        }
    }

    private fun RealtimeServerEnvelopeDto.toTransportEvent(): RealtimeTransportEvent = when (this) {
        is RealtimeServerEnvelopeDto.SnapshotFull -> RealtimeTransportEvent.SnapshotFull(
            requestId = requestId,
            ts = ts,
            version = payload.version,
            context = payload.context.toDomain(),
        )

        is RealtimeServerEnvelopeDto.SnapshotPatch -> RealtimeTransportEvent.SnapshotPatch(
            requestId = requestId,
            ts = ts,
            fromVersion = payload.fromVersion,
            toVersion = payload.toVersion,
            applied = payload.applied.map { item ->
                AppliedBoardAction(
                    version = item.version,
                    actorUserId = item.actorUserId,
                    action = item.action.toDomain(),
                )
            },
        )

        is RealtimeServerEnvelopeDto.ActionApplied -> RealtimeTransportEvent.ActionApplied(
            requestId = requestId,
            ts = ts,
            payload = AppliedBoardAction(
                version = payload.version,
                actorUserId = payload.actorUserId,
                action = payload.action.toDomain(),
            ),
        )

        is RealtimeServerEnvelopeDto.ActionRejected -> RealtimeTransportEvent.ActionRejected(
            requestId = requestId,
            ts = ts,
            actionId = payload.actionId,
            reason = payload.reason,
            lockedBy = payload.lockedBy,
        )

        is RealtimeServerEnvelopeDto.LockGranted -> RealtimeTransportEvent.LockGranted(
            requestId = requestId,
            ts = ts,
            nodeId = payload.nodeId,
            lockedBy = payload.lockedBy,
        )

        is RealtimeServerEnvelopeDto.LockRejected -> RealtimeTransportEvent.LockRejected(
            requestId = requestId,
            ts = ts,
            nodeId = payload.nodeId,
            lockedBy = payload.lockedBy,
        )

        is RealtimeServerEnvelopeDto.LockReleased -> RealtimeTransportEvent.LockReleased(
            requestId = requestId,
            ts = ts,
            nodeId = payload.nodeId,
            releasedBy = payload.releasedBy,
        )

        is RealtimeServerEnvelopeDto.PresenceUpdated -> RealtimeTransportEvent.PresenceUpdated(
            requestId = requestId,
            ts = ts,
            boardId = payload.boardId,
            onlineCount = payload.online.size,
        )

        is RealtimeServerEnvelopeDto.NodeDragUpdated -> RealtimeTransportEvent.NodeDragUpdated(
            requestId = requestId,
            ts = ts,
            boardId = payload.boardId,
            nodeId = payload.nodeId,
            position = payload.position.toDomain(),
            actorUserId = payload.actorUserId,
        )

        is RealtimeServerEnvelopeDto.ConnectionLost -> RealtimeTransportEvent.ConnectionLost(
            requestId = requestId,
            ts = ts,
            reason = payload.reason,
        )

        is RealtimeServerEnvelopeDto.HelloAck,
        is RealtimeServerEnvelopeDto.Pong,
        -> RealtimeTransportEvent.Unknown(
            requestId = requestId,
            ts = ts,
            type = this::class.simpleName.orEmpty(),
            rawPayload = this.toString(),
        )
    }

    private fun log(message: String) {
        println("[erd-realtime-client] $message")
    }

    private fun RealtimeClientEnvelopeDto.withTransportMetadata(): RealtimeClientEnvelopeDto {
        val requestId = requestId ?: "req_${Uuid.random().toString().replace("-", "").take(12)}"
        val timestamp = ts ?: Clock.System.now().toString()
        return when (this) {
            is RealtimeClientEnvelopeDto.Hello -> copy(requestId = requestId, ts = timestamp)
            is RealtimeClientEnvelopeDto.ActionSubmit -> copy(requestId = requestId, ts = timestamp)
            is RealtimeClientEnvelopeDto.LockRequest -> copy(requestId = requestId, ts = timestamp)
            is RealtimeClientEnvelopeDto.LockRelease -> copy(requestId = requestId, ts = timestamp)
            is RealtimeClientEnvelopeDto.Ping -> copy(requestId = requestId, ts = timestamp)
            is RealtimeClientEnvelopeDto.NodeDragUpdate -> copy(requestId = requestId, ts = timestamp)
        }
    }

    private fun String.masked(): String =
        if (length <= 8) "***" else "${take(4)}...${takeLast(4)}"

    private fun String.compactForLog(maxLength: Int = 240): String {
        val compact = replace('\n', ' ').replace(Regex("\\s+"), " ").trim()
        return if (compact.length <= maxLength) compact else compact.take(maxLength) + "..."
    }

    private fun RealtimeTransportEvent.describe(): String = when (this) {
        is RealtimeTransportEvent.SnapshotFull ->
            "SnapshotFull version=$version nodes=${context.nodes.size} edges=${context.edges.size}"
        is RealtimeTransportEvent.SnapshotPatch ->
            "SnapshotPatch fromVersion=$fromVersion toVersion=$toVersion applied=${applied.size}"
        is RealtimeTransportEvent.ActionApplied ->
            "ActionApplied version=${payload.version} action=${payload.action::class.simpleName}"
        is RealtimeTransportEvent.ActionRejected ->
            "ActionRejected actionId=$actionId reason=$reason lockedBy=$lockedBy"
        is RealtimeTransportEvent.LockGranted ->
            "LockGranted nodeId=$nodeId lockedBy=$lockedBy"
        is RealtimeTransportEvent.LockRejected ->
            "LockRejected nodeId=$nodeId lockedBy=$lockedBy"
        is RealtimeTransportEvent.LockReleased ->
            "LockReleased nodeId=$nodeId releasedBy=$releasedBy"
        is RealtimeTransportEvent.PresenceUpdated ->
            "PresenceUpdated boardId=$boardId onlineCount=$onlineCount"
        is RealtimeTransportEvent.NodeDragUpdated ->
            "NodeDragUpdated boardId=$boardId nodeId=$nodeId actorUserId=$actorUserId position=$position"
        is RealtimeTransportEvent.ConnectionLost ->
            "ConnectionLost reason=$reason"
        is RealtimeTransportEvent.Unknown ->
            "Unknown type=$type requestId=$requestId"
    }
}
