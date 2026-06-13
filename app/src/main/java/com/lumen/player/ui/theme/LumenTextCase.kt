package com.lumen.player.ui.theme

enum class LumenTextCase(val displayName: String) {
    LOWERCASE("lowercase"),
    SENTENCE("Sentence case"),
    TITLE("Title Case"),
    UPPERCASE("UPPERCASE")
}

fun String.applyLumenCase(case: LumenTextCase): String = when (case) {
    LumenTextCase.LOWERCASE -> lowercase()
    LumenTextCase.SENTENCE -> replaceFirstChar { it.uppercase() }
    LumenTextCase.TITLE -> split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { it.uppercase() }
    }
    LumenTextCase.UPPERCASE -> uppercase()
}
