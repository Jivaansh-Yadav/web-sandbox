package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sandbox_profiles")
data class SandboxProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val url: String,
    val isDesktopMode: Boolean = false,
    val isPullToRefreshEnabled: Boolean = false,
    val customCss: String = "",
    val customJs: String = "",
    val cssHideSelectors: String = "",
    val blockExternalPages: Boolean = true,
    val isDirectLaunch: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
