package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SandboxDatabase
import com.example.data.SandboxProfile
import com.example.data.SandboxRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SandboxViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SandboxRepository
    
    val allProfiles: StateFlow<List<SandboxProfile>>
    
    private val _activeSandbox = MutableStateFlow<SandboxProfile?>(null)
    val activeSandbox: StateFlow<SandboxProfile?> = _activeSandbox.asStateFlow()
    
    private val _editingProfile = MutableStateFlow<SandboxProfile?>(null)
    val editingProfile: StateFlow<SandboxProfile?> = _editingProfile.asStateFlow()

    private val _isShowingNewProfileForm = MutableStateFlow(false)
    val isShowingNewProfileForm: StateFlow<Boolean> = _isShowingNewProfileForm.asStateFlow()

    init {
        val database = SandboxDatabase.getDatabase(application)
        repository = SandboxRepository(database.sandboxDao())
        
        allProfiles = repository.allProfiles
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
            
        // Pre-populate empty database with rich preset sandboxes to instantly demonstrate functionality
        viewModelScope.launch {
            val currentList = repository.allProfiles.first()
            if (currentList.isEmpty()) {
                createPresets()
            }
            
            // Auto Direct Launch check on application start
            val directLaunch = repository.getDirectLaunchProfile()
            if (directLaunch != null) {
                _activeSandbox.value = directLaunch
            }
        }
    }

    private suspend fun createPresets() {
        val preset1 = SandboxProfile(
            name = "GitHub Dashboard",
            url = "https://github.com",
            isDesktopMode = false,
            isPullToRefreshEnabled = true,
            cssHideSelectors = ".js-header-wrapper, .banner-cookie-consent, div[class*='app-down']",
            customCss = "/* Clean modern scrolling without web header */\n.js-header-wrapper { display: none !important; }",
            blockExternalPages = false
        )
        val preset2 = SandboxProfile(
            name = "Linear Developer Screen",
            url = "https://linear.app",
            isDesktopMode = true,
            isPullToRefreshEnabled = false,
            cssHideSelectors = "footer, .CookieConsent",
            customCss = "body { background-color: #0b0b0f !important; }",
            blockExternalPages = true
        )
        val preset3 = SandboxProfile(
            name = "Notion Canvas",
            url = "https://notion.so",
            isDesktopMode = false,
            isPullToRefreshEnabled = false,
            customCss = ".notion-sidebar-container { border-right: 1px solid #1f1f1f !important; }",
            blockExternalPages = false
        )
        val preset4 = SandboxProfile(
            name = "Vercel Platform",
            url = "https://vercel.com/dashboard",
            isDesktopMode = true,
            isPullToRefreshEnabled = true,
            blockExternalPages = false
        )
        
        repository.insertProfile(preset1)
        repository.insertProfile(preset2)
        repository.insertProfile(preset3)
        repository.insertProfile(preset4)
    }

    fun launchSandbox(profile: SandboxProfile) {
        _activeSandbox.value = profile
    }

    fun exitSandbox() {
        _activeSandbox.value = null
    }

    fun startEditingProfile(profile: SandboxProfile) {
        _editingProfile.value = profile
        _isShowingNewProfileForm.value = true
    }

    fun startNewProfile() {
        _editingProfile.value = null
        _isShowingNewProfileForm.value = true
    }

    fun cancelEditing() {
        _editingProfile.value = null
        _isShowingNewProfileForm.value = false
    }

    fun saveProfile(
        name: String,
        url: String,
        isDesktopMode: Boolean,
        isPullToRefreshEnabled: Boolean,
        cssHideSelectors: String,
        customCss: String,
        customJs: String,
        blockExternalPages: Boolean,
        isDirectLaunch: Boolean
    ) {
        viewModelScope.launch {
            val formattedUrl = when {
                url.startsWith("http://") || url.startsWith("https://") -> url
                else -> "https://$url"
            }
            
            val current = _editingProfile.value
            if (current != null) {
                val updated = current.copy(
                    name = name,
                    url = formattedUrl,
                    isDesktopMode = isDesktopMode,
                    isPullToRefreshEnabled = isPullToRefreshEnabled,
                    cssHideSelectors = cssHideSelectors,
                    customCss = customCss,
                    customJs = customJs,
                    blockExternalPages = blockExternalPages,
                    isDirectLaunch = isDirectLaunch,
                    timestamp = System.currentTimeMillis()
                )
                repository.updateProfile(updated)
            } else {
                val newProfile = SandboxProfile(
                    name = name,
                    url = formattedUrl,
                    isDesktopMode = isDesktopMode,
                    isPullToRefreshEnabled = isPullToRefreshEnabled,
                    cssHideSelectors = cssHideSelectors,
                    customCss = customCss,
                    customJs = customJs,
                    blockExternalPages = blockExternalPages,
                    isDirectLaunch = isDirectLaunch
                )
                repository.insertProfile(newProfile)
            }
            _editingProfile.value = null
            _isShowingNewProfileForm.value = false
        }
    }

    fun deleteProfile(profile: SandboxProfile) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
            if (_activeSandbox.value?.id == profile.id) {
                _activeSandbox.value = null
            }
            if (_editingProfile.value?.id == profile.id) {
                _editingProfile.value = null
                _isShowingNewProfileForm.value = false
            }
        }
    }
}
