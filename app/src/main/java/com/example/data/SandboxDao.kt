package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SandboxDao {
    @Query("SELECT * FROM sandbox_profiles ORDER BY timestamp DESC")
    fun getAllProfiles(): Flow<List<SandboxProfile>>

    @Query("SELECT * FROM sandbox_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Int): SandboxProfile?

    @Query("SELECT * FROM sandbox_profiles WHERE id = :id LIMIT 1")
    fun getProfileByIdFlow(id: Int): Flow<SandboxProfile?>

    @Query("SELECT * FROM sandbox_profiles WHERE isDirectLaunch = 1 LIMIT 1")
    suspend fun getDirectLaunchProfile(): SandboxProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: SandboxProfile): Long

    @Update
    suspend fun updateProfile(profile: SandboxProfile)

    @Delete
    suspend fun deleteProfile(profile: SandboxProfile)

    @Query("UPDATE sandbox_profiles SET isDirectLaunch = 0 WHERE id != :profileId")
    suspend fun clearOtherDirectLaunchFlags(profileId: Int)
}
