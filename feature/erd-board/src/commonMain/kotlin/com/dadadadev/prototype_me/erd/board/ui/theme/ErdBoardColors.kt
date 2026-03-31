package com.dadadadev.prototype_me.erd.board.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Semantic color palette.
 *
 * All UI colors for the board, cards, edges, and dialogs are defined here.
 * No `Color(0x...)` literals should appear outside this file.
 *
 * Naming convention: `<component>_<role>` for clarity at call-site.
 */
object ErdBoardColors {

    // ── Text hierarchy (dark → light) ──────────────────────────────────────────
    val textPrimary     = Color(0xFF111111)
    val textSecondary   = Color(0xFF333333)
    val textTertiary    = Color(0xFF444444)
    val textMuted       = Color(0xFF555555)
    val textSubtle      = Color(0xFF666666)
    val textDisabled    = Color(0xFF888888)
    val textPlaceholder = Color(0xFFAAAAAA)
    val textHint        = Color(0xFFBBBBBB)
    val textGhost       = Color(0xFFCCCCCC)

    // ── Borders ────────────────────────────────────────────────────────────────
    val borderStrong    = Color(0xFF111111)
    val borderDefault   = Color(0xFFDDDDDD)
    val borderLight     = Color(0xFFEEEEEE)
    val borderLocked    = Color(0xFFBBBBBB)

    // ── Surfaces / Backgrounds ─────────────────────────────────────────────────
    val surfaceCard           = Color.White
    val surfaceCardHighlight  = Color(0xFFF8F8F8)
    val surfaceFieldHighlight = Color(0xFFF2F2F2)
    val surfaceChipInactive   = Color(0xFFF0F0F0)
    val surfaceCodeBlock      = Color(0xFFFAFAFA)
    val surfaceDialog         = Color.White
    val surfaceMenu           = Color.White
    val surfaceToolbar        = Color.White
    val separator             = Color(0xFFE0E0E0)
    val divider               = Color(0xFFEEEEEE)
    val gridDot               = Color(0xFFDDDDDD)

    // ── Edge / Connection lines ────────────────────────────────────────────────
    val edgeDefault     = Color(0xFF888888)
    val edgeSelected    = Color(0xFF111111)
    val edgeDragHandle  = Color(0xFF555555)

    // ── Port dots (field-level connection points) ──────────────────────────────
    val portActive      = Color(0xFF111111)
    val portAvailable   = Color(0xFF888888)
    val portInactive    = Color(0xFFCCCCCC)

    // ── Toolbar ────────────────────────────────────────────────────────────────
    val toolbarBackground = Color(0xFF111111)
    val toolbarEnabled    = Color(0xFF444444)
    val toolbarDisabled   = Color(0xFFBBBBBB)

    // ── On-accent (text on dark / accent backgrounds) ──────────────────────────
    val textOnAccent = Color.White

    // ── Accent colors ──────────────────────────────────────────────────────────
    val accentBlue = Color(0xFF2255CC)
    val accentRed  = Color(0xFFCC3333)
}
