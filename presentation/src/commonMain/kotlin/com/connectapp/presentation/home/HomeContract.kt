package com.connectapp.presentation.home


import com.connectapp.commonresources.*
import com.connectapp.domain.model.Medication
import com.connectapp.domain.model.User
import org.jetbrains.compose.resources.StringResource


data class DashboardItem(val title: StringResource, val subtitle: StringResource)

val dashboardItems = listOf(
    DashboardItem(home_projects_title, home_projects_subtitle),
    DashboardItem(home_tasks_title, home_tasks_subtitle),
    DashboardItem(home_messages_title, home_messages_subtitle),
    DashboardItem(settings, home_settings_subtitle)
)


data class HomeState(
    val medications: List<Medication>,
    val items: List<DashboardItem>,
    val user: User?,
    val isLoading: Boolean,
    val error: String?
) {
    companion object {
        val EMPTY_DATA = HomeState(
            medications = emptyList(),
            items = dashboardItems,
            user = null,
            isLoading = false,
            error = null
        )
    }
}