package com.connectapp.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.connectapp.commonresources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun AuthLayout(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Box(modifier.fillMaxSize().imePadding(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = Dimen560).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = Dimen32, vertical = Dimen24),
            verticalArrangement = Arrangement.spacedBy(Dimen20), content = content
        )
    }
}

@Composable
fun SectionIntro(title: String, subtitle: String? = null, icon: ImageVector? = null, compact: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimen16)) {
        if (icon != null) BrandIcon(icon)
        Text(title, style = if (compact) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineLarge,
            modifier = Modifier.semantics { heading() })
        if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun BrandIcon(icon: ImageVector, modifier: Modifier = Modifier, large: Boolean = false) {
    Surface(modifier.size(if (large) Dimen80 else Dimen64), shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, null, Modifier.size(if (large) Dimen48 else Dimen32)) }
    }
}

@Composable
fun PrimaryActionButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier,
                        enabled: Boolean = true, busy: Boolean = false) {
    Button(onClick, modifier.fillMaxWidth().heightIn(min = Dimen56), enabled = enabled && !busy,
        shape = CircleShape, elevation = ButtonDefaults.buttonElevation(defaultElevation = Dimen4),
        contentPadding = PaddingValues(horizontal = Dimen24, vertical = Dimen16)) {
        if (busy) {
            CircularProgressIndicator(Modifier.size(Dimen20), strokeWidth = Dimen2)
            Spacer(Modifier.width(Dimen12))
        }
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun AppTextField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier,
                 keyboardType: KeyboardType = KeyboardType.Text, enabled: Boolean = true,
                 isError: Boolean = false, trailingIcon: ImageVector? = null) {
    val trailing: (@Composable () -> Unit)? = trailingIcon?.let { icon -> { Icon(icon, null) } }
    OutlinedTextField(value, onValueChange, modifier.fillMaxWidth(), label = { Text(label) },
        singleLine = true, enabled = enabled, isError = isError, shape = MaterialTheme.shapes.small,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType), trailingIcon = trailing)
}

@Composable
fun PasswordField(value: String, onValueChange: (String) -> Unit, label: String, visible: Boolean,
                  onToggle: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, isError: Boolean = false) {
    OutlinedTextField(value, onValueChange, modifier.fillMaxWidth(), label = { Text(label) },
        singleLine = true, enabled = enabled, isError = isError, shape = MaterialTheme.shapes.small,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onToggle, enabled = enabled) {
                Icon(if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    stringResource(content_desc_toggle_password))
            }
        })
}

@Composable
fun InformationPanel(text: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer) {
        Row(Modifier.padding(Dimen24), horizontalArrangement = Arrangement.spacedBy(Dimen16), verticalAlignment = Alignment.Top) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(Dimen24))
            Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        }
    }
}
