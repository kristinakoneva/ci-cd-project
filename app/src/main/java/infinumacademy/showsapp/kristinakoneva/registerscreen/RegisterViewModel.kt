package infinumacademy.showsapp.kristinakoneva.registerscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import infinumacademy.showsapp.kristinakoneva.Constants
import infinumacademy.showsapp.kristinakoneva.model.RegisterRequest
import infinumacademy.showsapp.kristinakoneva.model.RegisterResponse
import infinumacademy.showsapp.kristinakoneva.networking.ApiModule
import infinumacademy.showsapp.kristinakoneva.networking.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel : ViewModel() {

    private val emailLiveData = MutableLiveData<String?>()
    private val passwordLiveData = MutableLiveData<String?>()
    private val repeatPasswordLiveData = MutableLiveData<String?>()
    private val _isValidLiveData = MediatorLiveData<Boolean>().apply {
        this.value = false

        addSource(emailLiveData) { email ->
            val password = passwordLiveData.value
            val repeatPassword = repeatPasswordLiveData.value
            this.value = validateLoginForm(email, password, repeatPassword)
        }
        addSource(passwordLiveData) { password ->
            val email = emailLiveData.value
            val repeatPassword = repeatPasswordLiveData.value
            this.value = validateLoginForm(email, password, repeatPassword)
        }
        addSource(repeatPasswordLiveData) { repeatPassword ->
            val email = emailLiveData.value
            val password = passwordLiveData.value
            this.value = validateLoginForm(email, password, repeatPassword)
        }
    }
    val isValidLiveData: LiveData<Boolean> = _isValidLiveData

    private val _isValidEmail = MutableLiveData(false)
    val isValidEmail: LiveData<Boolean> = _isValidEmail

    private val _isValidPassword = MutableLiveData(false)
    val isValidPassword: LiveData<Boolean> = _isValidPassword

    private val _isValidRepeatPassword = MutableLiveData(false)
    val isValidRepeatPassword: LiveData<Boolean> = _isValidRepeatPassword

    private fun validateLoginForm(email: String?, password: String?, repeatPassword: String?): Boolean {
        val isValidEmail = email != null && email.isNotBlank() && email.matches(Constants.EMAIL_REGEX.toRegex())
        val isValidPassword = password != null && password.isNotBlank() &&
            password.length >= Constants.MIN_CHARS_FOR_PASSWORD
        val isValidRepeatPassword = password.equals(repeatPassword)

        _isValidEmail.value = isValidEmail
        _isValidPassword.value = isValidPassword
        _isValidRepeatPassword.value = isValidRepeatPassword

        return isValidEmail && isValidPassword && isValidRepeatPassword
    }

    fun onEmailTextChanged(email: String?) {
        emailLiveData.value = email
    }

    fun onPasswordTextChanges(password: String?) {
        passwordLiveData.value = password
    }

    fun onRepeatPasswordTextChanges(repeatPassword: String?) {
        repeatPasswordLiveData.value = repeatPassword
    }

    private val registrationResultLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    private val _apiCallInProgress = MutableLiveData(false)
    val apiCallInProgress: LiveData<Boolean> = _apiCallInProgress

    fun getRegistrationResultLiveData(): LiveData<Boolean> {
        return registrationResultLiveData
    }

    fun onRegisterButtonClicked(email: String, password: String, sessionManager: SessionManager) {
        _apiCallInProgress.value = true
        val registerRequest = RegisterRequest(
            email = email,
            password = password,
            passwordConfirmation = password
        )
        ApiModule.retrofit.register(registerRequest)
            .enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                    registrationResultLiveData.value = response.isSuccessful
                    _apiCallInProgress.value = false
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    _apiCallInProgress.value = false
                    registrationResultLiveData.value = false
                }
            })


    }
}