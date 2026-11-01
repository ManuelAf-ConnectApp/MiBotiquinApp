package com.connectapp.presentation.settings

import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    var showPrivacyDialog by remember { mutableStateOf(false) }

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
                SettingsEffect.ShowPrivacyInfo -> showPrivacyDialog = true
                is SettingsEffect.ShowMessage -> onShowMessage(effect.message)
            }
        }
    }

    SettingsScreenContent(
        modifier = modifier, state = state,
        onProfile = { viewModel.onIntent(SettingsIntent.EditProfileClicked) },
        onSecurity = { viewModel.onIntent(SettingsIntent.SecurityClicked) },
        onNotifications = { viewModel.onIntent(SettingsIntent.NotificationsToggled(it)) },
        onDarkMode = { viewModel.onIntent(SettingsIntent.DarkModeToggled(it)) },
        onAbout = { showAboutDialog = true },
        onPrivacy = { viewModel.onIntent(SettingsIntent.PrivacyPolicyClicked) },
        onNotificationDiagnostics = { viewModel.onIntent(SettingsIntent.NotificationDiagnosticsClicked) },
        onLogout = { viewModel.onIntent(SettingsIntent.LogoutClicked) }
    )
    if (showPrivacyDialog) AlertDialog(
        onDismissRequest = { showPrivacyDialog = false },
        title = { Text(stringResource(settings_item_privacy)) },
        text = { Text(stringResource(privacy_local_information), modifier = Modifier.verticalScroll(rememberScrollState())) },
        confirmButton = { TextButton(onClick = { showPrivacyDialog = false }) { Text(stringResource(close)) } }
    )

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
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
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
internal fun SettingsScreenContent(
    modifier: Modifier = Modifier, state: SettingsState,
    onProfile: () -> Unit, onSecurity: () -> Unit,
    onNotifications: (Boolean) -> Unit, onDarkMode: (Boolean) -> Unit,
    onAbout: () -> Unit, onPrivacy: () -> Unit, onNotificationDiagnostics: () -> Unit = {}, onLogout: () -> Unit
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            Modifier.widthIn(max = Dimen560).fillMaxWidth(), contentPadding = PaddingValues(Dimen24)
        ) {
            item { SectionHeader(stringResource(settings_section_account)) }
            item { SettingsRow(Icons.Default.PersonOutline, stringResource(settings_item_personal_info), stringResource(settings_profile_subtitle), onProfile) }
            item { SettingsRow(Icons.Default.Shield, stringResource(settings_item_security), stringResource(settings_security_subtitle), onSecurity) }
            item { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) }
            item { SectionHeader(stringResource(settings_section_preferences)) }
            item { SettingsSwitchRow(Icons.Default.NotificationsNone, stringResource(settings_item_notifications),
                stringResource(settings_notifications_subtitle), state.notificationsEnabled, onNotifications) }
            item { SettingsRow(Icons.Default.BugReport, "Diagnóstico de avisos", state.notificationDiagnostic ?: "Comprueba permiso, alarmas y avisos programados", onNotificationDiagnostics) }
            item { SettingsSwitchRow(Icons.Default.DarkMode, stringResource(settings_item_dark_mode),
                stringResource(settings_theme_subtitle), state.darkModeEnabled, onDarkMode) }
            item { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) }
            item { SectionHeader(stringResource(settings_section_more)) }
            item { SettingsRow(Icons.Default.Info, stringResource(settings_item_about), stringResource(settings_item_version, state.appVersion), onAbout) }
            item { SettingsRow(Icons.Default.PrivacyTip, stringResource(settings_item_privacy), stringResource(settings_privacy_subtitle), onPrivacy) }
            item { Spacer(Modifier.height(Dimen24)) }
            state.errorMessage?.let { error -> item { Text(error.asString(), color = MaterialTheme.colorScheme.error) } }
            item {
                OutlinedButton(onLogout, modifier = Modifier.fillMaxWidth().heightIn(min = Dimen56), enabled = !state.isLoading,
                    shape = CircleShape, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    if (state.isLoading) CircularProgressIndicator(Modifier.size(Dimen20), strokeWidth = Dimen2)
                    else Icon(Icons.AutoMirrored.Filled.Logout, null)
                    Spacer(Modifier.width(Dimen12))
                    Text(stringResource(logout))
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title.uppercase(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = Dimen24, bottom = Dimen12).semantics { heading() })
}

@Composable
private fun SettingsIcon(icon: ImageVector) {
    Surface(Modifier.size(Dimen48), color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.large) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, null, Modifier.size(Dimen24)) }
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(onClick, Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.padding(vertical = Dimen16), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimen16)) {
            SettingsIcon(icon)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimen4)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsSwitchRow(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().toggleable(checked, role = Role.Switch, onValueChange = onChange)
        .padding(vertical = Dimen16), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimen16)) {
        SettingsIcon(icon)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimen4)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked, onCheckedChange = null)
    }
}
