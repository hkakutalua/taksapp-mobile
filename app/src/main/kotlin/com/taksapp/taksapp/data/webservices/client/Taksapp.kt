package com.taksapp.taksapp.data.webservices.client

import com.taksapp.taksapp.data.webservices.client.httpclients.HttpClient
import com.taksapp.taksapp.data.webservices.client.jsonconverters.JsonConverter
import com.taksapp.taksapp.data.webservices.client.resources.passengers.PassengersResource

enum class Environment{
    DEVELOPMENT,
    STAGING,
    PRODUCTION,
}

interface ConfigurationProvider {
    val client: HttpClient
    val jsonConverter : JsonConverter
    val authenticationTokensStore: AuthenticationTokensStore
}

public class Taksapp (
    val environment: Environment,
    val sessionExpiredCallback: SessionExpiredCallback,
    override val authenticationTokensStore: AuthenticationTokensStore,
    override val client: HttpClient,
    override val jsonConverter: JsonConverter) : ConfigurationProvider {

    class Builder {
        private lateinit var environment : Environment
        private lateinit var sessionExpiredCallback: SessionExpiredCallback
        private lateinit var authenticationTokensStore: AuthenticationTokensStore
        private lateinit var client: HttpClient
        private lateinit var jsonConverter: JsonConverter

        fun environment(environment: Environment) : Builder {
            this.environment = environment
            return this
        }

        fun sessionExpiredCallback(callback: SessionExpiredCallback): Builder {
            this.sessionExpiredCallback = callback
            return this
        }

        fun authenticationTokensStore(store: AuthenticationTokensStore): Builder {
            this.authenticationTokensStore = store
            return this
        }

        fun client(client: HttpClient): Builder {
            this.client = client
            return this
        }

        fun jsonConverter(jsonConverter: JsonConverter): Builder {
            this.jsonConverter = jsonConverter
            return this
        }

        fun build(): Taksapp {
            return Taksapp(
                environment = this.environment,
                sessionExpiredCallback = this.sessionExpiredCallback,
                authenticationTokensStore = this.authenticationTokensStore,
                client = this.client,
                jsonConverter = this.jsonConverter
            )
        }
    }

    val passengers: PassengersResource get() = PassengersResource(this, store = this.authenticationTokensStore)
}