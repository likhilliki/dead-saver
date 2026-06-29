package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val title: String,
    val source: String,
    val startTime: String, // ISO 8601 string, e.g., "2026-06-29T10:00:00Z"
    val location: String,
    val status: String, // "pending" | "resolved"
    val priorityScore: Int, // 1 to 10
    val taskCategory: String, // "Coding" | "Writing" | "Admin"
    val extractedAt: Long = System.currentTimeMillis()
) : Serializable
