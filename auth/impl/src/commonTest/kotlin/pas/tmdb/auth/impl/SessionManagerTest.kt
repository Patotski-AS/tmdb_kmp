package pas.tmdb.auth.impl

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pas.tmdb.auth.api.SessionState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SessionManagerTest {

    @Test
    fun onLoginSuccess_persistsSessionAndUpdatesState() = runTest {
        val storage = FakeSessionStorage()
        val manager = SessionManager(storage)

        manager.onLoginSuccess(
            sessionId = "session-1",
            accountId = 42,
            username = "testuser",
        )

        assertEquals(
            SessionState.Authenticated(accountId = 42, username = "testuser"),
            manager.sessionState.value,
        )
        assertEquals("session-1", storage.sessionId)
        assertEquals(42, storage.accountId)
        assertEquals("testuser", storage.username)
        assertEquals("session-1", manager.getSessionId())
    }

    @Test
    fun logout_clearsSessionAndResetsState() = runTest {
        val storage = FakeSessionStorage()
        val manager = SessionManager(storage)
        manager.onLoginSuccess("session-1", 42, "testuser")

        manager.logout()

        assertEquals(SessionState.Guest, manager.sessionState.value)
        assertNull(storage.sessionId)
        assertNull(manager.getSessionId())
    }

    @Test
    fun restoreSession_onInit_loadsPersistedSession() = runTest {
        val storage = FakeSessionStorage().apply {
            sessionId = "restored-session"
            accountId = 7
            username = "restored"
        }
        val manager = SessionManager(storage)

        val state = manager.sessionState
            .filter { it is SessionState.Authenticated }
            .first()

        assertEquals(
            SessionState.Authenticated(accountId = 7, username = "restored"),
            state,
        )
        assertEquals("restored-session", manager.getSessionId())
    }
}
