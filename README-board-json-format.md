# Board JSON Format

This document describes the JSON format used to export and import an ERD board in **PrototypeMe**.

---

## How to Use

### View / Export
1. Open the board.
2. Tap the **`{ }`** button in the bottom toolbar.
3. The **"View / Export"** tab shows the current board as formatted JSON.
4. **Copy to Clipboard** — copies the JSON text.
5. **Save to File** — saves `board.json` to disk:
   - **Android**: system "Create Document" file picker
   - **Desktop (JVM)**: JFileChooser save dialog
   - **Browser (JS / WasmJS)**: browser file download

### Import
1. Tap **`{ }`** → switch to the **"Import"** tab.
2. Paste valid board JSON into the text field.
3. Tap **Import** — the current board is replaced with the pasted content.

> **Warning:** Importing replaces the entire current board. The undo history is cleared.

---

## JSON Schema

### Root Document

```jsonc
{
  "version": 1,           // integer — schema version (currently always 1)
  "nodes": [ ... ],       // array of NodeObject
  "edges": [ ... ]        // array of EdgeObject
}
```

| Field     | Type    | Required | Description                                      |
|-----------|---------|----------|--------------------------------------------------|
| `version` | integer | yes      | Schema version. Currently always `1`.            |
| `nodes`   | array   | yes      | List of entity nodes (can be empty `[]`).        |
| `edges`   | array   | yes      | List of relation edges (can be empty `[]`).      |

---

### NodeObject

```jsonc
{
  "id": "abc123",          // string — unique node identifier
  "name": "User",          // string — display name of the entity
  "x": 120.0,              // float  — canvas X position (board-space units)
  "y": 80.0,               // float  — canvas Y position (board-space units)
  "fields": [ ... ]        // array of FieldObject (optional, default [])
}
```

| Field    | Type   | Required | Description                                                  |
|----------|--------|----------|--------------------------------------------------------------|
| `id`     | string | yes      | Unique identifier. Referenced by edges.                      |
| `name`   | string | yes      | Entity name shown in the card header.                        |
| `x`      | float  | yes      | Horizontal position in board-space (not screen pixels).      |
| `y`      | float  | yes      | Vertical position in board-space (not screen pixels).        |
| `fields` | array  | no       | List of fields. Omit or use `[]` for a header-only entity.   |

---

### FieldObject

```jsonc
{
  "id": "field_1",         // string — unique field identifier within the node
  "name": "email",         // string — display name of the field
  "type": "TEXT"           // string — one of: TEXT, NUMBER, BOOLEAN, DATE
}
```

| Field  | Type   | Required | Description                                                         |
|--------|--------|----------|---------------------------------------------------------------------|
| `id`   | string | yes      | Unique identifier. Referenced by edges as `sourceFieldId` / `targetFieldId`. |
| `name` | string | yes      | Field name shown in the card row.                                   |
| `type` | string | yes      | Data type. Must be one of the values listed below.                  |

**Allowed `type` values:**

| Value     | Meaning              |
|-----------|----------------------|
| `TEXT`    | Free-form string     |
| `NUMBER`  | Numeric value        |
| `BOOLEAN` | True / false flag    |
| `DATE`    | Date / datetime      |

---

### EdgeObject

```jsonc
{
  "id": "edge_1",                // string  — unique edge identifier
  "sourceNodeId": "node_a",      // string  — id of the source node
  "sourceFieldId": "field_1",    // string? — id of the source field (null = entity-level)
  "targetNodeId": "node_b",      // string  — id of the target node
  "targetFieldId": "field_2",    // string? — id of the target field (null = entity-level)
  "label": "has many"            // string? — optional display label on the edge
}
```

| Field          | Type    | Required | Description                                                  |
|----------------|---------|----------|--------------------------------------------------------------|
| `id`           | string  | yes      | Unique edge identifier.                                      |
| `sourceNodeId` | string  | yes      | Must match an `id` in `nodes`.                               |
| `sourceFieldId`| string  | no       | Must match a `fields[].id` in the source node if provided.   |
| `targetNodeId` | string  | yes      | Must match an `id` in `nodes`.                               |
| `targetFieldId`| string  | no       | Must match a `fields[].id` in the target node if provided.   |
| `label`        | string  | no       | Optional text label rendered on the edge line.               |

---

## Full Example

```json
{
  "version": 1,
  "nodes": [
    {
      "id": "node_user",
      "name": "User",
      "x": 100.0,
      "y": 150.0,
      "fields": [
        { "id": "f_id",    "name": "id",    "type": "NUMBER" },
        { "id": "f_email", "name": "email", "type": "TEXT"   },
        { "id": "f_name",  "name": "name",  "type": "TEXT"   }
      ]
    },
    {
      "id": "node_order",
      "name": "Order",
      "x": 380.0,
      "y": 150.0,
      "fields": [
        { "id": "o_id",      "name": "id",       "type": "NUMBER"  },
        { "id": "o_user_id", "name": "userId",   "type": "NUMBER"  },
        { "id": "o_date",    "name": "createdAt","type": "DATE"    }
      ]
    },
    {
      "id": "node_product",
      "name": "Product",
      "x": 380.0,
      "y": 360.0,
      "fields": [
        { "id": "p_id",    "name": "id",    "type": "NUMBER" },
        { "id": "p_name",  "name": "name",  "type": "TEXT"   },
        { "id": "p_price", "name": "price", "type": "NUMBER" }
      ]
    }
  ],
  "edges": [
    {
      "id": "edge_user_order",
      "sourceNodeId": "node_user",
      "sourceFieldId": "f_id",
      "targetNodeId": "node_order",
      "targetFieldId": "o_user_id"
    },
    {
      "id": "edge_order_product",
      "sourceNodeId": "node_order",
      "targetNodeId": "node_product",
      "label": "contains"
    }
  ]
}
```

---

## Minimal Valid Document

A board with no nodes and no edges:

```json
{
  "version": 1,
  "nodes": [],
  "edges": []
}
```

A single node with no fields:

```json
{
  "version": 1,
  "nodes": [
    { "id": "n1", "name": "Entity", "x": 0.0, "y": 0.0 }
  ],
  "edges": []
}
```

---

## Notes

- **IDs** are arbitrary strings. UUIDs are generated automatically when editing the board, but any unique non-empty string is valid on import.
- **Position units** are board-space coordinates, not screen pixels. The visible range depends on the current pan/zoom, but positions around `(0, 0)` to `(1000, 1000)` work well for typical boards.
- **Unknown fields** at any level are silently ignored during import (forward-compatible parsing).
- **Edges referencing non-existent nodes** will be imported but will not be rendered (the geometry lookup will silently fail).
- The `version` field is reserved for future schema migrations and is currently always `1`.
