package com.medlog.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.medlog.app.ui.features.dashboard.DashboardScreen
import com.medlog.app.ui.features.medication.MedicationListScreen
import com.medlog.app.ui.features.medication.MedicationDetailScreen
import com.medlog.app.ui.features.medication.AddMedicationScreen
import com.medlog.app.ui.features.condition.ConditionListScreen
import com.medlog.app.ui.features.condition.ConditionDetailScreen
import com.medlog.app.ui.features.condition.AddConditionScreen
import com.medlog.app.ui.features.appointment.AppointmentListScreen
import com.medlog.app.ui.features.appointment.AddAppointmentScreen
import com.medlog.app.ui.features.appointment.AppointmentDetailScreen
import com.medlog.app.ui.features.journal.JournalListScreen
import com.medlog.app.ui.features.journal.AddJournalEntryScreen
import com.medlog.app.ui.features.journal.JournalDetailScreen
import com.medlog.app.ui.features.clutter.ClutterScreen
import com.medlog.app.ui.features.section.SectionListScreen
import com.medlog.app.ui.features.section.AddSectionScreen
import com.medlog.app.ui.features.section.SectionDetailScreen
import com.medlog.app.ui.features.search.SearchScreen
import com.medlog.app.ui.features.settings.SettingsScreen
import com.medlog.app.ui.features.onboarding.OnboardingScreen
import com.medlog.app.ui.features.profile.ProfileSelectorScreen
import androidx.compose.ui.platform.LocalContext
import com.medlog.app.MedLogApp

sealed class Route(val route: String) {
    data object Onboarding : Route("onboarding")
    data object ProfileSelect : Route("profile_select")
    data object Dashboard : Route("dashboard")
    data object Medications : Route("medications")
    data object AddMedication : Route("medications/add")
    data object MedicationDetail : Route("medications/{id}") {
        fun createRoute(id: Long) = "medications/$id"
    }
    data object Conditions : Route("conditions")
    data object AddCondition : Route("conditions/add")
    data object ConditionDetail : Route("conditions/{id}") {
        fun createRoute(id: Long) = "conditions/$id"
    }
    data object Appointments : Route("appointments")
    data object AddAppointment : Route("appointments/add")
    data object AppointmentDetail : Route("appointments/{id}") {
        fun createRoute(id: Long) = "appointments/$id"
    }
    data object Journal : Route("journal")
    data object AddJournal : Route("journal/add")
    data object JournalDetail : Route("journal/{id}") {
        fun createRoute(id: Long) = "journal/$id"
    }
    data object Clutter : Route("clutter")
    data object Sections : Route("sections")
    data object AddSection : Route("sections/add")
    data object SectionDetail : Route("sections/{id}") {
        fun createRoute(id: Long) = "sections/$id"
    }
    data object Search : Route("search")
    data object Settings : Route("settings")
}

@Composable
fun MedLogNavHost(
    navController: NavHostController,
    startRoute: String = Route.Onboarding.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startRoute,
        modifier = modifier
    ) {
        composable(Route.Onboarding.route) {
            val app = LocalContext.current.applicationContext as MedLogApp
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Route.Dashboard.route) {
                        popUpTo(Route.Onboarding.route) { inclusive = true }
                    }
                },
                profileRepository = app.container.profileRepository,
                settingsRepository = app.container.settingsRepository
            )
        }
        composable(Route.ProfileSelect.route) {
            ProfileSelectorScreen(
                onProfileSelected = {
                    navController.navigate(Route.Dashboard.route) {
                        popUpTo(Route.ProfileSelect.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Route.Dashboard.route) { DashboardScreen(navController) }
        composable(Route.Medications.route) { MedicationListScreen(navController) }
        composable(Route.AddMedication.route) { AddMedicationScreen(navController) }
        composable(
            Route.MedicationDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { MedicationDetailScreen(navController) }
        composable(Route.Conditions.route) { ConditionListScreen(navController) }
        composable(Route.AddCondition.route) { AddConditionScreen(navController) }
        composable(
            Route.ConditionDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { ConditionDetailScreen(navController) }
        composable(Route.Appointments.route) { AppointmentListScreen(navController) }
        composable(Route.AddAppointment.route) { AddAppointmentScreen(navController) }
        composable(
            Route.AppointmentDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { AppointmentDetailScreen(navController) }
        composable(Route.Journal.route) { JournalListScreen(navController) }
        composable(Route.AddJournal.route) { AddJournalEntryScreen(navController) }
        composable(
            Route.JournalDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { JournalDetailScreen(navController) }
        composable(Route.Clutter.route) { ClutterScreen(navController) }
        composable(Route.Sections.route) { SectionListScreen(navController) }
        composable(Route.AddSection.route) { AddSectionScreen(navController) }
        composable(
            Route.SectionDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { SectionDetailScreen(navController) }
        composable(Route.Search.route) { SearchScreen(navController) }
        composable(Route.Settings.route) { SettingsScreen(navController) }
    }
}
