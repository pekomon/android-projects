package com.pekomon.lockbox.domain.model

data class LockBoxSettings(
    val relockOnBackground: Boolean = true,
    val screenshotsProtected: Boolean = true,
)
