package com.example.newsapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface HeadlineDao {
    @Query("SELECT * FROM cached_headlines WHERE sourceIds = :sourceIds ORDER BY publishedAt DESC")
    fun observeHeadlines(sourceIds: String): Flow<List<CachedHeadlineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(headlines: List<CachedHeadlineEntity>)

    @Query("DELETE FROM cached_headlines WHERE sourceIds = :sourceIds")
    suspend fun clearForSources(sourceIds: String)

    // Swap the cached page for these sourceIds atomically so observers never see a
    // flash of emptiness between the delete and the insert.
    @Transaction
    suspend fun replaceHeadlines(sourceIds: String, headlines: List<CachedHeadlineEntity>) {
        clearForSources(sourceIds)
        insertAll(headlines)
    }
}
