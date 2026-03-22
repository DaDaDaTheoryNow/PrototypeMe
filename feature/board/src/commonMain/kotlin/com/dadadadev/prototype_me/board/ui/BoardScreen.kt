package com.dadadadev.prototype_me.board.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadadev.prototype_me.board.presentation.BoardIntent
import com.dadadadev.prototype_me.board.presentation.BoardSideEffect
import com.dadadadev.prototype_me.board.presentation.BoardViewModel
import com.dadadadev.prototype_me.domain.models.EntityNode
import com.dadadadev.prototype_me.domain.models.FieldType
import com.dadadadev.prototype_me.domain.models.NodeField
import com.dadadadev.prototype_me.domain.models.Position
import com.dadadadev.prototype_me.domain.models.RelationEdge
import com.dadadadev.prototype_me.domain.models.RelationType
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectSideEffect
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun BoardScreen(viewModel: BoardViewModel = koinViewModel()) {
    val state by viewModel.container.stateFlow.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val density = LocalDensity.current.density
    val textMeasurer = rememberTextMeasurer()

    var showAddNodeDialog by remember { mutableStateOf(false) }
    var newNodeName by remember { mutableStateOf("") }

    val isConnecting = state.connectingFromNodeId != null

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is BoardSideEffect.ShowLockError ->
                snackbarHostState.showSnackbar("Node locked by ${effect.lockedBy}")
            is BoardSideEffect.ShowConnectionLost ->
                snackbarHostState.showSnackbar("Connection lost — reconnecting…")
        }
    }

    // Pre-compute edge midpoints for hit testing (in composition phase)
    val edgeMidpoints = remember(state.edges, state.nodes, state.scale, state.panOffset, density) {
        state.edges.mapValues { (_, edge) ->
            computeEdgeAnchors(edge, state.nodes, state.scale, state.panOffset, density)
                ?.let { (src, tgt) -> Offset((src.x + tgt.x) / 2f, (src.y + tgt.y) / 2f) }
        }
    }

    // Fields highlighted by the currently selected edge
    val highlightedFields = remember(state.selectedEdgeId, state.edges) {
        val edge = state.edges[state.selectedEdgeId]
        buildSet {
            edge?.sourceFieldId?.let { add(it) }
            edge?.targetFieldId?.let { add(it) }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        viewModel.onIntent(BoardIntent.OnPanZoom(centroid, pan, zoom))
                    }
                }
                // Canvas tap: deselect edge + cancel pending connection
                .pointerInput(edgeMidpoints, state.selectedEdgeId) {
                    detectTapGestures { tap ->
                        val tapped = edgeMidpoints.entries.firstOrNull { (_, midpoint) ->
                            if (midpoint == null) return@firstOrNull false
                            val dx = tap.x - midpoint.x
                            val dy = tap.y - midpoint.y
                            sqrt(dx * dx + dy * dy) < 28f
                        }
                        if (tapped != null) {
                            viewModel.onIntent(BoardIntent.OnSelectEdge(tapped.key))
                        } else {
                            viewModel.onIntent(BoardIntent.OnSelectEdge(null))
                            if (isConnecting) viewModel.onIntent(BoardIntent.OnCancelConnect)
                        }
                    }
                }
        ) {
            val screenW = constraints.maxWidth.toFloat()
            val screenH = constraints.maxHeight.toFloat()

            val visibleNodes by remember(state.nodes, state.panOffset, state.scale) {
                derivedStateOf {
                    val margin = 200f
                    state.nodes.values.filter { node ->
                        val sx = node.position.x * state.scale + state.panOffset.x
                        val sy = node.position.y * state.scale + state.panOffset.y
                        sx > -margin && sx < screenW + margin &&
                            sy > -margin && sy < screenH + margin
                    }
                }
            }

            // ── Canvas: dot grid + edges ──────────────────────────────────────
            val capturedState = state
            val capturedDensity = density
            val capturedTextMeasurer = textMeasurer

            Canvas(modifier = Modifier.fillMaxSize()) {
                // ── Dot grid ─────────────────────────────────────────────────
                val gridStep = 32f * capturedState.scale
                val offsetX = capturedState.panOffset.x % gridStep
                val offsetY = capturedState.panOffset.y % gridStep
                val dotRadius = (1.5f * capturedState.scale).coerceIn(1f, 3f)
                var x = offsetX
                while (x < size.width) {
                    var y = offsetY
                    while (y < size.height) {
                        drawCircle(color = Color(0xFFDDDDDD), radius = dotRadius, center = Offset(x, y))
                        y += gridStep
                    }
                    x += gridStep
                }

                // ── Edges ─────────────────────────────────────────────────────
                val targetPortCount = mutableMapOf<String, Int>()
                val edgePortIndex = mutableMapOf<String, Int>()
                capturedState.edges.values.forEach { edge ->
                    val key = "${edge.targetNodeId}:${edge.targetFieldId ?: "c"}"
                    val idx = targetPortCount[key] ?: 0
                    edgePortIndex[edge.id] = idx
                    targetPortCount[key] = idx + 1
                }

                capturedState.edges.values.forEach { edge ->
                    val anchors = computeEdgeAnchors(
                        edge, capturedState.nodes,
                        capturedState.scale, capturedState.panOffset, capturedDensity
                    ) ?: return@forEach

                    val portIdx = edgePortIndex[edge.id] ?: 0
                    val spreadOffset = portIdx * 5f
                    val src = anchors.first
                    val tgt = anchors.second.copy(y = anchors.second.y + spreadOffset)

                    val isSelected = edge.id == capturedState.selectedEdgeId
                    val edgeColor = if (isSelected) Color(0xFF111111) else Color(0xFF666666)
                    val strokeWidth = if (isSelected) 2.5f else 2f

                    // Bezier path
                    val dx = abs(tgt.x - src.x)
                    val controlDist = (dx * 0.5f).coerceIn(60f, 300f)
                    val srcDir = if (src.x < tgt.x) 1f else -1f
                    val c1 = Offset(src.x + srcDir * controlDist, src.y)
                    val c2 = Offset(tgt.x - srcDir * controlDist, tgt.y)

                    val path = Path().apply {
                        moveTo(src.x, src.y)
                        cubicTo(c1.x, c1.y, c2.x, c2.y, tgt.x, tgt.y)
                    }

                    val pathStyle = if (edge.type == RelationType.INHERITS) {
                        Stroke(
                            width = strokeWidth,
                            join = StrokeJoin.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f))
                        )
                    } else {
                        Stroke(width = strokeWidth, join = StrokeJoin.Round)
                    }

                    drawPath(path, edgeColor, style = pathStyle)

                    // Crow's Foot notation
                    drawSourceNotation(src, c1, edge.type, edgeColor, strokeWidth)
                    drawTargetNotation(tgt, c2, edge.type, edgeColor, strokeWidth)

                    // Relation type label at midpoint
                    val mid = Offset((src.x + tgt.x) / 2f, (src.y + tgt.y) / 2f)
                    val labelText = edge.type.label()
                    val labelStyle = TextStyle(
                        fontSize = 9.sp,
                        color = if (isSelected) Color(0xFF333333) else Color(0xFF999999),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    val measured = capturedTextMeasurer.measure(labelText, labelStyle)
                    // Small white background pill behind label
                    drawRect(
                        color = Color.White.copy(alpha = 0.85f),
                        topLeft = Offset(
                            mid.x - measured.size.width / 2f - 3f,
                            mid.y - measured.size.height / 2f - 2f
                        ),
                        size = Size(
                            measured.size.width + 6f,
                            measured.size.height + 4f
                        )
                    )
                    drawText(
                        measured,
                        topLeft = Offset(
                            mid.x - measured.size.width / 2f,
                            mid.y - measured.size.height / 2f
                        )
                    )

                    // Selected midpoint dot
                    if (isSelected) {
                        drawCircle(Color(0xFF111111), radius = 4f, center = mid)
                    }
                }
            }

            // ── Entity cards ──────────────────────────────────────────────────
            visibleNodes.forEach { node ->
                EntityCard(
                    nodeId = node.id,
                    stateFlow = viewModel.container.stateFlow,
                    isSourceNode = state.connectingFromNodeId == node.id,
                    isConnecting = isConnecting,
                    onDragStart = { viewModel.onIntent(BoardIntent.OnDragStart(node.id)) },
                    onDrag = { delta -> viewModel.onIntent(BoardIntent.OnDragNode(node.id, delta)) },
                    onDragEnd = { viewModel.onIntent(BoardIntent.OnDragEnd(node.id)) },
                    onLongPress = { viewModel.onIntent(BoardIntent.OnSelectNode(node.id)) },
                    onHeaderPortTap = { viewModel.onIntent(BoardIntent.OnNodeTap(node.id)) },
                    onFieldPortTap = { fieldId -> viewModel.onIntent(BoardIntent.OnNodeFieldTap(node.id, fieldId)) },
                    highlightedFieldIds = highlightedFields
                )
            }

            // ── Connection-in-progress hint ────────────────────────────────────
            if (isConnecting) {
                val srcName = state.nodes[state.connectingFromNodeId]?.let { n ->
                    val field = n.fields.firstOrNull { it.id == state.connectingFromFieldId }
                    if (field != null) "${n.name}.${field.name}" else n.name
                }
                val hintText = if (srcName == null) "Tap a port dot to start"
                else "From: $srcName  →  tap target port"

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .background(Color(0xFF111111), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(hintText, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            // ── Edge selection toolbar ────────────────────────────────────────
            val selEdge = state.edges[state.selectedEdgeId]
            if (selEdge != null) {
                val midpoint = edgeMidpoints[selEdge.id]
                val toolbarX = ((midpoint?.x ?: (screenW / 2f)) - 80f).coerceIn(4f, screenW - 180f)
                val toolbarY = ((midpoint?.y ?: (screenH - 80f)) - 52f).coerceIn(4f, screenH - 52f)

                Box(
                    modifier = Modifier
                        .offset { IntOffset(toolbarX.roundToInt(), toolbarY.roundToInt()) }
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = selEdge.type.label(),
                            fontSize = 11.sp,
                            color = Color(0xFF444444),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        TextButton(onClick = {
                            viewModel.onIntent(BoardIntent.OnChangeEdgeType(selEdge.id, selEdge.type.next()))
                        }) {
                            Text("⇄", color = Color(0xFF333333), fontSize = 13.sp)
                        }
                        TextButton(onClick = {
                            viewModel.onIntent(BoardIntent.OnDeleteEdge(selEdge.id))
                        }) {
                            Text("✕", color = Color(0xFFAAAAAA), fontSize = 13.sp)
                        }
                    }
                }
            }

            // ── Bottom toolbar ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .background(Color.White, RoundedCornerShape(28.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { newNodeName = ""; showAddNodeDialog = true }) {
                        Text("+ Add Node", color = Color(0xFF111111), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { data -> Snackbar(snackbarData = data) }
    }

    // ── Add Node dialog ───────────────────────────────────────────────────────
    if (showAddNodeDialog) {
        val panOffset = state.panOffset
        val scale = state.scale
        AlertDialog(
            onDismissRequest = { showAddNodeDialog = false },
            title = { Text("New Node", fontWeight = FontWeight.SemiBold, color = Color(0xFF111111)) },
            text = {
                OutlinedTextField(
                    value = newNodeName, onValueChange = { newNodeName = it },
                    placeholder = { Text("Node name", color = Color(0xFFAAAAAA)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF111111),
                        unfocusedBorderColor = Color(0xFFCCCCCC),
                        cursorColor = Color(0xFF111111)
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showAddNodeDialog = false
                    val cx = (400f - panOffset.x) / scale
                    val cy = (300f - panOffset.y) / scale
                    viewModel.onIntent(BoardIntent.OnAddNode(newNodeName.ifBlank { "Node" }, Position(cx, cy)))
                    newNodeName = ""
                }) { Text("Add", color = Color(0xFF111111), fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showAddNodeDialog = false }) {
                    Text("Cancel", color = Color(0xFF888888))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // ── Node field editor dialog ──────────────────────────────────────────────
    val selNodeId = state.selectedNodeId
    val selNode = state.nodes[selNodeId]
    if (selNodeId != null && selNode != null) {
        NodeDetailDialog(
            nodeName = selNode.name,
            fields = selNode.fields,
            onAddField = { name, type -> viewModel.onIntent(BoardIntent.OnAddField(selNodeId, name, type)) },
            onRemoveField = { fieldId -> viewModel.onIntent(BoardIntent.OnRemoveField(selNodeId, fieldId)) },
            onDismiss = { viewModel.onIntent(BoardIntent.OnSelectNode(null)) }
        )
    }
}

// ── Anchor computation ────────────────────────────────────────────────────────

/**
 * Computes screen-space source and target anchor points for an edge.
 * Smart anchoring: source uses right port when left of target, else left port.
 */
fun computeEdgeAnchors(
    edge: RelationEdge,
    nodes: Map<String, EntityNode>,
    scale: Float,
    panOffset: Offset,
    density: Float,
): Pair<Offset, Offset>? {
    val srcNode = nodes[edge.sourceNodeId] ?: return null
    val tgtNode = nodes[edge.targetNodeId] ?: return null

    val cardW = CARD_WIDTH_DP * density * scale
    val headerH = CARD_HEADER_DP * density * scale
    val dividerH = CARD_DIVIDER_DP * density * scale
    val rowH = CARD_FIELD_ROW_DP * density * scale

    fun anchorY(node: EntityNode, fieldId: String?): Float {
        val base = node.position.y * scale + panOffset.y
        return if (fieldId == null) {
            base + headerH / 2f
        } else {
            val idx = node.fields.indexOfFirst { it.id == fieldId }.coerceAtLeast(0)
            base + headerH + dividerH + rowH * idx + rowH / 2f
        }
    }

    val srcCenterX = srcNode.position.x * scale + panOffset.x + cardW / 2f
    val tgtCenterX = tgtNode.position.x * scale + panOffset.x + cardW / 2f

    val srcX: Float
    val tgtX: Float
    if (srcCenterX < tgtCenterX) {
        srcX = srcNode.position.x * scale + panOffset.x + cardW   // right
        tgtX = tgtNode.position.x * scale + panOffset.x            // left
    } else {
        srcX = srcNode.position.x * scale + panOffset.x            // left
        tgtX = tgtNode.position.x * scale + panOffset.x + cardW   // right
    }

    return Pair(
        Offset(srcX, anchorY(srcNode, edge.sourceFieldId)),
        Offset(tgtX, anchorY(tgtNode, edge.targetFieldId))
    )
}

// ── Crow's Foot Notation ──────────────────────────────────────────────────────

private fun DrawScope.drawSourceNotation(
    point: Offset, controlPoint: Offset,
    type: RelationType, color: Color, strokeWidth: Float
) {
    when (type) {
        RelationType.ONE_TO_ONE, RelationType.ONE_TO_MANY -> drawSingleBar(point, controlPoint, color, strokeWidth)
        RelationType.MANY_TO_MANY -> drawCrowsFoot(point, controlPoint, color, strokeWidth)
        RelationType.INHERITS -> { /* no source marker for inheritance */ }
    }
}

private fun DrawScope.drawTargetNotation(
    point: Offset, controlPoint: Offset,
    type: RelationType, color: Color, strokeWidth: Float
) {
    when (type) {
        RelationType.ONE_TO_ONE -> drawSingleBar(point, controlPoint, color, strokeWidth)
        RelationType.ONE_TO_MANY, RelationType.MANY_TO_MANY -> drawCrowsFoot(point, controlPoint, color, strokeWidth)
        RelationType.INHERITS -> drawInheritArrow(point, controlPoint, color, strokeWidth)
    }
}

/** Single perpendicular bar — "one" cardinality */
private fun DrawScope.drawSingleBar(end: Offset, prevPoint: Offset, color: Color, sw: Float) {
    val len = 9f
    val angle = atan2(end.y - prevPoint.y, end.x - prevPoint.x)
    val perp = angle + PI.toFloat() / 2f
    drawLine(
        color,
        Offset(end.x + len * cos(perp), end.y + len * sin(perp)),
        Offset(end.x - len * cos(perp), end.y - len * sin(perp)),
        strokeWidth = sw, cap = StrokeCap.Round
    )
}

/** Crow's foot — three lines + crossbar — "many" cardinality */
private fun DrawScope.drawCrowsFoot(end: Offset, prevPoint: Offset, color: Color, sw: Float) {
    val len = 14f
    val spread = 0.42f
    val angle = atan2(end.y - prevPoint.y, end.x - prevPoint.x)
    val back = Offset(end.x - len * cos(angle), end.y - len * sin(angle))
    // centre + two spread lines
    drawLine(color, end, back, strokeWidth = sw, cap = StrokeCap.Round)
    drawLine(color, end,
        Offset(end.x - len * cos(angle - spread), end.y - len * sin(angle - spread)),
        strokeWidth = sw, cap = StrokeCap.Round)
    drawLine(color, end,
        Offset(end.x - len * cos(angle + spread), end.y - len * sin(angle + spread)),
        strokeWidth = sw, cap = StrokeCap.Round)
    // crossbar at back
    val perp = angle + PI.toFloat() / 2f
    val barLen = 8f
    drawLine(
        color,
        Offset(back.x + barLen * cos(perp), back.y + barLen * sin(perp)),
        Offset(back.x - barLen * cos(perp), back.y - barLen * sin(perp)),
        strokeWidth = sw, cap = StrokeCap.Round
    )
}

/** Open triangle arrow — INHERITS */
private fun DrawScope.drawInheritArrow(end: Offset, prevPoint: Offset, color: Color, sw: Float) {
    val len = 16f
    val halfW = 9f
    val angle = atan2(end.y - prevPoint.y, end.x - prevPoint.x)
    val perp = angle + PI.toFloat() / 2f
    val base = Offset(end.x - len * cos(angle), end.y - len * sin(angle))
    val left = Offset(base.x + halfW * cos(perp), base.y + halfW * sin(perp))
    val right = Offset(base.x - halfW * cos(perp), base.y - halfW * sin(perp))
    val path = Path().apply {
        moveTo(end.x, end.y); lineTo(left.x, left.y); lineTo(right.x, right.y); close()
    }
    drawPath(path, Color.White, style = Fill)
    drawPath(path, color, style = Stroke(width = sw, join = StrokeJoin.Round))
}

// ── RelationType helpers ──────────────────────────────────────────────────────

private fun RelationType.label() = when (this) {
    RelationType.ONE_TO_ONE   -> "1:1"
    RelationType.ONE_TO_MANY  -> "1:N"
    RelationType.MANY_TO_MANY -> "M:N"
    RelationType.INHERITS     -> "ISA"
}

private fun RelationType.next() = when (this) {
    RelationType.ONE_TO_ONE   -> RelationType.ONE_TO_MANY
    RelationType.ONE_TO_MANY  -> RelationType.MANY_TO_MANY
    RelationType.MANY_TO_MANY -> RelationType.INHERITS
    RelationType.INHERITS     -> RelationType.ONE_TO_ONE
}

// ── Node field editor dialog ──────────────────────────────────────────────────

@Composable
private fun NodeDetailDialog(
    nodeName: String,
    fields: List<NodeField>,
    onAddField: (name: String, type: FieldType) -> Unit,
    onRemoveField: (fieldId: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var newFieldName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(FieldType.TEXT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(12.dp),
        title = { Text(nodeName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF111111)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (fields.isNotEmpty()) {
                    Text("Fields", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFAAAAAA), modifier = Modifier.padding(bottom = 4.dp))
                    fields.forEach { field ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(field.name, fontSize = 13.sp, color = Color(0xFF333333), modifier = Modifier.weight(1f))
                            Text(field.type.name.lowercase(), fontSize = 11.sp, color = Color(0xFFAAAAAA),
                                modifier = Modifier.padding(horizontal = 8.dp))
                            TextButton(onClick = { onRemoveField(field.id) }) {
                                Text("✕", color = Color(0xFFCCCCCC), fontSize = 12.sp)
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEEEEEE))
                }
                Text("Add field", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFAAAAAA), modifier = Modifier.padding(bottom = 6.dp))
                OutlinedTextField(
                    value = newFieldName, onValueChange = { newFieldName = it },
                    placeholder = { Text("Field name", color = Color(0xFFCCCCCC)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF111111),
                        unfocusedBorderColor = Color(0xFFDDDDDD),
                        cursorColor = Color(0xFF111111)
                    )
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FieldType.entries.forEach { type ->
                        val sel = selectedType == type
                        TextButton(
                            onClick = { selectedType = type },
                            modifier = Modifier.background(
                                if (sel) Color(0xFF111111) else Color(0xFFF0F0F0),
                                RoundedCornerShape(16.dp)
                            )
                        ) {
                            Text(type.name.lowercase(), fontSize = 11.sp,
                                color = if (sel) Color.White else Color(0xFF555555))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        if (newFieldName.isNotBlank()) {
                            onAddField(newFieldName.trim(), selectedType)
                            newFieldName = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().background(Color(0xFF111111), RoundedCornerShape(8.dp))
                ) {
                    Text("Add field", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = Color(0xFF111111), fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
