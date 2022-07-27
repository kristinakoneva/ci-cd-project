package infinumacademy.showsapp.kristinakoneva

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import model.LoginRequest
import model.LoginResponse
import networking.ApiModule
import networking.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.lifecycle.MediatorLiveData


class LoginViewModel: ViewModel() {

    /*

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

    private val _isValidEmail = MutableLiveData<Boolean>(false)
    val isValidEmail: LiveData<Boolean> = _isValidEmail

    private val _isValidPassword = MutableLiveData<Boolean>(false)
    val isValidPassword: LiveData<Boolean> = _isValidPassword

    private fun validateLoginForm(email: String?, password: String?): Boolean {
        val isValidEmail = email != null && email.isNotBlank() && email.matches("^[a-z][a-z0-9\\.\\_]*@[a-z]+\\.[a-z]+".toRegex())
        val isValidPassword = password != null && password.isNotBlank() && password.length >= Constants.MIN_CHARS_FOR_PASSWORD

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


     */
    private val loginResultLiveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun getLoginResultLiveData(): LiveData<Boolean>{
        return loginResultLiveData
    }

    fun onLoginButtonClicked(email: String, password: String, sessionManager: SessionManager){
        val loginRequest = LoginRequest(
            email = email,
            password = password
        )
        ApiModule.retrofit.login(loginRequest).enqueue(object: Callback<LoginResponse>{
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                loginResultLiveData.value = response.isSuccessful

                val token = response.headers()["access-token"].toString()
                if(response.isSuccessful){
                    sessionManager.saveAuthToken(token)
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                loginResultLiveData.value = false
            }

        })
    }
}