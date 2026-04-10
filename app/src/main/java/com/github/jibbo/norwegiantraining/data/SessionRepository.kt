package com.github.jibbo.norwegiantraining.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

interface SessionRepository {
    suspend fun getSessions(limit: Int = 30, offset: Int = 0): List<Session>
    suspend fun getSessionsInRange(from: Date, to: Date): List<Session>
    suspend fun upsertSession(session: Session): Long
    suspend fun insertSession(session: Session): Long
    suspend fun insertSessions(sessions: List<Session>)
    suspend fun getTodaySession(): Session?
}

class PersistentSessionRepository @Inject constructor(
    private val sessionDao: SessionDao
) : SessionRepository {

    override suspend fun getSessions(
        limit: Int,
        offset: Int
    ): List<Session> = sessionDao.getAll(limit, offset)

    override suspend fun getSessionsInRange(from: Date, to: Date): List<Session> =
        sessionDao.getInRange(from, to)

    override suspend fun upsertSession(session: Session): Long = sessionDao.upsert(session)

    override suspend fun insertSession(session: Session): Long = sessionDao.insert(session)

    override suspend fun insertSessions(sessions: List<Session>) = sessionDao.insert(sessions)

    override suspend fun getTodaySession(): Session? {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val startOfDay = cal.timeInMillis
        cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
        val endOfDay = cal.timeInMillis - 1
        return sessionDao.getTodaySession(startOfDay, endOfDay)
    }
}

@Module
@InstallIn(SingletonComponent::class)
interface SessionRepositoryModule {
    @Binds
    @Singleton
    fun bindSessionRepository(persistentLogRepository: PersistentSessionRepository): SessionRepository
}
