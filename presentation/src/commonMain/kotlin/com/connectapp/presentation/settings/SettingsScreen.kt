package com.connectapp.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.commonresources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onShowMessage: (String) -> Unit = {}
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showAboutDialog by remember { mutableStateOf(false) }

    // El ViewModel vive tanto como la Activity: se recarga al entrar para no mostrar el
    // nombre y email de una sesión anterior.
    LaunchedEffect(Unit) {
        viewModel.onIntent(SettingsIntent.LoadSettings)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SettingsEffect.NavigateToLogin -> onLogout()
                SettingsEffect.NavigateToProfile -> onNavigateToProfile()
                is SettingsEffect.ShowMessage -> onShowMessage(effect.message)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Dimen16),
            verticalArrangement = Arrangement.spacedBy(Dimen8)
        ) {
            item {
                ProfileHeader(
                    name = state.userName,
                    email = state.userEmail,
                    onEditClick = { viewModel.onIntent(SettingsIntent.EditProfileClicked) }
                )
            }

            item { SectionHeader(title = stringResource(settings_section_account)) }
            item {
                SettingsRow(
                    icon = Icons.Default.Person,
                    title = stringResource(settings_item_personal_info),
                    onClick = { viewModel.onIntent(SettingsIntent.EditProfileClicked) }
                )
            }
            item {
                SettingsRow(
                    icon = Icons.Default.Lock,
                    title = stringResource(settings_item_security),
                    onClick = { viewModel.onIntent(SettingsIntent.SecurityClicked) }
                )
            }

            item { SectionHeader(title = stringResource(settings_section_preferences)) }
            item {
                SettingsSwitchRow(
                    icon = Icons.Default.Notifications,
                    title = stringResource(settings_item_notifications),
                    checked = state.notificationsEnabled,
                    onCheckedChange = { viewModel.onIntent(SettingsIntent.NotificationsToggled(it)) }
                )
            }
            item {
                SettingsSwitchRow(
                    icon = Icons.Default.DarkMode,
                    title = stringResource(settings_item_dark_mode),
                    checked = state.darkModeEnabled,
                    onCheckedChange = { viewModel.onIntent(SettingsIntent.DarkModeToggled(it)) }
                )
            }

            item { SectionHeader(title = stringResource(settings_section_more)) }
            item {
                SettingsRow(
                    icon = Icons.Default.Info,
                    title = stringResource(settings_item_about),
                    subtitle = stringResource(settings_item_version, state.appVersion),
                    onClick = { showAboutDialog = true }
                )
            }
            item {
                SettingsRow(
                    icon = Icons.Default.Description,
                    title = stringResource(settings_item_privacy),
                    onClick = { viewModel.onIntent(SettingsIntent.PrivacyPolicyClicked) }
                )
            }

            item { Spacer(modifier = Modifier.height(Dimen24)) }

            state.errorMessage?.let { errorMessage ->
                item {
                    Text(
                        text = errorMessage.asString(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = Dimen8)
                    )
                }
            }

            item {
                if (state.isLoading) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    Button(
                        onClick = { viewModel.onIntent(SettingsIntent.LogoutClicked) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.width(Dimen8))
                        Text(stringResource(logout))
                    }
                }
            }
        }
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    val email = stringResource(about_dialog_email)
    val linkColor = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(Dimen48)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        title = {
            Text(
                text = stringResource(settings_item_about),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimen16)
            ) {
                Text(
                    text = stringResource(about_dialog_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                HorizontalDivider()

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimen4)
                ) {
                    Text(
                        text = stringResource(about_dialog_contact_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimen8)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = linkColor,
                            modifier = Modifier.size(Dimen20)
                        )
                        // Real inline hyperlink: tapping it opens the device's default mail
                        // client (or the picker, if several are installed) via a mailto: URI.
                        Text(
                            text = buildAnnotatedString {
                                withLink(
                                    LinkAnnotation.Url(
                                        url = "mailto:$email",
                                        styles = TextLinkStyles(
                                            style = SpanStyle(
                                                color = linkColor,
                                                fontWeight = FontWeight.SemiBold,
                                                textDecoration = TextDecoration.Underline
                                            )
                                        )
                                    )
                                ) {
                                    append(email)
                                }
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(close))
            }
        }
    )
}

@Composable
private fun ProfileHeader(
    name: String,
    email: String,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimen16),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(Dimen16)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Dimen64)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                if (name.isNotBlank()) {
                    Text(
                        text = name.trim().take(1).uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(Dimen16))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(edit_profile))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = Dimen16, bottom = Dimen8)
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null
) {
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .padding(vertical = Dimen12, horizontal = Dimen4)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Dimen24)
            )
            Spacer(modifier = Modifier.width(Dimen16))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (onClick != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface
        ) {
            content()
        }
    } else {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(vertical = Dimen8, horizontal = Dimen4)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Dimen24)
        )
        Spacer(modifier = Modifier.width(Dimen16))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
