package infinumacademy.showsapp.kristinakoneva.loginscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import infinumacademy.showsapp.kristinakoneva.Constants
import infinumacademy.showsapp.kristinakoneva.UserInfo
import infinumacademy.showsapp.kristinakoneva.model.LoginRequest
import infinumacademy.showsapp.kristinakoneva.model.LoginResponse
import infinumacademy.showsapp.kristinakoneva.networking.ApiModule
import infinumacademy.showsapp.kristinakoneva.networking.Session
import infinumacademy.showsapp.kristinakoneva.networking.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : ViewModel() {

    private val emailLiveData = MutableLiveData<String?>()
    private val passwordLiveData = MutableLiveData<String?>()
    private val _isValidLiveData = MediatorLiveData<Boolean>().apply {
        this.value = false

        addSource(emailLiveData) { email ->
            val password = passwordLiveData.value
            this.value = validateLoginForm(email, password)
        }
        addSource(passwordLiveData) { password ->
            val email = emailLiveData.value
            this.value = validateLoginForm(email, password)
        }
    }
    val isValidLiveData: LiveData<Boolean> = _isValidLiveData

    private val _isValidEmail = MutableLiveData(false)
    val isValidEmail: LiveData<Boolean> = _isValidEmail

    private val _isValidPassword = MutableLiveData(false)
    val isValidPassword: LiveData<Boolean> = _isValidPassword

    private fun validateLoginForm(email: String?, password: String?): Boolean {
        val isValidEmail = email != null && email.isNotBlank() && email.matches(Constants.EMAIL_REGEX.toRegex())
        val isValidPassword = password != null && password.isNotBlank() &&
            password.length >= Constants.MIN_CHARS_FOR_PASSWORD

        _isValidEmail.value = isValidEmail
        _isValidPassword.value = isValidPassword

        return isValidEmail && isValidPassword
    }

    fun onEmailTextChanged(email: String?) {
        emailLiveData.value = email
    }

    fun onPasswordTextChanges(password: String?) {
        passwordLiveData.value = password
    }

    private val _apiCallInProgress = MutableLiveData(false)
    val apiCallInProgress: LiveData<Boolean> = _apiCallInProgress

    private val loginResultLiveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun getLoginResultLiveData(): LiveData<Boolean> {
        return loginResultLiveData
    }

    fun onLoginButtonClicked(email: String, password: String, sessionManager: SessionManager) {
        _apiCallInProgress.value = true
        val loginRequest = LoginRequest(
            email = email,
            password = password
        )

        ApiModule.retrofit.login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    UserInfo.id = response.body()?.user?.id ?: "0"
                    UserInfo.email = response.body()?.user?.email ?: "username@gmail.com"
                    UserInfo.imageUrl = response.body()?.user?.imageUrl

                    val token = response.headers()[Constants.ACCESS_TOKEN].toString()
                    val client = response.headers()[Constants.CLIENT].toString()
                    val expiry = response.headers()[Constants.EXPIRY].toString()
                    val uid = response.headers()[Constants.UID].toString()
                    val contentType = response.headers()[Constants.CONTENT_TYPE].toString()

                    Session.accessToken = token
                    Session.client = client
                    Session.expiry = expiry
                    Session.uid = uid
                    Session.contentType = contentType

                    sessionManager.saveSession(token, client, expiry, uid, contentType)
                }
                loginResultLiveData.value = response.isSuccessful
                _apiCallInProgress.value = false
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                loginResultLiveData.value = false
                _apiCallInProgress.value = false
            }

        })
    }
}
