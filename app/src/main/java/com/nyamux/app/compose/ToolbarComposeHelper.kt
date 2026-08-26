package com.nyamux.app

import androidx.compose.ui.platform.ComposeView
import com.nyamux.app.compose.TermuxToolbar

fun setToolbarContent(
    composeView: ComposeView,
    activity: TermuxActivity,
    savedTextInput: String?
) {
    composeView.setContent {
        com.nyamux.app.compose.TermuxTheme {
            TermuxToolbar(activity, savedTextInput)
        }
    }
}
