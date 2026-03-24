package com.dadadadev.prototype_me.erd.board.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.offset
import com.dadadadev.prototype_me.erd.board.ui.canvas.buildEdgeLabel
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.EntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.RelationEdge
import kotlin.math.roundToInt

/** Bottom bar with add, undo and JSON-view actions. */
@Composable
internal fun AddEntityToolbar(
    modifier: Modifier = Modifier,
    canUndo: Boolean = false,
    onUndo: (() -> Unit)? = null,
    onShowJson: () -> Unit,
    onAddEntity: () -> Unit,
) {
    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(28.dp))
            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(28.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onUndo != null) {
                TextButton(onClick = onUndo, enabled = canUndo) {
                    Text(
                        text = "Undo",
                        color = if (canUndo) Color(0xFF444444) else Color(0xFFBBBBBB),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                ToolbarDivider()
            }
            TextButton(onClick = onAddEntity) {
                Text(
                    "+ Add Entity",
                    color = Color(0xFF111111),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            ToolbarDivider()
            TextButton(onClick = onShowJson) {
                Text(
                    "{ }",
                    color = Color(0xFF555555),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun ToolbarDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(20.dp)
            .background(Color(0xFFE0E0E0)),
    )
}

/** Floating toolbar shown next to the selected edge. */
@Composable
internal fun EdgeSelectionToolbar(
    edge: RelationEdge,
    midpoint: Offset?,
    screenW: Float,
    screenH: Float,
    nodes: Map<String, EntityNode>,
    onDeleteEdge: () -> Unit,
) {
    val tbX = ((midpoint?.x ?: (screenW / 2f)) - 60f).coerceIn(4f, screenW - 140f)
    val tbY = ((midpoint?.y ?: (screenH - 80f)) - 48f).coerceIn(4f, screenH - 48f)

    Box(
        modifier = Modifier
            .offset { IntOffset(tbX.roundToInt(), tbY.roundToInt()) }
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = buildEdgeLabel(edge, nodes),
                fontSize = 11.sp,
                color = Color(0xFF666666),
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            TextButton(onClick = onDeleteEdge) {
                Text("Delete", color = Color(0xFFCC3333), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

/** Top-center hint banner shown while connect mode is active. */
@Composable
internal fun ConnectingHintBanner(
    connectingFromNodeId: String?,
    connectingFromFieldId: String?,
    nodes: Map<String, EntityNode>,
    modifier: Modifier = Modifier,
) {
    if (connectingFromNodeId == null) return

    val srcName = nodes[connectingFromNodeId]?.let { n ->
        val field = n.fields.firstOrNull { it.id == connectingFromFieldId }
        if (field != null) "${n.name}.${field.name}" else n.name
    }

    Box(
        modifier = modifier
            .background(Color(0xFF111111), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = if (srcName == null) "Tap a port to start" else "From: $srcName  ->  tap target",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

