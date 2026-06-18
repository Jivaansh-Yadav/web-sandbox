package com.example.data

import kotlinx.coroutines.flow.Flow

class SandboxRepository(private val sandboxDao: SandboxDao) {
    val allProfiles: Flow<List<SandboxProfile>> = sandboxDao.getAllProfiles()

    suspend fun getProfileById(id: Int): SandboxProfile? = sandboxDao.getProfileById(id)

    fun getProfileByIdFlow(id: Int): Flow<SandboxProfile?> = sandboxDao.getProfileByIdFlow(id)

    suspend fun getDirectLaunchProfile(): SandboxProfile? = sandboxDao.getDirectLaunchProfile()

    suspend fun insertProfile(profile: SandboxProfile): Long {
        val id = sandboxDao.insertProfile(profile)
        if (profile.isDirectLaunch) {
            sandboxDao.clearOtherDirectLaunchFlags(id.toInt())
        }
        return id
    }

    suspend fun updateProfile(profile: SandboxProfile) {
        sandboxDao.updateProfile(profile)
        if (profile.isDirectLaunch) {
            sandboxDao.clearOtherDirectLaunchFlags(profile.id)
        }
    }

    suspend fun deleteProfile(profile: SandboxProfile) {
        sandboxDao.deleteProfile(profile)
    }
}
