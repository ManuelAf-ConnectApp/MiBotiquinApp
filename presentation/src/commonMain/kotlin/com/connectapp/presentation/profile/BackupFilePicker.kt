package com.connectapp.presentation.profile

import androidx.compose.runtime.Composable

@Composable
expect fun BackupFilePicker(
    contentToSave: String?,
    openRequested: Boolean,
    onSaveHandled: () -> Unit,
    onFileOpened: (String?) -> Unit
)
