package com.dadadadev.prototype_me

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dadadadev.prototype_me.core.ui.theme.AppTheme
import com.dadadadev.prototype_me.erd.board.ui.screen.ErdBoardScreen
import com.dadadadev.prototype_me.feature.home.ui.screen.HomeScreen

private sealed interface AppScreen {
    data object Home : AppScreen
    data class Board(val boardId: String, val userId: String) : AppScreen
}

@Composable
fun App() {
    var screen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }

    AppTheme {
        when (val s = screen) {
            is AppScreen.Home -> HomeScreen(
                onNavigateToBoard = { boardId, userId ->
                    screen = AppScreen.Board(boardId = boardId, userId = userId)
                },
            )

            is AppScreen.Board -> ErdBoardScreen(
                boardId = s.boardId,
                userId = s.userId,
                onNavigateBack = {
                    screen = AppScreen.Home
                },
            )
        }
    }
}
