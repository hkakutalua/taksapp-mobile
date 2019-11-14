package com.taksapp.taksapp.data.webservices.client.resources.users.requests

import com.taksapp.taksapp.data.webservices.client.*
import com.taksapp.taksapp.data.webservices.client.httpclients.OkHttpClientAdapter
import com.taksapp.taksapp.data.webservices.client.jsonconverters.MoshiJsonConverterAdapter
import com.taksapp.taksapp.data.webservices.client.resources.users.errors.LoginRequestError
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
class UserLoginRequestTests {
    private val successfulLoginBodyPath = "json/users/login/successful_login_body.json"
    private val incorrectCredentialsBodyPath = "json/users/login/incorrect_credentials_body.json"

    @Test
    fun login_httpRequestMade_invokeCorrectEndpoint() {
        // Arrange
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(204))
        server.enqueue(MockResponse().setBody(FileUtilities.getFileContent(successfulLoginBodyPath)))
        server.start()

        val taksapp = getTaksapp(baseUrl = server.url("").toString())
        val request = taksapp.users
            .loginRequestBuilder()
            .phoneNumber("+244925571908")
            .password("123456")
            .role(UserType.Rider)
            .build()

        // Act
        request.login()

        // Assert
        val userExistenceRequest = server.takeRequest()
        val authenticationRequest = server.takeRequest()

        Assert.assertEquals("/api/v1/users/+244925571908/verify-existence?role=rider",
            userExistenceRequest.path?.toLowerCase())
        Assert.assertEquals("/connect/token", authenticationRequest.path?.toLowerCase())
    }

    @Test
    fun login_successfulLogin_returnsSuccessfulResponse() {
        // Arrange
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(204))
        server.enqueue(MockResponse().setBody(FileUtilities.getFileContent(successfulLoginBodyPath)))
        server.start()

        val taksapp = getTaksapp(baseUrl = server.url("").toString())
        val request = taksapp.users
            .loginRequestBuilder()
            .phoneNumber("+244925571908")
            .password("123456")
            .role(UserType.Rider)
            .build()

        // Act
        val response = request.login()

        // Assert
        Assert.assertTrue(response.successful)
        Assert.assertNull(response.error)
    }

    @Test
    fun login_successfulLogin_storeAuthenticationTokensAndUserType() {
        // Arrange
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(204))
        server.enqueue(MockResponse().setBody(FileUtilities.getFileContent(successfulLoginBodyPath)))
        server.start()

        val storeMock = Mockito.mock(SessionStore::class.java)

        val taksapp = getTaksapp(baseUrl = server.url("").toString(), store = storeMock)
        val request = taksapp.users
            .loginRequestBuilder()
            .phoneNumber("+244925571908")
            .password("123456")
            .role(UserType.Rider)
            .build()

        // Act
        request.login()

        // Assert
        Mockito.verify(storeMock, Mockito.times(1)).saveRefreshToken("395db7da-be13-4a53-b696-b91453247bb7")
        Mockito.verify(storeMock, Mockito.times(1))
            .saveAccessToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
        Mockito.verify(storeMock, Mockito.times(1))
            .saveUserType(UserType.Rider)
    }

    @Test
    fun login_incorrectCredentials_returnsIncorrectCredentialsError() {
        // Arrange
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(204))
        server.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody(FileUtilities.getFileContent(incorrectCredentialsBodyPath))
        )
        server.start()

        val taksapp = getTaksapp(baseUrl = server.url("").toString())
        val request = taksapp.users
            .loginRequestBuilder()
            .phoneNumber("+244925571908")
            .password("123456")
            .role(UserType.Rider)
            .build()

        // Act
        val response = request.login()

        // Assert
        Assert.assertFalse(response.successful)
        Assert.assertEquals(LoginRequestError.INVALID_CREDENTIALS, response.error)
    }

    @Test
    fun login_nonExistentAccount_returnsAccountDoesNotExistsError() {
        // Arrange
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(404))
        server.start()

        val taksapp = getTaksapp(baseUrl = server.url("").toString())
        val request = taksapp.users
            .loginRequestBuilder()
            .phoneNumber("+244925571908")
            .password("123456")
            .role(UserType.Rider)
            .build()

        // Act
        val response = request.login()

        // Assert
        Assert.assertFalse(response.successful)
        Assert.assertEquals(LoginRequestError.ACCOUNT_DOES_NOT_EXISTS, response.error)
    }

    private fun getTaksapp(
        store: SessionStore = Mockito.mock(SessionStore::class.java),
        baseUrl: String
    ): Taksapp {
        return Taksapp.Builder()
            .environment(Environment.PRODUCTION)
            .client(OkHttpClientAdapter(baseUrl, timeout = 30.toDuration(TimeUnit.SECONDS)))
            .jsonConverter(MoshiJsonConverterAdapter())
            .sessionStore(store)
            .sessionExpiredCallback(Mockito.mock(SessionExpiredCallback::class.java))
            .build()
    }
}