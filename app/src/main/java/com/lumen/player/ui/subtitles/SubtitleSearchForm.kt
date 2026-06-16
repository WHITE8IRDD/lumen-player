package com.lumen.player.ui.subtitles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val subtitleLanguages = listOf(
    "en" to "English", "ar" to "Arabic", "fr" to "French", "es" to "Spanish",
    "pt" to "Portuguese", "de" to "German", "it" to "Italian", "tr" to "Turkish",
    "ja" to "Japanese", "ko" to "Korean", "zh" to "Chinese"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitleSearchForm(
    visible: Boolean,
    initialName: String,
    initialSeason: String,
    initialEpisode: String,
    initialLanguage: String,
    onDismiss: () -> Unit,
    onSearch: (name: String, season: String?, episode: String?, language: String, hearingImpaired: Boolean) -> Unit
) {
    if (!visible) return

    var nameField by remember { mutableStateOf(initialName) }
    var seasonField by remember { mutableStateOf(initialSeason) }
    var episodeField by remember { mutableStateOf(initialEpisode) }
    var selectedLanguage by remember { mutableStateOf(initialLanguage) }
    var selectedLanguageName by remember { mutableStateOf(subtitleLanguages.find { it.first == initialLanguage }?.second ?: "English") }
    var hearingImpaired by remember { mutableStateOf(false) }
    var langExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C1C),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp)
                .padding(bottom = 40.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Search subtitles", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Rounded.Close, "Close", tint = Color.White.copy(0.6f), modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = nameField, onValueChange = { nameField = it },
                label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = formColors()
            )
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = seasonField, onValueChange = { seasonField = it },
                    label = { Text("Season") }, modifier = Modifier.weight(1f), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = formColors()
                )
                OutlinedTextField(
                    value = episodeField, onValueChange = { episodeField = it },
                    label = { Text("Episode") }, modifier = Modifier.weight(1f), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = formColors()
                )
            }
            Spacer(Modifier.height(12.dp))

            ExposedDropdownMenuBox(expanded = langExpanded, onExpandedChange = { langExpanded = it }) {
                OutlinedTextField(
                    value = selectedLanguageName, onValueChange = {}, readOnly = true,
                    label = { Text("Language") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(), colors = formColors()
                )
                ExposedDropdownMenu(
                    expanded = langExpanded, onDismissRequest = { langExpanded = false },
                    modifier = Modifier.background(Color(0xFF1A1A1A))
                ) {
                    subtitleLanguages.forEach { (code, name) ->
                        DropdownMenuItem(
                            text = { Text(name, fontSize = 14.sp, color = Color.White) },
                            onClick = {
                                selectedLanguage = code; selectedLanguageName = name; langExpanded = false
                                onSearch(nameField, seasonField.ifBlank { null }, episodeField.ifBlank { null }, code, hearingImpaired)
                                onDismiss()
                            },
                            modifier = Modifier.height(44.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { onSearch(nameField, seasonField.ifBlank { null }, episodeField.ifBlank { null }, selectedLanguage, hearingImpaired); onDismiss() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFAB40)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.Search, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Search", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun formColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFFFFAB40),
    unfocusedBorderColor = Color.White.copy(0.2f),
    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
    focusedLabelColor = Color(0xFFFFAB40), unfocusedLabelColor = Color.White.copy(0.5f),
    cursorColor = Color(0xFFFFAB40)
)
