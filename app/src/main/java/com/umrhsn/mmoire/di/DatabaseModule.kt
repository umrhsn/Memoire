package com.umrhsn.mmoire.di

import android.content.Context
import androidx.room.Room
import com.umrhsn.mmoire.db.AppDatabase
import com.umrhsn.mmoire.db.GameDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "memoire_db"
        ).build()
    }

    @Provides
    fun provideGameDao(database: AppDatabase): GameDao {
        return database.gameDao()
    }
}
