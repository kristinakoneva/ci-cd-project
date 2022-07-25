package infinumacademy.showsapp.kristinakoneva

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import infinumacademy.showsapp.kristinakoneva.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null

    private val binding get() = _binding!!

    private val emailLiveData = MutableLiveData<String>()
    private val passwordLiveData = MutableLiveData<String>()
    private val isValidLiveData = MediatorLiveData<Boolean>().apply {
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

    companion object {
        const val MIN_CHARS_FOR_PASSWORD = 6
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeLiveDataForValidation()
        initListeners()

    }

    private fun observeLiveDataForValidation() {
        binding.tilEmail.editText?.doOnTextChanged { text, _, _, _ ->
            emailLiveData.value = text?.toString()
        }

        binding.tilPassword.editText?.doOnTextChanged { text, _, _, _ ->
            passwordLiveData.value = text?.toString()
        }

        isValidLiveData.observe(viewLifecycleOwner) { isValid ->
            binding.btnLogin.isEnabled = isValid
        }
    }

    private fun initListeners() {
        binding.btnLogin.setOnClickListener {
            val username = extractUsername()
            val directions = LoginFragmentDirections.toShowsNavGraph(username)
            findNavController().navigate(directions)
        }
    }

    private fun extractUsername(): String {
        val email = binding.etEmail.text.toString()
        val parts = email.split("@")
        val username = parts[0]
        return username
    }

    private fun validateLoginForm(email: String?, password: String?): Boolean {
        val isValidEmail = email != null && email.isNotBlank() && email.matches("^[a-z][a-z0-9\\.\\_]*@[a-z]+\\.[a-z]+".toRegex())
        val isValidPassword = password != null && password.isNotBlank() && password.length >= MIN_CHARS_FOR_PASSWORD

        setEmailError(isValidEmail)
        setPasswordError(isValidPassword)

        return isValidEmail && isValidPassword
    }

    private fun setEmailError(isValidEmail: Boolean) {
        if (!isValidEmail) {
            binding.etEmail.error = getString(R.string.invalid_email_error_message)
        }
    }

    private fun setPasswordError(isValidPassword: Boolean) {
        if (!isValidPassword) {
            binding.etPassword.error = getString(R.string.invalid_password_error_message)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}