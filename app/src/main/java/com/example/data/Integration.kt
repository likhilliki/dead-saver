package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "integrations")
data class Integration(
    @PrimaryKey val id: String, // "google", "outlook", "github"
    val isConnected: Boolean,
    val accountEmail: String
)
