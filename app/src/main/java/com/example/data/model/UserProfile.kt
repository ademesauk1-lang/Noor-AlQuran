package com.example.data.model
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val full_name: String? = null,
    val last_read_surah: Int? = null,
    val last_read_ayah: Int? = null
)
