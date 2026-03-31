# ERD Board Realtime Contract (v1)

This contract is aligned with current app models:
- `ErdBoardAction`
- `ErdEntityNode`
- `ErdRelationEdge`
- lock side effects (`LockRejected`, `ConnectionLost`)

REST part is defined in: `docs/backend/openapi.erd-board.v1.yaml`.

## 1. WebSocket Handshake

`GET /v1/realtime?boardId={boardId}&sessionToken={sessionToken}`

- `boardId` is returned by `POST /v1/boards`.
- `sessionToken` is returned by `POST /v1/boards/{boardId}/join`.

Server returns `101 Switching Protocols` on success.

## 2. Transport Envelope

All client/server WS frames use JSON envelope:

```json
{
  "type": "snapshot.full",
  "requestId": "optional-correlation-id",
  "ts": "2026-03-27T18:24:13.000Z",
  "payload": {}
}
```

Fields:
- `type` (string): event name.
- `requestId` (string, optional): for correlation with client requests.
- `ts` (ISO-8601 string): server/client timestamp.
- `payload` (object): event payload.

## 3. Client -> Server Events

### 3.1 `hello`
Sent once right after WS open.

```json
{
  "type": "hello",
  "payload": {
    "boardId": "brd_01JABCDEF",
    "lastSeenVersion": 42,
    "client": {
      "platform": "web",
      "appVersion": "0.1.0"
    }
  }
}
```

### 3.2 `action.submit`
Submit one board action, schema must match `ErdBoardAction` in OpenAPI.

```json
{
  "type": "action.submit",
  "requestId": "req_123",
  "payload": {
    "action": {
      "type": "MoveNode",
      "actionId": "7dbfcd56-2f50-4f74-a281-94fce9f8a0b0",
      "nodeId": "node_1",
      "newPosition": { "x": 240.0, "y": 120.0 }
    }
  }
}
```

### 3.3 `node.drag.update`
Transient event for live node dragging preview. Server broadcasts it to room participants
without persisting it in board action log and without incrementing board `version`.

```json
{
  "type": "node.drag.update",
  "requestId": "req_drag_1",
  "payload": {
    "nodeId": "node_1",
    "position": { "x": 248.0, "y": 128.0 }
  }
}
```

Rules:
- server allows preview when node has no conflicting lock;
- if node is locked, sender must own that lock;
- server may silently ignore preview if node is missing or locked by another user;
- client should throttle frames to about `20-30 fps`.

### 3.4 `lock.request`
```json
{
  "type": "lock.request",
  "requestId": "req_lock_1",
  "payload": {
    "nodeId": "node_1"
  }
}
```

### 3.5 `lock.release`
```json
{
  "type": "lock.release",
  "requestId": "req_unlock_1",
  "payload": {
    "nodeId": "node_1"
  }
}
```

### 3.6 `ping`
```json
{
  "type": "ping",
  "payload": { "nonce": "abc" }
}
```

## 4. Server -> Client Events

### 4.1 `hello.ack`
```json
{
  "type": "hello.ack",
  "payload": {
    "boardId": "brd_01JABCDEF",
    "serverVersion": 42,
    "userId": "usr_01",
    "displayName": "Alex"
  }
}
```

### 4.2 `snapshot.full`
Full board context (for first connect or hard resync).

```json
{
  "type": "snapshot.full",
  "payload": {
    "version": 42,
    "context": {
      "boardId": "brd_01JABCDEF",
      "nodes": {},
      "edges": {}
    }
  }
}
```

### 4.3 `snapshot.patch`
Optional optimization: list of already-applied actions after `lastSeenVersion`.

```json
{
  "type": "snapshot.patch",
  "payload": {
    "fromVersion": 40,
    "toVersion": 42,
    "applied": [
      {
        "version": 41,
        "actorUserId": "usr_01",
        "action": { "type": "DeleteEdge", "actionId": "...", "edgeId": "edge_1" }
      },
      {
        "version": 42,
        "actorUserId": "usr_02",
        "action": { "type": "AddField", "actionId": "...", "nodeId": "node_1", "field": { "id": "f1", "name": "email", "type": "TEXT" } }
      }
    ]
  }
}
```

