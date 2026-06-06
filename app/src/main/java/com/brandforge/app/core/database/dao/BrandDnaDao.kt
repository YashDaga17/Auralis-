package com.brandforge.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.brandforge.app.core.database.entity.BrandDnaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BrandDnaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: BrandDnaEntity): Long

    @Query("SELECT * FROM brand_dna WHERE creator_id = :creatorId")
    fun observeByCreator(creatorId: String): Flow<BrandDnaEntity?>

    @Query("SELECT * FROM brand_dna WHERE creator_id = :creatorId")
    suspend fun getByCreator(creatorId: String): BrandDnaEntity?
}
