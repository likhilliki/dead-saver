package com.example.data

import kotlinx.coroutines.flow.Flow

class EventRepository(
    private val eventDao: EventDao,
    private val integrationDao: IntegrationDao
) {
    val allEvents: Flow<List<Event>> = eventDao.getAllEvents()
    val allIntegrations: Flow<List<Integration>> = integrationDao.getAllIntegrations()

    suspend fun insertEvent(event: Event) {
        eventDao.insertEvent(event)
    }

    suspend fun updateEvent(event: Event) {
        eventDao.updateEvent(event)
    }

    suspend fun deleteEvent(event: Event) {
        eventDao.deleteEvent(event)
    }

    suspend fun deleteAllEvents() {
        eventDao.deleteAllEvents()
    }

    suspend fun insertIntegration(integration: Integration) {
        integrationDao.insertIntegration(integration)
    }
}
