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

    override fun setAnalyticsEnabled(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getAnalyticsEnabled(): Boolean {
        TODO("Not yet implemented")
    }
}

class FakeTracker : Analytics {
    override fun logScreenView(name: String, clazz: Class<*>) {
        TODO("Not yet implemented")
    }

    override fun logChangeName() {
        TODO("Not yet implemented")
    }

    override fun logAnnouncePhase(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun logAnnounceDescriptionCurrentPhase(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun logAnnounceCountdownBeforeNextPhase(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun enable() {
        TODO("Not yet implemented")
    }

    override fun disable() {
        TODO("Not yet implemented")
    }

}
