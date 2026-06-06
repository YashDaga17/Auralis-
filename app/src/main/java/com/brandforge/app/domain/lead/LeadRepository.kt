package com.brandforge.app.domain.lead

import kotlinx.coroutines.flow.Flow

interface LeadRepository {
    fun observeInbox(creatorId: String, limit: Int = 50): Flow<List<Lead>>
    suspend fun persist(input: LeadDetectionInput): Lead
    suspend fun latestByCreator(creatorId: String, limit: Int = 30): List<Lead>
}
