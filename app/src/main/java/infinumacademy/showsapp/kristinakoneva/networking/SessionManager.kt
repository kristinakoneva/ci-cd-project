package infinumacademy.showsapp.kristinakoneva.networking

import android.content.Context
import android.content.SharedPreferences
import infinumacademy.showsapp.kristinakoneva.Constants

@Suppress("TooManyFunctions")
class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences(Constants.SHOWS_APP, Context.MODE_PRIVATE)

    fun saveSession(token: String, client: String, expiry: String, uid: String, contentType: String) {
        saveAuthToken(token)
        saveClient(client)
        saveExpiry(expiry)
        saveUID(uid)
        saveContentType(contentType)
    }

    fun clearSession() {
        val editor = prefs.edit()
        editor.putString(Constants.ACCESS_TOKEN, null)
        editor.putString(Constants.CLIENT, null)
        editor.putString(Constants.EXPIRY, null)
        editor.putString(Constants.UID, null)
        editor.putString(Constants.CONTENT_TYPE, null)
        editor.apply()
    }

    private fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(Constants.ACCESS_TOKEN, token)
        editor.apply()
    }

    fun fetchAuthToken(): String? {
        return prefs.getString(Constants.ACCESS_TOKEN, null)
    }

    private fun saveClient(client: String) {
        val editor = prefs.edit()
        editor.putString(Constants.CLIENT, client)
        editor.apply()
    }

    fun fetchClient(): String? {
        return prefs.getString(Constants.CLIENT, null)
    }

    private fun saveExpiry(expiry: String) {
        val editor = prefs.edit()
        editor.putString(Constants.EXPIRY, expiry)
        editor.apply()
    }

    fun fetchExpiry(): String? {
        return prefs.getString(Constants.EXPIRY, null)
    }

    private fun saveUID(uid: String) {
        val editor = prefs.edit()
        editor.putString(Constants.UID, uid)
        editor.apply()
    }

    fun fetchUID(): String? {
        return prefs.getString(Constants.UID, null)
    }

    private fun saveContentType(contentType: String) {
        val editor = prefs.edit()
        editor.putString(Constants.CONTENT_TYPE, contentType)
        editor.apply()
    }

    fun fetchContentType(): String? {
        return prefs.getString(Constants.CONTENT_TYPE, null)
    }
}