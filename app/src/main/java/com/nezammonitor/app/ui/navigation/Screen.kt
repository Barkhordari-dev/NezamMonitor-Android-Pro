package com.nezammonitor.app.ui.navigation

sealed class Screen(val route: String, val title: String, val icon: String) {
    object Home : Screen("home", "خانه", "🏠")
    object Cases : Screen("cases", "پرونده‌ها", "📋")
    object CaseDetail : Screen("case_detail/{caseNumber}", "جزئیات پرونده", "📋") {
        fun createRoute(caseNumber: String) = "case_detail/$caseNumber"
    }
    object Engineers : Screen("engineers", "مهندسین", "👷")
    object Fees : Screen("fees", "حق‌الزحمه", "💰")
    object Reports : Screen("reports", "گزارش‌ها", "📄")
    object FollowUp : Screen("followup", "پیگیری", "📌")
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
