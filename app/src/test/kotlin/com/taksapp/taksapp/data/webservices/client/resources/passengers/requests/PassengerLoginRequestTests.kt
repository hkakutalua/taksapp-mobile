package com.taksapp.taksapp.data.webservices.client.resources.passengers.requests

import com.taksapp.taksapp.data.webservices.client.*
import com.taksapp.taksapp.data.webservices.client.httpclients.OkHttpClientAdapter
import com.taksapp.taksapp.data.webservices.client.jsonconverters.MoshiJsonConverterAdapter
import com.taksapp.taksapp.data.webservices.client.resources.passengers.errors.LoginRequestError
import com.taksapp.taksapp.utils.FileUtilities
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.runners.MockitoJUnitRunner
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

@ExperimentalTime
@RunWith(MockitoJUnitRunner::class)
class PassengerLoginRequestTests {
    private val successfulLoginBodyPath = "json/passengers/login/successful_login_body.json"
    private val incorrectCredentialsBodyPath = "json/passengers/login/incorrect_credentials_body.json"
    private val accountDoesNotExistsBodyPath = "json/passengers/login/account_does_not_exists_body.json"

    @Test
    fun login_httpRequestMade_invokeCorrectEndpoint() {
        // Arrange
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(FileUtilities.getFileContent(successfulLoginBodyPath)))
        server.start()

        val provider = getConfigurationProvider(baseUrl = server.url("").toString())
        val request = PassengerLoginRequest.Builder(provider, provider.authenticationTokensStore)
            .email("henrick@mail.com")
            .password("1234567")
            .pushNotificationToken("abcdxyz1234")
            .build()

        // Act
        request.login()

        // Assert
        val recordedRequest = server.takeRequest()
        Assert.assertEquals("/api/v1/passengers/login", recordedRequest.path?.toLowerCase())
    }

    @Test
    fun login_successfulLogin_returnsSuccessfulResponse() {
        // Arrange
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(FileUtilities.getFileContent(successfulLoginBodyPath)))
        server.start()

        val provider = getConfigurationProvider(baseUrl = server.url("").toString())
        val request = PassengerLoginRequest.Builder(provider, provider.authenticationTokensStore)
            .email("henrick@mail.com")
            .password("1234567")
            .pushNotificationToken("abcdxyz1234")
            .build()

        // Act
        val response = request.login()

        // Assert
        Assert.assertTrue(response.successful)
        Assert.assertNull(response.errorCode)
    }

    @Test
    fun login_successfulLogin_storeAuthenticationTokens() {
        // Arrange
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(FileUtilities.getFileContent(successfulLoginBodyPath)))
        server.start()

        val tokensStoreMock = Mockito.mock(AuthenticationTokensStore::class.java)

        val provider = getConfigurationProvider(
            baseUrl = server.url("").toString(),
            store = tokensStoreMock
        )

        val request = PassengerLoginRequest.Builder(provider, provider.authenticationTokensStore)
            .email("henrick@mail.com")
            .password("1234567")
            .pushNotificationToken("abcdxyz1234")
            .build()

        // Act
        request.login()

        // Assert
        Mockito.verify(tokensStoreMock, Mockito.times(1)).saveRefreshToken("395db7da-be13-4a53-b696-b91453247bb7")
        Mockito.verify(tokensStoreMock, Mockito.times(1))
            .saveAccessToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
    }

    @Test
    fun login_incorrectCredentials_returnsIncorrectCredentialsError() {
        // Arrange
        val server = MockWebServer()
        server.enqueue(MockResponse()
            .setResponseCode(403)
            .setBody(FileUtilities.getFileContent(incorrectCredentialsBodyPath)))
        server.start()

        val provider = getConfigurationProvider(baseUrl = server.url("").toString())

        val request = PassengerLoginRequest.Builder(provider, provider.authenticationTokensStore)
            .email("henrick@mail.com")
            .password("1234567")
            .pushNotificationToken("abcdxyz1234")
            .build()

        // Act
        val response = request.login()

        // Assert
        Assert.assertFalse(response.successful)
        Assert.assertEquals(LoginRequestError.INVALID_CREDENTIALS, response.errorCode)
    }

    @Test
    fun login_nonExistentAccount_returnsAccountDoesNotExistsError() {
        // Arrange
        val server = MockWebServer()
        server.enqueue(MockResponse()
            .setResponseCode(403)
            .setBody(FileUtilities.getFileContent(accountDoesNotExistsBodyPath)))
        server.start()

        val provider = getConfigurationProvider(baseUrl = server.url("").toString())

        val request = PassengerLoginRequest.Builder(provider, provider.authenticationTokensStore)
            .email("henrick@mail.com")
            .password("1234567")
            .pushNotificationToken("abcdxyz1234")
            .build()

        // Act
        val response = request.login()

        // Assert
        Assert.assertFalse(response.successful)
        Assert.assertEquals(LoginRequestError.ACCOUNT_DOES_NOT_EXISTS, response.errorCode)
    }

    private fun getConfigurationProvider(
        store: AuthenticationTokensStore = Mockito.mock(AuthenticationTokensStore::class.java),
        baseUrl: String): ConfigurationProvider {
        return Taksapp.Builder()
            .environment(Environment.PRODUCTION)
            .client(OkHttpClientAdapter(baseUrl, timeout = 30.toDuration(TimeUnit.SECONDS)))
            .jsonConverter(MoshiJsonConverterAdapter())
            .authenticationTokensStore(store)
            .sessionExpiredCallback(Mockito.mock(SessionExpiredCallback::class.java))
            .build()
    }
}