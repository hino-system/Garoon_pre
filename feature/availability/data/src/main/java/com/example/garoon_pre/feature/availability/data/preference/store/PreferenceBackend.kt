package com.example.garoon_pre.feature.availability.preference.store

enum class PreferenceBackend {
    LOCAL,
    API,
    AWS,
    AZURE,
    GCP;

    companion object {
        fun from(value: String): PreferenceBackend {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: LOCAL
        }
    }
}