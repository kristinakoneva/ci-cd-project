package infinumacademy.showsapp.kristinakoneva.networking

import android.content.Context
import infinumacademy.showsapp.kristinakoneva.Constants
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(context: Context) : Interceptor {
    private val sessionManager = SessionManager(context)

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        requestBuilder.addHeader(Constants.TOKEN_TYPE, Constants.BEARER)

        if (Session.accessToken != null && Session.client != null &&
            Session.uid != null && Session.expiry != null && Session.contentType != null
        ) {
            requestBuilder.addHeader(Constants.ACCESS_TOKEN, Session.accessToken!!)
            requestBuilder.addHeader(Constants.CLIENT, Session.client!!)
            requestBuilder.addHeader(Constants.EXPIRY, Session.expiry!!)
            requestBuilder.addHeader(Constants.UID, Session.uid!!)
            requestBuilder.addHeader(Constants.CONTENT_TYPE, Session.contentType!!)

        } else {
            sessionManager.fetchAuthToken()?.let {
                requestBuilder.addHeader(Constants.ACCESS_TOKEN, it)
            }
            sessionManager.fetchClient()?.let {
                requestBuilder.addHeader(Constants.CLIENT, it)
            }
            sessionManager.fetchExpiry()?.let {
                requestBuilder.addHeader(Constants.EXPIRY, it)
            }
            sessionManager.fetchUID()?.let {
                requestBuilder.addHeader(Constants.UID, it)
            }
            sessionManager.fetchContentType()?.let {
                requestBuilder.addHeader(Constants.CONTENT_TYPE, it)
            }
        }

        return chain.proceed(requestBuilder.build())
    }
}