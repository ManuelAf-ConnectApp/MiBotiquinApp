package com.connectapp.presentation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.ImageVector
import com.connectapp.commonresources.*
import com.connectapp.presentation.navigation.NavigationRoute
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Representa un elemento individual en la barra de navegación inferior.
 */
private data class BottomNavItem(
    val route: NavigationRoute,
    val icon: ImageVector,
    val label: StringResource
)

/**
 * Componente de barra de navegación inferior personalizado y robusto basado en Material 3.
 *
 * @param currentRoute La ruta actualmente activa para resaltar el icono correspondiente.
 * @param onNavigate Callback que se dispara cuando el usuario selecciona una nueva ruta.
 */
@Composable
fun CustomBottomBar(
    currentRoute: NavigationRoute,
    onNavigate: (NavigationRoute) -> Unit
) {
    // Definición de los destinos de la barra inferior. 
    // Esto se podría mover a un objeto de configuración si crece mucho.
    val navItems = listOf(
        BottomNavItem(
            route = NavigationRoute.HomeRoute(),
            icon = Icons.Default.Home,
            label = nav_home
        ),
        BottomNavItem(NavigationRoute.ScanRoute, Icons.Default.QrCodeScanner, nav_scan),
        BottomNavItem(
            route = NavigationRoute.ProfileRoute(),
            icon = Icons.Default.Person,
            label = nav_profile
        ),
        BottomNavItem(
            route = NavigationRoute.SettingsRoute,
            icon = Icons.Default.Settings,
            label = nav_settings
        )
    )

    Column {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = Dimen0 // Elevación sutil para separar del contenido
    ) {
        navItems.forEach { item ->
            // HomeRoute y ProfileRoute llevan el usuario logueado como parámetro, así que no
            // se pueden comparar por igualdad de valor: se comparan por tipo de ruta.
            val isSelected = when (item.route) {
                is NavigationRoute.HomeRoute -> currentRoute is NavigationRoute.HomeRoute
                is NavigationRoute.ProfileRoute -> currentRoute is NavigationRoute.ProfileRoute
                else -> currentRoute == item.route
            }
            
            NavigationBarItem(
                selected = isSelected,
                label = { 
                    Text(
                        text = stringResource(item.label),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ) 
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(item.label)
                    )
                },
                onClick = {
                    // Solo navegamos si no estamos ya en esa ruta para evitar redundancia
                    if (!isSelected) {
                        onNavigate(item.route)
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
    }
}
