package com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto

import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.realtime.RealtimeClientEnvelopeDto
import com.dadadadev.prototype_me.domains.erd.design.impl.data.remote.dto.realtime.RealtimeServerEnvelopeDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

internal object BoardContractJson {
    val serializersModule: SerializersModule = SerializersModule {
        polymorphic(ErdBoardActionDto::class) {
            subclass(ErdBoardActionDto.MoveNodeDto::class)
            subclass(ErdBoardActionDto.AddNodeDto::class)
            subclass(ErdBoardActionDto.DeleteNodeDto::class)
            subclass(ErdBoardActionDto.AddEdgeDto::class)
            subclass(ErdBoardActionDto.DeleteEdgeDto::class)
            subclass(ErdBoardActionDto.AddFieldDto::class)
            subclass(ErdBoardActionDto.RemoveFieldDto::class)
            subclass(ErdBoardActionDto.RenameFieldDto::class)
        }

        polymorphic(RealtimeClientEnvelopeDto::class) {
            subclass(RealtimeClientEnvelopeDto.Hello::class)
            subclass(RealtimeClientEnvelopeDto.ActionSubmit::class)
            subclass(RealtimeClientEnvelopeDto.LockRequest::class)
            subclass(RealtimeClientEnvelopeDto.LockRelease::class)
            subclass(RealtimeClientEnvelopeDto.Ping::class)
            subclass(RealtimeClientEnvelopeDto.NodeDragUpdate::class)
        }

        polymorphic(RealtimeServerEnvelopeDto::class) {
            subclass(RealtimeServerEnvelopeDto.HelloAck::class)
            subclass(RealtimeServerEnvelopeDto.SnapshotFull::class)
            subclass(RealtimeServerEnvelopeDto.SnapshotPatch::class)
            subclass(RealtimeServerEnvelopeDto.ActionApplied::class)
            subclass(RealtimeServerEnvelopeDto.ActionRejected::class)
            subclass(RealtimeServerEnvelopeDto.LockGranted::class)
            subclass(RealtimeServerEnvelopeDto.LockRejected::class)
            subclass(RealtimeServerEnvelopeDto.LockReleased::class)
            subclass(RealtimeServerEnvelopeDto.PresenceUpdated::class)
            subclass(RealtimeServerEnvelopeDto.ConnectionLost::class)
            subclass(RealtimeServerEnvelopeDto.Pong::class)
            subclass(RealtimeServerEnvelopeDto.NodeDragUpdated::class)
        }
    }

    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        explicitNulls = false
        classDiscriminator = "type"
        serializersModule = BoardContractJson.serializersModule
    }
}
