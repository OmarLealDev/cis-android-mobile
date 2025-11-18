@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)

package com.cis_ac.cis_ac.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cis_ac.cis_ac.ui.feature.auth.login.LoginScreen
import androidx.compose.material3.Text
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.cis_ac.cis_ac.ui.feature.auth.signup.patient.PatientSignUpScreen
import com.cis_ac.cis_ac.ui.feature.auth.signup.professional.ProfessionalSignUpScreen
import com.cis_ac.cis_ac.ui.feature.home.admin.AdminHomeNav
import com.cis_ac.cis_ac.ui.feature.home.admin.professionals.ProfessionalsRoute
import com.cis_ac.cis_ac.ui.feature.home.admin.requests.RequestsRoute
import com.cis_ac.cis_ac.ui.feature.home.admin.requests.detail.RequestDetailRoute
import com.cis_ac.cis_ac.ui.feature.home.patient.PatientHomeNav
import com.cis_ac.cis_ac.ui.feature.home.professional.ProfessionalHomeNav

import com.cis_ac.cis_ac.ui.feature.patient.professionals.PatientProfessionalsRoute
import com.cis_ac.cis_ac.ui.feature.patient.professionals.PatientProfessionalItem
import com.cis_ac.cis_ac.ui.feature.patient.professionalprofile.ProfessionalProfileRoute

import com.cis_ac.cis_ac.ui.feature.patient.appointments.PatientAppointmentsRoute
import com.cis_ac.cis_ac.ui.feature.patient.appointments.new.NewAppointmentRoute
import com.cis_ac.cis_ac.ui.feature.patient.history.PatientHistoryRoute
import com.cis_ac.cis_ac.ui.feature.patient.history.PatientHistoryRouteForPro
import com.cis_ac.cis_ac.ui.feature.patient.networks.PatientNetworksRoute
import com.cis_ac.cis_ac.ui.feature.patient.networks.PatientPostDetailRoute
import com.cis_ac.cis_ac.ui.feature.patient.profile.PatientProfileRoute

import com.cis_ac.cis_ac.ui.feature.professional.appointments.ProfessionalAppointmentsRoute
import com.cis_ac.cis_ac.ui.feature.professional.appointments.detail.ProfessionalAppointmentDetailRoute

import com.cis_ac.cis_ac.ui.feature.professional.networks.ProfessionalNetworksRoute
import com.cis_ac.cis_ac.ui.feature.professional.networks.ProfessionalNewPostRoute
import com.cis_ac.cis_ac.ui.feature.professional.networks.ProfessionalPostDetailRoute

