package com.nezammonitor.app.ui.navigation

import java.net.URLEncoder

sealed class Screen(val route: String, val title: String, val icon: String) {
    object Home : Screen("home", "خانه", "🏠")
    object Cases : Screen("cases", "پرونده\u200Cها", "📋")
    object CaseDetail : Screen("case_detail/{caseNumber}", "جزئیات پرونده", "📋") {
        fun createRoute(caseNumber: String): String {
            val encoded = URLEncoder.encode(caseNumber, "UTF-8")
            return "case_detail/$encoded"
        }
    }
    object Engineers : Screen("engineers", "مهندسین", "👷")
    object Fees : Screen("fees", "حق\u200Cالزحمه", "💰")
    object Reports : Screen("reports", "گزارش\u200Cها", "📄")
    object FollowUp : Screen("followup", "پیگیری\u200Cها", "📌")
    object Settings : Screen("settings", "تنظیمات", "⚙️")
    object Import : Screen("import", "ورود اطلاعات", "📥")
}

val drawerItems = listOf(
    Screen.Home,
    Screen.Cases,
    Screen.Engineers,
    Screen.Fees,
    Screen.Reports,
    Screen.FollowUp,
    Screen.Settings
)
