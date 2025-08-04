package com.github.jibbo.norwegiantraining.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

interface SessionRepository {
    suspend fun getSessions(limit: Int = 30, offset: Int = 0): List<Session>
    suspend fun upsertSession(session: Session)
    suspend fun getTodaySession(): Session?
}

class PersistentSessionRepository @Inject constructor(
    private val sessionDao: SessionDao
) : SessionRepository {

    override suspend fun getSessions(
        limit: Int,
        offset: Int
    ): List<Session> = sessionDao.getAll(limit, offset)

    override suspend fun upsertSession(session: Session) = sessionDao.upsert(session)

    override suspend fun getTodaySession() = sessionDao.getTodaySession()
}

@Module
@InstallIn(SingletonComponent::class)
interface SessionRepositoryModule {
    @Binds
    @Singleton
    fun bindSessionRepository(persistentLogRepository: PersistentSessionRepository): SessionRepository
}
