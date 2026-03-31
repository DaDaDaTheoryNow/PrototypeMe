package com.dadadadev.prototype_me.erd.board.ui.dimens

/**
 * Shared UI dimension constants for menus, toolbars, overlays,
 * and general layout values used across the ERD board feature.
 *
 * All values are in **dp** unless the constant name ends with `_PX` or `_SP`.
 */
internal object ErdBoardDimens {

    // ── Port dots (NodesLayer) ───────────────────────────────────────────────────
    /** Tap-target radius around each port dot, in dp. */
    const val PORT_HIT_TARGET_DP = 10f

    /** Visual size of the port tap target, in dp. */
    const val PORT_HIT_TARGET_SIZE_DP = 20f

    /** Visible dot radius inside the tap target, in dp. */
    const val PORT_DOT_SIZE_DP = 8f

    // ── Entity card (EntityCard) ─────────────────────────────────────────────────
    const val CARD_CORNER_RADIUS_DP = 6f
    const val CARD_ELEVATION_DEFAULT_DP = 2f
    const val CARD_ELEVATION_HIGHLIGHT_DP = 6f
    const val CARD_BORDER_WIDTH_DEFAULT_DP = 1f
    const val CARD_BORDER_WIDTH_HIGHLIGHT_DP = 2f
    const val CARD_HEADER_PADDING_HORIZONTAL_DP = 12f
    const val CARD_FIELD_PADDING_START_DP = 12f
    const val CARD_FIELD_PADDING_END_DP = 8f

    const val CARD_TITLE_FONT_SP = 13f
    const val CARD_LOCKED_FONT_SP = 9f
    const val CARD_FIELD_NAME_FONT_SP = 11f
    const val CARD_FIELD_TYPE_FONT_SP = 10f

    // ── Compact menus (BoardMenus) ───────────────────────────────────────────────
    const val MENU_CORNER_RADIUS_DP = 12f
    const val MENU_BORDER_WIDTH_DP = 1f
    const val MENU_SHADOW_ELEVATION_DP = 4f
    const val MENU_INNER_PADDING_H_DP = 4f
    const val MENU_INNER_PADDING_V_DP = 2f
    const val MENU_ACTION_MIN_HEIGHT_DP = 32f
    const val MENU_ACTION_PADDING_H_DP = 12f
    const val MENU_ACTION_PADDING_V_DP = 7f
    const val MENU_DIVIDER_HEIGHT_DP = 20f
    const val MENU_DIVIDER_PADDING_H_DP = 2f

    const val MENU_OFFSET_FROM_NODE_DP = 6f
    const val MENU_LABEL_FONT_SP = 12f
    const val MENU_COUNT_FONT_SP = 11f

    /** Min margin from screen edges for menus, in px. */
    const val MENU_SCREEN_MARGIN_PX = 4f
    /** Estimated menu width for bounding, in px. */
    const val MENU_ESTIMATED_WIDTH_PX = 140f
    /** Estimated menu height for bounding, in px. */
    const val MENU_ESTIMATED_HEIGHT_PX = 80f

    // ── Toolbars (BoardToolbars) ─────────────────────────────────────────────────
    const val TOOLBAR_PADDING_H_DP = 4f
    const val TOOLBAR_PADDING_V_DP = 2f
    const val TOOLBAR_FONT_LARGE_SP = 13f
    const val TOOLBAR_FONT_MEDIUM_SP = 12f
    const val TOOLBAR_FONT_SMALL_SP = 11f
    const val TOOLBAR_DIVIDER_HEIGHT_DP = 20f
    const val TOOLBAR_DIVIDER_PADDING_H_DP = 2f

    const val TOOLBAR_EDGE_LABEL_PADDING_H_DP = 8f
    const val TOOLBAR_HINT_PADDING_H_DP = 16f
    const val TOOLBAR_HINT_PADDING_V_DP = 8f

    /** Estimated half-width for edge toolbar positioning, in px. */
    const val EDGE_TOOLBAR_HALF_WIDTH_PX = 60f
    /** Estimated toolbar height for edge toolbar offset, in px. */
    const val EDGE_TOOLBAR_HEIGHT_PX = 48f

    // ── Overlays (ErdBoardCanvasOverlays) ────────────────────────────────────────
    const val OVERLAY_CONNECT_HINT_TOP_DP = 16f
    const val OVERLAY_ADD_TOOLBAR_BOTTOM_DP = 24f

    // ── Dialogs ──────────────────────────────────────────────────────────────────
    const val DIALOG_CORNER_RADIUS_DP = 12f
    const val DIALOG_BUTTON_CORNER_RADIUS_DP = 8f
    const val DIALOG_CHIP_CORNER_RADIUS_DP = 16f
    const val DIALOG_CODE_BORDER_WIDTH_DP = 1f
    const val DIALOG_CODE_CORNER_RADIUS_DP = 8f
    const val DIALOG_BUTTONS_SPACING_DP = 4f
    const val DIALOG_ROW_VERTICAL_PADDING_DP = 3f
    const val DIALOG_LABEL_BOTTOM_PADDING_DP = 4f
    const val DIALOG_SECTION_SPACING_MD_DP = 6f
    const val DIALOG_SECTION_SPACING_LG_DP = 8f
    const val DIALOG_SECTION_SPACING_XL_DP = 12f
    const val DIALOG_FIELD_TYPE_PADDING_H_DP = 8f
    const val DIALOG_CODE_PADDING_DP = 12f
    const val DIALOG_JSON_EXPORT_MIN_HEIGHT_DP = 200f
    const val DIALOG_JSON_EXPORT_MAX_HEIGHT_DP = 360f
    const val DIALOG_JSON_IMPORT_MIN_HEIGHT_DP = 160f
    const val DIALOG_JSON_IMPORT_MAX_HEIGHT_DP = 300f

    const val DIALOG_TITLE_FONT_SP = 16f
    const val DIALOG_FIELD_NAME_FONT_SP = 13f
    const val DIALOG_BUTTON_FONT_SP = 13f
    const val DIALOG_BODY_FONT_SP = 12f
    const val DIALOG_CAPTION_FONT_SP = 11f
    const val DIALOG_CODE_LINE_HEIGHT_SP = 16f

    // ── Platform-specific buttons (Save / Copy) ─────────────────────────────────
    const val PLATFORM_BUTTON_FONT_SP = 13f
}
