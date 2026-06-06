package com.brandforge.app.data.lead

import com.brandforge.app.core.database.dao.LeadDao
import com.brandforge.app.domain.lead.Lead
import com.brandforge.app.domain.lead.LeadDetectionInput
import com.brandforge.app.domain.lead.LeadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeadRepositoryImpl @Inject constructor(
    private val leadDao: LeadDao,
) : LeadRepository {
    override fun observeInbox(creatorId: String, limit: Int): Flow<List<Lead>> =
        leadDao.observeInbox(creatorId, limit).map { rows ->
            rows.map { it.toDomain() }
        }

    override suspend fun persist(input: LeadDetectionInput): Lead {
        val entity = input.toEntity()
        leadDao.upsert(entity)
        return entity.toDomain()
    }

    override suspend fun latestByCreator(creatorId: String, limit: Int): List<Lead> =
        leadDao.latestByCreator(creatorId, limit).map { it.toDomain() }
}
