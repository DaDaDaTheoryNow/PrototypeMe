package com.dadadadev.prototype_me.erd.board.ui.theme

/**
 * Centralized UI string constants for the ERD Board feature.
 *
 * All user-visible labels, button texts, and messages are defined here
 * to simplify future localization (i18n) and maintain consistency.
 */
internal object ErdBoardStrings {

    // ── Node menu ────────────────────────────────────────────────────────────────
    const val MENU_EDIT_FIELDS = "Edit Fields"
    const val MENU_DELETE = "Delete"
    const val MENU_COPY = "Copy"

    // ── Toolbar buttons ──────────────────────────────────────────────────────────
    const val TOOLBAR_UNDO = "Undo"
    const val TOOLBAR_ADD_ENTITY = "+ Add Entity"
    const val TOOLBAR_JSON = "{ }"
    const val TOOLBAR_SHARE = "Share"

    // ── Multi-select ─────────────────────────────────────────────────────────────
    fun multiSelectCount(count: Int): String = "$count selected"

    // ── Edge toolbar ─────────────────────────────────────────────────────────────
    const val EDGE_DELETE = "Delete"

    // ── Connecting hint banner ────────────────────────────────────────────────────
    const val CONNECT_TAP_TO_START = "Tap a port to start"
    fun connectFromLabel(sourceName: String): String = "From: $sourceName  →  tap target"

    // ── Add Entity dialog ────────────────────────────────────────────────────────
    const val DIALOG_NEW_ENTITY_TITLE = "New Entity"
    const val DIALOG_ENTITY_NAME_PLACEHOLDER = "Entity name"
    const val DIALOG_ENTITY_DEFAULT_NAME = "Entity"
    const val DIALOG_ADD = "Add"
    const val DIALOG_CANCEL = "Cancel"

    // ── Node Detail dialog ───────────────────────────────────────────────────────
    const val DETAIL_FIELDS_HEADER = "Fields"
    const val DETAIL_ADD_FIELD_HEADER = "Add field"
    const val DETAIL_FIELD_NAME_PLACEHOLDER = "Field name"
    const val DETAIL_ADD_FIELD_BUTTON = "Add field"
    const val DETAIL_DONE = "Done"
    const val DETAIL_REMOVE_FIELD = "x"

    // ── Board JSON dialog ────────────────────────────────────────────────────────
    const val JSON_DIALOG_TITLE = "Board JSON"
    const val JSON_TAB_VIEW_EXPORT = "View / Export"
    const val JSON_TAB_IMPORT = "Import"
    const val JSON_IMPORT_BUTTON = "Import"
    const val JSON_IMPORT_ERROR_EMPTY = "Paste JSON to import"
    const val JSON_CLOSE = "Close"
    const val JSON_IMPORT_HINT = "Paste board JSON below."
    const val JSON_IMPORT_PLACEHOLDER = """{ "version": 1, "nodes": [...], ... }"""
    const val JSON_IMPORT_WARNING = "Warning: importing replaces the current board."

    // ── JSON buttons (platform-specific) ────────────────────────────────────────
    const val JSON_COPY_BUTTON = "Copy"
    const val JSON_SAVE_BUTTON = "Save to File"
    const val JSON_SAVE_DIALOG_TITLE = "Save Board as JSON"

    // ── Share dialog ────────────────────────────────────────────────────────────
    const val SHARE_DIALOG_TITLE = "Share Board"
    const val SHARE_BOARD_ID_LABEL = "Board ID"
    const val SHARE_CLOSE = "Close"
    const val SHARE_COPY_BUTTON = "Copy"

    // ── Entity card ─────────────────────────────────────────────────────────────
    /** Short lock indicator on the card header. */
    fun cardLockedLabel(user: String): String = "* $user"

    // ── Snackbar / status messages ────────────────────────────────────────────────
    fun lockedByMessage(user: String): String = "Locked by $user"
    const val CONNECTION_LOST = "Connection lost - reconnecting..."
}
