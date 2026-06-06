package com.brandforge.app.data.twin

import com.brandforge.app.core.database.dao.TwinChatMessageDao
import com.brandforge.app.domain.twin.TwinChatMessage
import com.brandforge.app.domain.twin.TwinChatMessageDraft
import com.brandforge.app.domain.twin.TwinChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TwinChatRepositoryImpl @Inject constructor(
    private val twinChatMessageDao: TwinChatMessageDao,
) : TwinChatRepository {
    override fun observeMessages(creatorId: String, limit: Int): Flow<List<TwinChatMessage>> =
        twinChatMessageDao.observeByCreator(creatorId, limit).map { rows ->
            rows.map { it.toDomain() }
        }

    override suspend fun persistMessage(message: TwinChatMessageDraft): TwinChatMessage {
        val entity = message.toEntity()
        twinChatMessageDao.upsert(entity)
        return entity.toDomain()
    }

    override suspend fun latestMessages(creatorId: String, limit: Int): List<TwinChatMessage> =
        twinChatMessageDao.latestByCreator(creatorId, limit)
            .asReversed()
            .map { it.toDomain() }
}
