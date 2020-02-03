package com.taksapp.taksapp.data.webservices.client.httpclient.okhttpclient

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.taksapp.taksapp.data.webservices.client.SessionExpiryListener
import com.taksapp.taksapp.data.webservices.client.SessionStore
import com.taksapp.taksapp.data.webservices.client.httpclients.okhttpclient.TokenRefreshAuthenticator
import com.taksapp.taksapp.data.webservices.client.jsonconverters.MoshiJsonConverterAdapter
import com.taksapp.taksapp.utils.FileUtilities
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.ExperimentalTime

@ExperimentalTime
@RunWith(AndroidJUnit4::class)
class TokenRefreshAuthenticatorTests {
    private lateinit var sessionStoreMock: SessionStore
    private lateinit var sessionExpiryListenerMock: SessionExpiryListener

    @Before
    fun beforeEachTest() {
        sessionStoreMock = mock()
        sessionExpiryListenerMock = mock()
    }

    @Test
    fun refreshesAccessToken() {
        // Arrange
        val successfulTokenRefreshJson = "json/users/token-refresh/successful_token_refresh_body.json"

        val server = MockWebServer()
        val serverBaseUrl = server.url("").toString()
        val successfulResponse = MockResponse()
            .setResponseCode(200)
            .setBody(FileUtilities.getFileContent(successfulTokenRefreshJson))
        server.enqueue(successfulResponse)

        whenever(sessionStoreMock.getAccessToken()).thenReturn("old-access-token")
        whenever(sessionStoreMock.getRefreshToken()).thenReturn("old-refresh-token")

        val authenticator = buildAuthenticator(baseUrl = serverBaseUrl)

        // Act
        val authenticatorResultRequest = authenticator.authenticate(null,
            Response.Builder()
                .request(Request.Builder().url("http://endpoint-needing-auth").build())
                .protocol(Protocol.HTTP_2)
                .message("")
                .code(401)
                .build())

        // Assert
        val serverRecordedRequest = server.takeRequest()
        Assert.assertEquals("/connect/token", serverRecordedRequest.requestUrl?.toUrl()?.path)

        Assert.assertEquals("bearer new-access-token",
            authenticatorResultRequest?.header("Authorization")?.toLowerCase())

        verify(sessionStoreMock).saveAccessToken("new-access-token")
        verify(sessionStoreMock).saveRefreshToken("new-refresh-token")
    }

    @Test
    fun expiresSessionWhenTheresNoCachedRefreshToken() {
        // Arrange
        val server = MockWebServer()

        whenever(sessionStoreMock.getAccessToken()).thenReturn("")
        whenever(sessionStoreMock.getRefreshToken()).thenReturn("")

        val authenticator = buildAuthenticator(baseUrl = server.url("").toString())

        // Act
        val authenticatorResultRequest = authenticator.authenticate(null,
            Response.Builder()
                .request(Request.Builder().url("http://endpoint-needing-auth").build())
                .protocol(Protocol.HTTP_2)
                .message("")
                .code(401)
                .build())

        // Assert
        Assert.assertNull(authenticatorResultRequest)
        verify(sessionExpiryListenerMock).onSessionExpired()
    }

    @Test
    fun givesUpTokenRefreshAfterOneTry() {
        // Arrange
        val failedTokenRefreshJson = "json/users/token-refresh/failed_token_refresh_body.json"

        val server = MockWebServer()
        val serverBaseUrl = server.url("").toString()
        val failedResponse = MockResponse()
            .setResponseCode(400)
            .setBody(FileUtilities.getFileContent(failedTokenRefreshJson))
        server.enqueue(failedResponse)
        server.enqueue(failedResponse)

        whenever(sessionStoreMock.getAccessToken()).thenReturn("old-access-token")
        whenever(sessionStoreMock.getRefreshToken()).thenReturn("old-refresh-token")

        val authenticator = buildAuthenticator(baseUrl = server.url("").toString())

        // Act
        val authenticatorResultRequest = authenticator.authenticate(null,
            Response.Builder()
                .request(Request.Builder().url("http://endpoint-needing-auth").build())
                .protocol(Protocol.HTTP_2)
                .message("")
                .code(401)
                .build())

        // Assert
        Assert.assertNull(authenticatorResultRequest)
        verify(sessionExpiryListenerMock).onSessionExpired()
    }

    private fun buildAuthenticator(baseUrl: String) =
        TokenRefreshAuthenticator(
            baseUrl, sessionStoreMock, sessionExpiryListenerMock, MoshiJsonConverterAdapter()
        )
}