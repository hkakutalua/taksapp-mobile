package com.taksapp.taksapp.data.webservices.client.resources.passengers.requests

import com.taksapp.taksapp.data.webservices.client.*
import com.taksapp.taksapp.data.webservices.client.httpclients.OkHttpClientAdapter
import com.taksapp.taksapp.data.webservices.client.jsonconverters.MoshiJsonConverterAdapter
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Test
import org.mockito.Mockito

class PassengerLoginRequestTests {
    @Test
    fun login_correctLoginCredentials_returnsSuccessfulResponse() {
        val provider = getConfigurationProvider()

        val mockedResponse = MockResponse()
            .setBody("{\"accessToken\": \"...\", \"refreshToken\": \"...\"}")

        val server = MockWebServer()
        server.enqueue(mockedResponse)
        server.start(port = 5000)

        val request = PassengerLoginRequest.Builder(provider, provider.authenticationTokensStore)
            .email("henrick@mail.com")
            .password("1234567")
            .pushNotificationToken("abcdxyz1234")
            .build()

        val response = request.login()

        server.shutdown()
    }

    private fun getConfigurationProvider(): ConfigurationProvider {
        return Taksapp.Builder()
            .environment(Environment.PRODUCTION)
            .client(OkHttpClientAdapter("http://localhost:5000/api/v1/"))
            .jsonConverter(MoshiJsonConverterAdapter())
            .authenticationTokensStore(Mockito.mock(AuthenticationTokensStore::class.java))
            .sessionExpiredCallbacl(Mockito.mock(SessionExpiredCallback::class.java))
            .build()
    }
}