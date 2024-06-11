package com.mrsajal.plugins

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


val redirects = mutableMapOf<String, String>()


fun Routing.googleAuthRoute(httpClient: HttpClient = applicationHttpClient) {

    authenticate("auth-oauth-google") {
        get("/login") {

        }

        get("/callback") {
            val currentPrincipal: OAuthAccessTokenResponse.OAuth2? = call.principal()
            currentPrincipal?.let { principal ->
                principal.state?.let { state ->
                    call.sessions.set(UserSession(state, principal.accessToken))
                    redirects[state]?.let { redirect ->
                        call.respondRedirect(redirect)
                        return@get
                    }
                }
            }
            call.respondRedirect("/home")
        }
    }
    get("/") {
        call.respondHtml {
            body {
                p {
                    a("/login") { +"Login with Google" }
                }
            }
        }
    }
    get("/home") {
        val userSession: UserSession? = getSession(call)
        if (userSession != null) {
            val userInfo: UserInfo = getPersonalGreeting(httpClient, userSession)
            call.respondText("Hello, ${userInfo.name}! Welcome home!")
        }
    }
    get("/{path}") {
        val userSession: UserSession? = getSession(call)
        if (userSession != null) {
            val userInfo: UserInfo = getPersonalGreeting(httpClient, userSession)
            call.respondText("Hello, ${userInfo.name}!")
        }
    }

}

private suspend fun getPersonalGreeting(
    httpClient: HttpClient,
    userSession: UserSession
): UserInfo = httpClient.get("https://www.googleapis.com/oauth2/v2/userinfo") {
    headers {
        append(HttpHeaders.Authorization, "Bearer ${userSession.token}")
    }
}.body()

private suspend fun getSession(
    call: ApplicationCall
): UserSession? {
    val userSession: UserSession? = call.sessions.get()
    if (userSession == null) {
        val redirectUrl = URLBuilder("http://0.0.0.0:8080/login").run {
            parameters.append("redirectUrl", call.request.uri)
            build()
        }
        call.respondRedirect(redirectUrl)
        return null
    }
    return userSession
}

@Serializable
data class UserInfo(
    val id: String,
    val name: String,
    @SerialName("given_name") val givenName: String,
    @SerialName("family_name") val familyName: String,
    val picture: String,
    val locale: String
)
