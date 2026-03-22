package com.dadadadev.prototype_me.board.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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

// ── Layout constants (must match actual dp values used in the card layout) ──
const val CARD_WIDTH_DP = 160f
const val CARD_HEADER_DP = 40f     // padding(8) + Text(13sp ~20dp) + padding(8) ≈ 36-40
const val CARD_FIELD_ROW_DP = 24f  // padding(4) + Text(11sp ~16dp) + padding(4) ≈ 24
const val CARD_DIVIDER_DP = 1f

/**
 * A single entity card on the infinite canvas.
 *
 * Port dots (small circles on the right edge of each row) are always visible
 * and tappable — tapping one starts or completes a connection without any
 * separate "connect mode" toggle.
 *
 * Interactions:
 * - Drag card body    → move node
 * - Long press body   → open field editor
 * - Tap port dot      → start / finish a connection
 */
@Composable
fun EntityCard(
    nodeId: String,
    stateFlow: StateFlow<BoardState>,
    isSourceNode: Boolean,                          // card is the source of a pending connection
    isConnecting: Boolean,                          // any connection is currently in progress
    onDragStart: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onLongPress: () -> Unit,
    onHeaderPortTap: () -> Unit,
    onFieldPortTap: (fieldId: String) -> Unit,
    highlightedFieldIds: Set<String> = emptySet(),
) {
    val node by produceState<EntityNode?>(initialValue = null, stateFlow) {
        stateFlow.map { it.nodes[nodeId] }.collect { value = it }
    }
    val scale by produceState(initialValue = 1f, stateFlow) {
        stateFlow.map { it.scale }.collect { value = it }
    }
    val panOffset by produceState(initialValue = Offset.Zero, stateFlow) {
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
    val bgColor = when {
        isSourceNode -> Color(0xFFF5F5F5)
        isLocked     -> Color(0xFFF0F0F0)
        else         -> Color.White
    }

    Box(
        modifier = Modifier.graphicsLayer {
            translationX = currentNode.position.x * scale + panOffset.x
            translationY = currentNode.position.y * scale + panOffset.y
            scaleX = scale
            scaleY = scale
            // TOP-LEFT pivot so card edge = translationX exactly
            transformOrigin = TransformOrigin(0f, 0f)
        }
    ) {
        Card(
            modifier = Modifier
                .width(CARD_WIDTH_DP.dp)
                .pointerInput(nodeId) {
                    detectDragGestures(
                        onDragStart = { onDragStart() },
                        onDragEnd = { onDragEnd() },
                        onDrag = { change, delta -> change.consume(); onDrag(delta) }
                    )
                }
                .pointerInput(nodeId) {
                    detectTapGestures(onLongPress = { onLongPress() })
                },
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(borderWidth, borderColor),
            colors = CardDefaults.cardColors(containerColor = bgColor),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isSourceNode) 6.dp else 2.dp)
        ) {
            Column {
                // ── Header row ────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentNode.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isLocked) Color(0xFF888888) else Color(0xFF111111),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isLocked) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "● ${currentNode.lockedBy}",
                                fontSize = 10.sp,
                                color = Color(0xFFBBBBBB),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    // Header port dot — always visible, tappable
                    Spacer(Modifier.width(4.dp))
                    PortDot(
                        isSource = isSourceNode,
                        isConnecting = isConnecting,
                        size = 8,
                        onTap = onHeaderPortTap
                    )
                }

                // ── Fields ────────────────────────────────────────────────────
                if (currentNode.fields.isNotEmpty()) {
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                    Column(modifier = Modifier.padding(bottom = 6.dp)) {
                        currentNode.fields.forEach { field ->
                            val isHighlighted = field.id in highlightedFieldIds
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isHighlighted) Color(0xFFF5F5F5) else Color.Transparent)
                                    .padding(start = 12.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
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
                                    maxLines = 1
                                )
                                // Field port dot — always visible, tappable
                                Spacer(Modifier.width(6.dp))
                                PortDot(
                                    isSource = isSourceNode,
                                    isConnecting = isConnecting,
                                    size = 7,
                                    onTap = { onFieldPortTap(field.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Small tappable circle that acts as a connection port.
 *
 * Colours:
 *  - source node     → dark (active)
 *  - connecting, not source → medium grey (valid target hint)
 *  - idle            → light grey (subtle)
 */
@Composable
private fun PortDot(
    isSource: Boolean,
    isConnecting: Boolean,
    size: Int,
    onTap: () -> Unit,
) {
    val color = when {
        isSource    -> Color(0xFF111111)
        isConnecting -> Color(0xFF888888)
        else        -> Color(0xFFCCCCCC)
    }
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(color, CircleShape)
            .pointerInput(isSource, isConnecting) {
                detectTapGestures(onTap = { onTap() })
            }
    )
}
