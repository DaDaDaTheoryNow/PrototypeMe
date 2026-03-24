package com.dadadadev.prototype_me.erd.board.ui.json

import androidx.compose.runtime.Composable

/**
 * Platform-specific button that saves [content] as a file named [filename].
 *
 * - Android: system "Create Document" file picker via ActivityResult API
 * - JVM: JFileChooser save dialog
 * - JS / WasmJS: browser anchor-element download
 */
@Composable
internal expect fun SaveJsonButton(filename: String, content: String)
