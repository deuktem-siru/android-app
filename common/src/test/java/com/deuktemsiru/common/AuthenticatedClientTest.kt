package com.deuktemsiru.common

import com.sun.net.httpserver.HttpServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.http.GET
import java.net.InetSocketAddress

class AuthenticatedClientTest {
    interface TestApi {
        @GET("private") fun request(): Call<Void>
    }

    private lateinit var server: HttpServer
    private var refreshStatus = 200
    private var refreshCount = 0
    private var requestCount = 0
    private var rejectNewToken = false
    private var mock = false
    private lateinit var client: TestClient

    private class TestClient(baseUrl: String, isMock: () -> Boolean) :
        AuthenticatedClient(baseUrl, isDebug = true, isMockSession = isMock) {
        val api = createApi(TestApi::class.java)
    }

    @Before fun setUp() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/private") { exchange ->
            requestCount++
            val accepted = !rejectNewToken && exchange.requestHeaders.getFirst("Authorization") == "Bearer new-token"
            exchange.sendResponseHeaders(if (accepted) 204 else 401, -1)
            exchange.close()
        }
        server.createContext("/api/v1/auth/refresh") { exchange ->
            refreshCount++
            assertEquals("POST", exchange.requestMethod)
            assertEquals("{\"refreshToken\":\"refresh-token\"}", exchange.requestBody.bufferedReader().use { it.readText() })
            val body = """{"code":200,"message":"ok","data":{"accessToken":"new-token"}}""".toByteArray()
            exchange.sendResponseHeaders(refreshStatus, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
            exchange.close()
        }
        server.start()
        client = TestClient("http://127.0.0.1:${server.address.port}/") { mock }
        client.accessToken = "old-token"
        client.refreshToken = "refresh-token"
    }

    @After fun tearDown() = server.stop(0)

    @Test fun `refreshes once and uses the new token on subsequent requests`() {
        var persistedToken: String? = null
        client.onTokenRefreshed = { persistedToken = it }
        assertEquals(204, client.api.request().execute().code())
        assertEquals(204, client.api.request().execute().code())
        assertEquals("new-token", persistedToken)
        assertEquals(1, refreshCount)
        assertEquals(3, requestCount)
    }

    @Test fun `invalid refresh credentials expire the session`() {
        for (status in listOf(400, 401)) {
            refreshStatus = status
            client.accessToken = "old-token"
            client.refreshToken = "refresh-token"
            var expired = false
            client.onSessionExpired = { expired = true }
            assertEquals(401, client.api.request().execute().code())
            assertTrue(expired)
            assertNull(client.accessToken)
            assertNull(client.refreshToken)
        }
    }

    @Test fun `temporary refresh failure keeps the session`() {
        refreshStatus = 500
        var expired = false
        client.onSessionExpired = { expired = true }
        assertEquals(401, client.api.request().execute().code())
        assertFalse(expired)
        assertEquals("old-token", client.accessToken)
        assertEquals("refresh-token", client.refreshToken)
    }

    @Test fun `mock and anonymous requests never attempt token refresh`() {
        mock = true
        assertEquals(401, client.api.request().execute().code())
        mock = false
        client.accessToken = null
        assertEquals(401, client.api.request().execute().code())
        assertEquals(0, refreshCount)
    }

    @Test fun `a second unauthorized response stops retrying`() {
        rejectNewToken = true
        assertEquals(401, client.api.request().execute().code())
        assertEquals(1, refreshCount)
        assertEquals(2, requestCount)
    }
}
