package com.dadadadev.prototype_me

import androidx.compose.runtime.Composable
import com.dadadadev.prototype_me.erd.board.ui.BoardScreen
import com.dadadadev.prototype_me.core.ui.theme.AppTheme

@Composable
fun App() {
    AppTheme {
        BoardScreen()
    }
}

