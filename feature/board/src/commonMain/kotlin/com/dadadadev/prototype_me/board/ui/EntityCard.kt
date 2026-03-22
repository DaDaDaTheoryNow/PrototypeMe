package com.dadadadev.prototype_me.board.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadadev.prototype_me.board.presentation.BoardState
import com.dadadadev.prototype_me.domain.models.EntityNode
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

// в”Ђв”Ђ Layout constants вЂ” must match actual dp values in the layout below в”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђ
const val CARD_WIDTH_DP = 160f
const val CARD_HEADER_DP = 44f    // Row height(44.dp)
const val CARD_FIELD_ROW_DP = 28f // Row height(28.dp)
const val CARD_DIVIDER_DP = 1f

@Composable
fun EntityCard(
    nodeId: String,
    stateFlow: StateFlow<BoardState>,
    isSourceNode: Boolean,
    isConnecting: Boolean,
    onDragStart: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    highlightedFieldIds: Set<String> = emptySet(),
) {
    val node by produceState<EntityNode?>(null, nodeId, stateFlow) {
        stateFlow.map { it.nodes[nodeId] }.collect { value = it }
    }
    val scale by produceState(1f, stateFlow) {
        stateFlow.map { it.scale }.collect { value = it }
    }
    val panOffset by produceState(Offset.Zero, stateFlow) {
        stateFlow.map { it.panOffset }.collect { value = it }
    }

    val currentNode = node ?: return
    val isLocked = currentNode.lockedBy != null

    val borderColor = when {
        isSourceNode -> Color(0xFF111111)
        isLocked     -> Color(0xFFBBBBBB)
        else         -> Color(0xFFDDDDDD)
    }
    val borderWidth = if (isSourceNode) 2.dp else 1.dp
    val bgColor = if (isSourceNode) Color(0xFFF8F8F8) else Color.White

    Box(
        modifier = Modifier.graphicsLayer {
            translationX = currentNode.position.x * scale + panOffset.x
            translationY = currentNode.position.y * scale + panOffset.y
            scaleX = scale
            scaleY = scale
            transformOrigin = TransformOrigin(0f, 0f)
        }
    ) {
        Card(
            modifier = Modifier
                .width(CARD_WIDTH_DP.dp)
                // в”Ђв”Ђ Single combined gesture handler: tap / long-press / drag в”Ђв”Ђ
                // Consumes DOWN immediately so parent canvas handler won't pan.
                .pointerInput(nodeId, scale) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = true)
                        down.consume() // в†ђ prevents canvas pan

                        val downPos = down.position
                        var isDragging = false
                        var released = false

                        // Race: long-press timeout vs pointer movement/release
                        val completed = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.find { it.id == down.id } ?: break
                                if (!change.pressed) {
                                    released = true
                                    change.consume()
                                    break
                                }
                                val dist = (change.position - downPos).getDistance()
                                if (!isDragging && dist > viewConfiguration.touchSlop) {
                                    isDragging = true
                                    onDragStart()
                                }
                                if (isDragging) {
                                    change.consume()
                                    onDrag(Offset((change.position.x - change.previousPosition.x) * scale, (change.position.y - change.previousPosition.y) * scale))
                                }
                            }
                        }

                        if (isDragging) {
                            // Was dragging вЂ” if not yet released, keep tracking
                            if (!released) {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.find { it.id == down.id } ?: break
                                    if (!change.pressed) { change.consume(); break }
                                    change.consume()
                                    onDrag(Offset((change.position.x - change.previousPosition.x) * scale, (change.position.y - change.previousPosition.y) * scale))
                                }
                            }
                            onDragEnd()
                        } else if (completed == null) {
                            // Timeout with no drag в†’ long press
                            onLongPress()
                        } else if (released) {
                            // Released before timeout and before slop в†’ tap
                            onTap()
                        }
                    }
                },
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(borderWidth, borderColor),
            colors = CardDefaults.cardColors(containerColor = bgColor),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isSourceNode) 6.dp else 2.dp)
        ) {
            Column {
                // в”Ђв”Ђ Header в”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђ
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(CARD_HEADER_DP.dp)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentNode.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isLocked) Color(0xFF888888) else Color(0xFF111111),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isLocked) {
                            Text(
                                text = "* ${currentNode.lockedBy}",
                                fontSize = 9.sp,
                                color = Color(0xFFBBBBBB),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // в”Ђв”Ђ Fields в”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђв”Ђ
                if (currentNode.fields.isNotEmpty()) {
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = CARD_DIVIDER_DP.dp)
                    currentNode.fields.forEach { field ->
                        val isHighlighted = field.id in highlightedFieldIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(CARD_FIELD_ROW_DP.dp)
                                .background(if (isHighlighted) Color(0xFFF2F2F2) else Color.Transparent)
                                .padding(start = 12.dp, end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = field.name,
                                fontSize = 11.sp,
                                color = if (isHighlighted) Color(0xFF111111) else Color(0xFF444444),
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = field.type.name.lowercase(),
                                fontSize = 10.sp,
                                color = Color(0xFFBBBBBB),
                            )
                        }
                    }
                }
            }
        }
    }
}
