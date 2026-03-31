package com.dadadadev.prototype_me.erd.board.layout

import com.dadadadev.prototype_me.domains.board.core.api.domain.model.BoardSize
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdEntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdNodeField

/**
 * Single source of truth for ERD entity-card dimensions (in dp).
 *
 * Shared by Presentation and UI layers to keep node sizing and rendering in sync.
 */
internal object ErdNodeDimens {
    /** Fixed card width in dp. */
    const val CARD_WIDTH_DP = 160f

    /** Header row height in dp. */
    const val CARD_HEADER_DP = 44f

    /** Single field-row height in dp. */
    const val CARD_FIELD_ROW_DP = 28f

    /** Divider thickness between header and field list in dp. */
    const val CARD_DIVIDER_DP = 1f
}

/**
 * Computes the visual [BoardSize] for an ERD entity node based on its field count.
 * Must be called from the Presentation layer when creating or mutating nodes.
 */
internal fun measureEntityNodeSize(fields: List<ErdNodeField>): BoardSize {
    val bodyHeight = if (fields.isNotEmpty()) {
        ErdNodeDimens.CARD_DIVIDER_DP + ErdNodeDimens.CARD_FIELD_ROW_DP * fields.size
    } else {
        0f
    }
    return BoardSize(
        width = ErdNodeDimens.CARD_WIDTH_DP,
        height = ErdNodeDimens.CARD_HEADER_DP + bodyHeight,
    )
}

/**
 * Returns a copy of this [ErdEntityNode] with updated [fields] and recalculated size.
 */
internal fun ErdEntityNode.withFields(fields: List<ErdNodeField>): ErdEntityNode =
    copy(fields = fields, size = measureEntityNodeSize(fields))

/**
 * Returns a copy of this [ErdEntityNode] with its [ErdEntityNode.size] recalculated
 * from the current [ErdEntityNode.fields]. Use when receiving nodes from the Data layer
 * (where size is not persisted).
 */
internal fun ErdEntityNode.remeasured(): ErdEntityNode =
    copy(size = measureEntityNodeSize(fields))
