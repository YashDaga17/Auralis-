package com.brandforge.app.domain.twin

import kotlinx.coroutines.flow.Flow

interface TwinChatRepository {
    fun observeMessages(creatorId: String, limit: Int = 80): Flow<List<TwinChatMessage>>
    suspend fun persistMessage(message: TwinChatMessageDraft): TwinChatMessage
    suspend fun latestMessages(creatorId: String, limit: Int = 12): List<TwinChatMessage>
}
