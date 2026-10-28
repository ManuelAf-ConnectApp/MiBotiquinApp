package com.connectapp.presentation.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun BackupFilePicker(contentToSave: String?, openRequested: Boolean, onSaveHandled: () -> Unit, onFileOpened: (String?) -> Unit) {
    val context = LocalContext.current
    val create = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null && contentToSave != null) context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(contentToSave) }
        onSaveHandled()
    }
    val open = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        onFileOpened(uri?.let { context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader -> reader.readText() } })
    }
    LaunchedEffect(contentToSave) { if (contentToSave != null) create.launch("MiBotiquinApp-backup.json") }
    LaunchedEffect(openRequested) { if (openRequested) open.launch(arrayOf("application/json", "application/octet-stream")) }
}
