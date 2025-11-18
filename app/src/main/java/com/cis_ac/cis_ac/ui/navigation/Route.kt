package com.cis_ac.cis_ac.ui.navigation

import com.cis_ac.cis_ac.core.model.Discipline
import java.net.URLEncoder

sealed class Route(val path: String) {

    // Auth / app shell
    data object Login : Route("login")
    data object Home  : Route("home")
    data object Forgot : Route("forgot")
    data object RegisterPatient : Route("register_patient")
    data object RegisterPro : Route("register_pro")

    // Paciente: perfil e historial
    data object PatientProfile : Route("patient_profile")
    data object PatientHistory : Route("patient_history")
    data object PatientAppointments : Route("patient_appointments")

    data object ClinicalHistory   : Route("clinical_history")
    data object SupportNetwork    : Route("support_network")
    data object Professionals     : Route("professionals")
    data object AppointmentDetail : Route("appointment_detail")
    data object Appointments      : Route("appointments")
    data object Messages          : Route("messages")

    // Admin
    data object AdminHome          : Route("admin_home")
    data object AdminProfessionals : Route("admin_professionals")
    data object AdminRequests      : Route("admin_requests")
    data object AdminRequestDetail : Route("admin_request_detail/{uid}") {
        fun path(uid: String) = "admin_request_detail/$uid"
    }
    data object AdminProfessionalDetail : Route("admin_professional_detail/{uid}") {
        fun path(uid: String) = "admin_professional_detail/$uid"
    }

    // Paciente: profesionales
    data object PatientProfessionals : Route("patient_professionals")
    data object PatientProfessionalProfile : Route("patient_professional_profile/{uid}") {
        fun path(uid: String) = "patient_professional_profile/$uid"
    }

    // Chat
    data object ChatList : Route("chat_list")
    data object Chat : Route("chat/{otherUserId}/{otherUserName}") {
        fun path(otherUserId: String, otherUserName: String): String {
            val encodedName = URLEncoder.encode(otherUserName, "UTF-8")
            return "chat/$otherUserId/$encodedName"
        }
    }
    data object StartChat : Route("start_chat/{otherUserId}") {
        fun path(otherUserId: String): String = "start_chat/$otherUserId"
    }

    // Nueva cita con preselección
    data object NewAppointment :
        Route("new_appointment?discipline={discipline}&uid={uid}&name={name}") {

        fun path() = "new_appointment"

        fun fromProfessional(discipline: Discipline, uid: String, name: String): String {
            val encName = URLEncoder.encode(name, "UTF-8")
            return "new_appointment?discipline=${discipline.name}&uid=$uid&name=$encName"
        }
    }

    // Profesional: citas
    data object ProfessionalAppointments : Route("professional_appointments")
    data object ProfessionalAppointmentDetail : Route("professional_appointment_detail/{id}") {
        fun path(id: String) = "professional_appointment_detail/$id"
    }

    // Profesional: perfil propio
    data object ProfessionalProfile : Route("professional_profile")

    // Profesional: pacientes
    data object ProfessionalPatients : Route("pro/patients")

    // Profesional: disponibilidad
    data object ProfessionalSchedule : Route("professional_schedule")
    data object ProfessionalPatientHistory : Route("proPatientHistory/{uid}") {
        fun path(uid: String) = "proPatientHistory/$uid"
    }

    // Profesional: redes
    data object ProfessionalNetworks : Route("professional_networks")
    data object ProfessionalPostDetail : Route("professional_post_detail/{postId}") {
        fun path(postId: String) = "professional_post_detail/$postId"
    }
    data object ProfessionalNewPost : Route("professional_new_post")

    // Paciente: redes
    data object PatientNetworks : Route("patient/networks")
    data object PatientPostDetail : Route("patient/post/{postId}") {
        fun path(postId: String) = "patient/post/$postId"
    }
}