### 4.4 `action.applied`
Broadcast to all board participants after server accepts action.

```json
{
  "type": "action.applied",
  "payload": {
    "version": 43,
    "actorUserId": "usr_03",
    "action": {
      "type": "AddEdge",
      "actionId": "79e5d2cb-45bb-4dc6-9604-c0e0716752bd",
      "edge": {
        "id": "edge_44",
        "sourceNodeId": "user",
        "sourceFieldId": "u_id",
        "targetNodeId": "order",
        "targetFieldId": "o_userId",
        "label": null
      }
    }
  }
}
```

### 4.5 `action.rejected`
Returned only to sender.

```json
{
  "type": "action.rejected",
  "requestId": "req_123",
  "payload": {
    "actionId": "7dbfcd56-2f50-4f74-a281-94fce9f8a0b0",
    "reason": "LOCKED",
    "lockedBy": "usr_02"
  }
}
```

### 4.6 `node.drag.updated`
Broadcast preview event for live movement. Does not affect persisted board state by itself.

```json
{
  "type": "node.drag.updated",
  "requestId": "req_drag_1",
  "payload": {
    "boardId": "brd_01JABCDEF",
    "nodeId": "node_1",
    "position": { "x": 248.0, "y": 128.0 },
    "actorUserId": "usr_03"
  }
}
```

### 4.7 `lock.granted`
```json
{
  "type": "lock.granted",
  "requestId": "req_lock_1",
  "payload": {
    "nodeId": "node_1",
    "lockedBy": "usr_01",
    "expiresAt": "2026-03-27T18:25:13.000Z"
  }
}
```

### 4.8 `lock.rejected`
Maps directly to app `BoardSyncEffect.LockRejected`.

```json
{
  "type": "lock.rejected",
  "requestId": "req_lock_1",
  "payload": {
    "nodeId": "node_1",
    "lockedBy": "usr_02"
  }
}
```

### 4.9 `lock.released`
```json
{
  "type": "lock.released",
  "payload": {
    "nodeId": "node_1",
    "releasedBy": "usr_02"
  }
}
```

### 4.10 `presence.updated`
```json
{
  "type": "presence.updated",
  "payload": {
    "boardId": "brd_01JABCDEF",
    "online": [
      { "userId": "usr_01", "displayName": "Alex" },
      { "userId": "usr_02", "displayName": "Sam" }
    ]
  }
}
```

### 4.11 `connection.lost`
Maps to app `BoardSyncEffect.ConnectionLost`.

```json
{
  "type": "connection.lost",
  "payload": {
    "reason": "SERVER_SHUTDOWN"
  }
}
```

### 4.12 `pong`
```json
{
  "type": "pong",
  "payload": { "nonce": "abc" }
}
```

## 5. Versioning and Idempotency Rules

- Server is authoritative and stores monotonic `version` per board.
- `action.actionId` is idempotency key within board scope.
- Re-submitting already applied `actionId` must not duplicate state changes.
- If client reconnects with `lastSeenVersion`, server should:
  - send `snapshot.patch` if gap is available in action log;
  - else send `snapshot.full`.

## 6. Validation Rules (must match current app behavior)

- `AddEdge`: source/target node must exist.
- `sourceFieldId` and `targetFieldId` (if not null) must exist in corresponding nodes.
- Lock conflict should produce `lock.rejected` / `action.rejected` with `lockedBy`.
- `node.drag.update` is ephemeral and should be ignored by reconnect/snapshot sync logic.
- `FieldType` only: `TEXT | NUMBER | BOOLEAN | DATE`.

## 7. Minimal Happy Path

1. `POST /v1/boards` -> receive `approved=true`, `boardId`, `webUrl`, `deepLink`.
2. Open `webUrl`, call `POST /v1/boards/{boardId}/join`.
3. Open WS `/v1/realtime?...` and send `hello`.
4. Receive `snapshot.full`.
5. Send `action.submit` and receive `action.applied` broadcast.
6. Multiple users connected to same board observe same ordered `version` stream.
