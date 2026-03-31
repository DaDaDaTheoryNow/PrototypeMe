package com.dadadadev.prototype_me.erd.board.presentation

/**
 * Runtime board connection context for the ERD editor session.
 *
 * Kept outside of runtime UI state to avoid hardcoded environment values
 * inside presentation mechanics.
 */
data class ErdBoardSession(
    val boardId: String,
    val currentUserId: String,
)
