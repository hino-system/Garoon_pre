package com.example.garoon_pre.core.model.user

data class GaroonUser(
    val id: String,
    val userId: String,
    val displayName: String,
    val department1: String? = null,
    val department2: String? = null,
    val position: String = "",
    val role: String = ""
) {
    val label: String
        get() = displayName.ifBlank { userId }

    val organizationLabel: String
        get() = listOfNotNull(
            department1?.takeIf { it.isNotBlank() },
            department2?.takeIf { it.isNotBlank() }
        ).joinToString(" / ")

    val subtitle: String
        get() = listOfNotNull(
            organizationLabel.takeIf { it.isNotBlank() },
            position.takeIf { it.isNotBlank() }
        ).joinToString(" / ")
}