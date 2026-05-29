package com.connectapp.presentation.home


import com.connectapp.commonresources.*
import org.jetbrains.compose.resources.StringResource


data class DashboardItem(val title: StringResource, val subtitle: StringResource)

val dashboardItems = listOf(
    DashboardItem(home_projects_title, home_projects_subtitle),
    DashboardItem(home_tasks_title, home_tasks_subtitle),
    DashboardItem(home_messages_title, home_messages_subtitle),
    DashboardItem(settings, home_settings_subtitle)
)


data class HomeState(
    val items: List<DashboardItem> = dashboardItems,
    val isLoading: Boolean = false,
    val error: String? = null
)