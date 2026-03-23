package com.dadadadev.prototype_me.board.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
import com.dadadadev.prototype_me.board.ui.buildEdgeLabel
import com.dadadadev.prototype_me.domain.models.EntityNode
import com.dadadadev.prototype_me.domain.models.RelationEdge
import kotlin.math.roundToInt

/** Bottom bar with the "+ Add Entity" button. */
@Composable
internal fun AddEntityToolbar(
    modifier: Modifier = Modifier,
    onAddEntity: () -> Unit,
) {
    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(28.dp))
            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(28.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        TextButton(onClick = onAddEntity) {
            Text(
                "+ Add Entity",
                color = Color(0xFF111111),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/** Floating toolbar that appears near a selected edge. */
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

/** Top-center hint banner shown while tap-to-connect mode is active. */
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
