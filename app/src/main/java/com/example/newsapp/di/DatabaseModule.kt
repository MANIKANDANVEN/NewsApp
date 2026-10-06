package com.example.newsapp.di

import android.content.Context
import androidx.room.Room
import com.example.newsapp.data.local.AppDatabase
import com.example.newsapp.data.local.ArticleDao
import com.example.newsapp.data.local.HeadlineDao
import com.example.newsapp.data.local.SourcePreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "news_database"
        )
            // cached_headlines is a disposable network cache, not user data,
            // so a schema bump can just drop and recreate it.
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideArticleDao(db: AppDatabase): ArticleDao = db.articleDao()

    @Provides
    fun provideHeadlineDao(db: AppDatabase): HeadlineDao = db.headlineDao()

    @Provides
    @Singleton
    fun provideSourcePreferences(@ApplicationContext context: Context): SourcePreferences {
        return SourcePreferences(context)
    }
}