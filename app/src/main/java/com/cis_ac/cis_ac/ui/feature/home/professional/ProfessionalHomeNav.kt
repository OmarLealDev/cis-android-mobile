package com.cis_ac.cis_ac.ui.feature.home.professional

sealed interface ProfessionalHomeNav {
    data object Profile : ProfessionalHomeNav

    data object Patients : ProfessionalHomeNav
    data object Appointments : ProfessionalHomeNav
    data object Messages : ProfessionalHomeNav
    data object Networks : ProfessionalHomeNav
    data object Availability : ProfessionalHomeNav

    data class AppointmentDetail(val id: String) : ProfessionalHomeNav
}