import com.cis_ac.cis_ac.ui.feature.professional.patients.ProfessionalPatientsRoute
import com.cis_ac.cis_ac.ui.feature.professional.schedule.ProfessionalScheduleRoute

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNav() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Route.Login.path) {

        // Login
        composable(Route.Login.path) {
            LoginScreen(
                onLoginSuccess = {
                    nav.navigate(Route.Home.path) {
                        popUpTo(Route.Login.path) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onForgot = { nav.navigate(Route.Forgot.path) },
                onRegisterPatient = { nav.navigate(Route.RegisterPatient.path) },
                onRegisterPro = { nav.navigate(Route.RegisterPro.path) }
            )
        }

        // Home enrutado por rol
        composable(Route.Home.path) {
            HomeRouterRoute(
                onGoToLogin = { nav.navigate(Route.Login.path) { popUpTo(0) } },

                onNavigateFromPatient = { dest ->
                    when (dest) {
                        PatientHomeNav.Profile -> nav.navigate(Route.PatientProfile.path)
                        PatientHomeNav.ProfessionalsList ->
                            nav.navigate(Route.PatientProfessionals.path) { launchSingleTop = true }

                        PatientHomeNav.AppointmentsTab ->
                            nav.navigate(Route.PatientAppointments.path)

                        PatientHomeNav.MessagesTab ->
                            nav.navigate(Route.ChatList.path)

                        PatientHomeNav.SupportNetwork ->
                            nav.navigate(Route.PatientNetworks.path)

                        PatientHomeNav.ClinicalHistory ->
                            nav.navigate(Route.PatientHistory.path)

                        PatientHomeNav.AppointmentDetail -> { /* ... */ }
                    }
                },

                onNavigateFromPro = { dest ->
                    when (dest) {
                        ProfessionalHomeNav.Profile      -> nav.navigate(Route.ProfessionalProfile.path)
                        ProfessionalHomeNav.Patients     -> nav.navigate(Route.ProfessionalPatients.path)
                        ProfessionalHomeNav.Messages     -> nav.navigate(Route.ChatList.path)
                        ProfessionalHomeNav.Networks     -> nav.navigate(Route.ProfessionalNetworks.path)
                        ProfessionalHomeNav.Availability -> nav.navigate(Route.ProfessionalSchedule.path)
                        ProfessionalHomeNav.Appointments -> nav.navigate(Route.ProfessionalAppointments.path)
                        is ProfessionalHomeNav.AppointmentDetail ->
                            nav.navigate(Route.ProfessionalAppointmentDetail.path(dest.id))
                    }
                },

                onNavigateFromAdmin = { dest ->
                    when (dest) {
                        AdminHomeNav.Home -> Unit
                        AdminHomeNav.Professionals -> nav.navigate(Route.AdminProfessionals.path)
                        AdminHomeNav.Requests -> nav.navigate(Route.AdminRequests.path)
                    }
                },
                onAdminSignedOut = {
                    nav.navigate(Route.Login.path) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                }
            )
        }

        // Admin: listado de profesionales
        composable(Route.AdminProfessionals.path) {
            ProfessionalsRoute(
                onBack = { nav.popBackStack() },
                onOpenProfile = { uid -> nav.navigate(Route.AdminProfessionalDetail.path(uid)) }
            )
        }

        // Admin: solicitudes
        composable(Route.AdminRequests.path) {
            RequestsRoute(
                onBack = { nav.popBackStack() },
                onOpenDetail = { uid -> nav.navigate(Route.AdminRequestDetail.path(uid)) }
            )
        }

        // Admin: detalle de solicitud
        composable(
            route = Route.AdminRequestDetail.path,
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { entry ->
            val uid = entry.arguments?.getString("uid") ?: return@composable
            RequestDetailRoute(
                uid = uid,
                onBack = { nav.popBackStack() },
                onApproved = { /* opcional */ },
                onRejected = { /* opcional */ }
            )
        }

        // Admin: detalle de profesional (readonly)
        composable(
            route = Route.AdminProfessionalDetail.path,
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { entry ->
            val uid = entry.arguments?.getString("uid") ?: return@composable
            RequestDetailRoute(
                uid = uid,
                onBack = { nav.popBackStack() },
                showActions = false
            )
        }

        // Auth auxiliares
        composable(Route.Forgot.path) { Text("Recuperar contraseña") }
        composable(Route.RegisterPatient.path) {
            PatientSignUpScreen(
                onBack = { nav.navigate(Route.Login.path) { popUpTo(0) } },
                onNavigateToLogin = { nav.navigate(Route.Login.path) { popUpTo(0) } }
            )
        }
        composable(Route.RegisterPro.path) {
            ProfessionalSignUpScreen(
                onBack = { nav.navigate(Route.Login.path) { popUpTo(0) } },
                onNavigateToLogin = { nav.navigate(Route.Login.path) { popUpTo(0) } }
            )
        }

        // Paciente: perfil
        composable(Route.PatientProfile.path) {
            PatientProfileRoute(
                onBack = { nav.popBackStack() },
                onOpenClinicalHistory = { nav.navigate(Route.PatientHistory.path) },
                onSignedOut = {
                    nav.navigate(Route.Login.path) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                }
            )
        }

        // Paciente: historial clínico
        composable(Route.PatientHistory.path) {
            PatientHistoryRoute(
                onBack = { nav.popBackStack() },
                onSave = { /* opcional */ },
                onOpenSection = { /* opcional */ }
            )
        }

        // Paciente: listado de profesionales
        composable(Route.PatientProfessionals.path) {
            PatientProfessionalsRoute(
                onBack = { nav.popBackStack() },
                onOpenProfile = { uid -> nav.navigate(Route.PatientProfessionalProfile.path(uid)) },
                onOpenSchedule = { item: PatientProfessionalItem ->
                    nav.navigate(
                        Route.NewAppointment.fromProfessional(
                            discipline = item.discipline,
                            uid = item.uid,
                            name = item.fullName
                        )
                    )
                },
                onOpenChat = { item: PatientProfessionalItem ->
                    nav.navigate(Route.Chat.path(item.uid, item.fullName))
                }
            )
        }

        // Paciente: perfil de profesional (detalle)
        composable(
            route = Route.PatientProfessionalProfile.path,
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { entry ->
            val uid = entry.arguments?.getString("uid") ?: return@composable
            ProfessionalProfileRoute(
                uid = uid,
                onBack = { nav.popBackStack() },
                onChat = { professionalId ->
                    nav.navigate(Route.StartChat.path(professionalId))
                }
            )
        }

        // Paciente: citas
        composable(Route.PatientAppointments.path) {
            PatientAppointmentsRoute(
                navController = nav,
                onBack = { nav.popBackStack() },
                onNewAppointment = { nav.navigate(Route.NewAppointment.path()) },
                onOpenAppointment = { /* ... */ }
            )
        }

        // Paciente: pantalla "Agendar cita"
        composable(
            route = Route.NewAppointment.path,
            arguments = listOf(
                navArgument("discipline") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("uid")        { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("name")       { type = NavType.StringType; nullable = true; defaultValue = null },
            )
        ) { entry ->
            val dName   = entry.arguments?.getString("discipline")
            val proId   = entry.arguments?.getString("uid")
            val proName = entry.arguments?.getString("name")?.let { java.net.URLDecoder.decode(it, "UTF-8") }

            NewAppointmentRoute(
                preselectedDisciplineName = dName,
                preselectedProfessionalId = proId,
                preselectedProfessionalName = proName,
                onBack = { nav.popBackStack() },
                onCreatedWithMessage = { msg ->
                    val popped = nav.popBackStack(Route.PatientAppointments.path, inclusive = false)
                    if (popped) {
                        nav.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("snackbar_message", msg)
                    } else {
                        nav.navigate(Route.PatientAppointments.path) {
                            popUpTo(Route.Home.path) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        // Profesional: citas
        composable(Route.ProfessionalAppointments.path) {
            com.cis_ac.cis_ac.ui.feature.professional.appointments.ProfessionalAppointmentsRoute(
                navController = nav,
                onBack = { nav.popBackStack() },
                onOpenDetail = { id -> nav.navigate(Route.ProfessionalAppointmentDetail.path(id)) }
            )
        }

        // Profesional: detalle de cita
        composable(
            route = Route.ProfessionalAppointmentDetail.path,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString("id") ?: return@composable
            ProfessionalAppointmentDetailRoute(
                appointmentId = id,
                onBack = { nav.popBackStack() },
                onUpdated = { _, msg: String ->
                    nav.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("pro_snackbar_msg", msg)
                    nav.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("pro_snackbar_refresh", true)
                    nav.popBackStack()
                }
            )
        }

        // Profesional: perfil
        composable(Route.ProfessionalProfile.path) {
            com.cis_ac.cis_ac.ui.feature.professional.profile.ProfessionalProfileRoute(
                onBack = { nav.popBackStack() },
                onSignedOut = {
                    nav.navigate(Route.Login.path) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                }
            )
        }

        // Profesional: pacientes
        composable(Route.ProfessionalPatients.path) {
            ProfessionalPatientsRoute(
                onBack = { nav.popBackStack() },
                onOpenHistory = { patientUid ->
                    nav.navigate(Route.ProfessionalPatientHistory.path(patientUid))
                },
                onMessage = { }
            )
        }

        composable(Route.ProfessionalSchedule.path) {
            ProfessionalScheduleRoute(
                onBack = { nav.popBackStack() }
            )
        }

        // Profesional: historial de paciente (pro)
        composable(
            route = Route.ProfessionalPatientHistory.path,
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { entry ->
            val patientUid = entry.arguments?.getString("uid") ?: return@composable
            PatientHistoryRouteForPro(
                patientUid = patientUid,
                onBack = { nav.popBackStack() }
            )
        }

        // Profesional: feed redes con snackbar de retorno
        composable(Route.ProfessionalNetworks.path) { entry ->
            val msg = entry.savedStateHandle.get<String>("pro_networks_snackbar")
            LaunchedEffect(Unit) { entry.savedStateHandle.remove<String>("pro_networks_snackbar") }

            ProfessionalNetworksRoute(
                onBack = { nav.popBackStack() },
                onOpenNewPost = { nav.navigate(Route.ProfessionalNewPost.path) },
                onOpenDetail = { postId -> nav.navigate(Route.ProfessionalPostDetail.path(postId)) },
                initialSnackbar = msg
            )
        }

        // Profesional: detalle de post
        composable(
            route = Route.ProfessionalPostDetail.path,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { entry ->
            val postId = entry.arguments?.getString("postId") ?: return@composable
            ProfessionalPostDetailRoute(
                postId = postId,
                onBack = { nav.popBackStack() }
            )
        }

        // Profesional: nuevo post
        composable(Route.ProfessionalNewPost.path) {
            ProfessionalNewPostRoute(
                onBack = { nav.popBackStack() },
                onPosted = { msg: String ->
                    nav.popBackStack()
                    nav.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("pro_networks_snackbar", msg)
                }
            )
        }

        // Paciente: Redes (feed)
        composable(Route.PatientNetworks.path) {
            PatientNetworksRoute(
                onBack = { nav.popBackStack() },
                onOpenDetail = { postId -> nav.navigate(Route.PatientPostDetail.path(postId)) }
            )
        }

        // Paciente: detalle de post
        composable(
            route = Route.PatientPostDetail.path,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { entry ->
            val postId = entry.arguments?.getString("postId") ?: return@composable
            PatientPostDetailRoute(
                postId = postId,
                onBack = { nav.popBackStack() }
            )
        }

        // Chat: lista
        composable(Route.ChatList.path) {
            com.cis_ac.cis_ac.ui.feature.chat.chatList.ChatListScreen(
                onChatClick = { otherUserId, otherUserName ->
                    nav.navigate(Route.Chat.path(otherUserId, otherUserName))
                },
                onBackClick = { nav.popBackStack() }
            )
        }

        // Chat: iniciar chat desde perfil
        composable(
            route = Route.StartChat.path,
            arguments = listOf(navArgument("otherUserId") { type = NavType.StringType })
        ) { entry ->
            val otherUserId = entry.arguments?.getString("otherUserId") ?: return@composable
            com.cis_ac.cis_ac.ui.feature.chat.chat.StartChatScreen(
                otherUserId = otherUserId,
                onNavigateToChat = { userId, userName ->
                    nav.navigate(Route.Chat.path(userId, userName)) {
                        popUpTo(Route.StartChat.path) { inclusive = true }
                    }
                },
                onBackClick = { nav.popBackStack() }
            )
        }

        // Chat: conversación
        composable(
            route = Route.Chat.path,
            arguments = listOf(
                navArgument("otherUserId") { type = NavType.StringType },
                navArgument("otherUserName") { type = NavType.StringType }
            )
        ) { entry ->
            val otherUserId = entry.arguments?.getString("otherUserId") ?: return@composable
            val otherUserName = entry.arguments?.getString("otherUserName")?.let {
                java.net.URLDecoder.decode(it, "UTF-8")
            } ?: "Usuario"

            com.cis_ac.cis_ac.ui.feature.chat.chat.ChatScreen(
                otherUserId = otherUserId,
                otherUserName = otherUserName,
                onBackClick = { nav.popBackStack() }
            )
        }
    }
}
