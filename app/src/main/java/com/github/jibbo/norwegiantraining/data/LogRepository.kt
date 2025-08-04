package com.github.jibbo.norwegiantraining.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

interface LogRepository {
    suspend fun getSessionLogs(limit: Int = 30, offset: Int = 0): List<Session>
    suspend fun upsertSession(session: Session)
}

class PersistentLogRepository @Inject constructor(
    private val sessionDao: SessionDao
) : LogRepository {

    override suspend fun getSessionLogs(
        limit: Int,
        offset: Int
    ): List<Session> = sessionDao.getAll(limit, offset)

    override suspend fun upsertSession(session: Session) = sessionDao.upsert(session)
}

@Module
@InstallIn(SingletonComponent::class)
interface LogRepositoryModule {
    @Binds
    @Singleton
    fun bindLogRepository(persistentLogRepository: PersistentLogRepository): LogRepository
}