package com.github.jibbo.norwegiantraining.data

class FakeSessionRepo : SessionRepository {
    override suspend fun getSessions(
        limit: Int,
        offset: Int
    ): List<Session> {
        TODO("Not yet implemented")
    }

    override suspend fun upsertSession(session: Session): Long {
        TODO("Not yet implemented")
    }

    override suspend fun getTodaySession(): Session? {
        TODO("Not yet implemented")
    }

}

class FakeUserPreferencesRepo : UserPreferencesRepo {
    override fun setUserName(name: String?) {
        TODO("Not yet implemented")
    }

    override fun getUserName(): String? = "Didi 🖤"

    override fun setAnnouncePhase(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getAnnouncePhase(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setAnnouncePhaseDesc(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getAnnouncePhaseDesc(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setAnnounceCountdown(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getAnnounceCountdown(): Boolean {
        TODO("Not yet implemented")
    }

}
