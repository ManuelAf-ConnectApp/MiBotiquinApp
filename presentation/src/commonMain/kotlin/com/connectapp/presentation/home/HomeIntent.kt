package com.connectapp.presentation.home

interface HomeIntent {
    data class ItemClicked(val item: DashboardItem) : HomeIntent
}