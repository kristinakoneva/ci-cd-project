package infinumacademy.showsapp.kristinakoneva.login_screen

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import infinumacademy.showsapp.kristinakoneva.Constants
import infinumacademy.showsapp.kristinakoneva.R
import infinumacademy.showsapp.kristinakoneva.databinding.FragmentLoginBinding
import networking.ApiModule
import networking.SessionManager

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null

    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences

    private val viewModel by viewModels<LoginViewModel>()

    private lateinit var sessionManager: SessionManager

    private val args by navArgs<LoginFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        ApiModule.initRetrofit(requireContext())
        sessionManager = SessionManager(requireContext())
        sharedPreferences = requireContext().getSharedPreferences(Constants.SHOWS_APP, Context.MODE_PRIVATE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getLoginResultLiveData().observe(viewLifecycleOwner) { loginSuccessful ->
            if (loginSuccessful) {
                saveData()
                val directions = LoginFragmentDirections.toShowsNavGraph()
                findNavController().navigate(directions)
            } else {
                Toast.makeText(requireContext(), getString(R.string.unsuccessful_login_msg), Toast.LENGTH_SHORT).show()
            }

        }


        displayLoadingScreen()
        checkRememberMe()
        observeLiveDataForValidation()
        initListeners()
        checkComingFromRegister()
    }

    private fun displayLoadingScreen() {
        viewModel.apiCallInProgress.observe(viewLifecycleOwner) { isApiInProgress ->
            binding.loadingProgressOverlay.isVisible = isApiInProgress
        }
    }

    private fun checkComingFromRegister() {
        if (args.comingFromRegister) {
            binding.btnRegister.isVisible = false
        }
    }

    private fun checkRememberMe() {
        binding.cbRememberMe.isChecked = sharedPreferences.getBoolean(Constants.REMEMBER_ME, false)
        if (binding.cbRememberMe.isChecked) {
            val directions = LoginFragmentDirections.toShowsNavGraph()
            findNavController().navigate(directions)
        } else {
            sessionManager.clearSession()
            sharedPreferences.edit {
                putBoolean(Constants.REMEMBER_ME, false)
                putString(Constants.EMAIL, null)
            }
        }
    }

    private fun saveRememberMe() {
        sharedPreferences.edit {
            putBoolean(Constants.REMEMBER_ME, binding.cbRememberMe.isChecked)
        }
    }

    private fun saveEmail() {
        val email = binding.etEmail.text.toString()
        sharedPreferences.edit {
            putString(Constants.EMAIL, email)
        }
    }

    private fun observeLiveDataForValidation() {
        binding.tilEmail.editText?.doOnTextChanged { text, _, _, _ ->
            viewModel.onEmailTextChanged(text?.toString())
            setEmailError()
        }

        binding.tilPassword.editText?.doOnTextChanged { text, _, _, _ ->
            viewModel.onPasswordTextChanges(text?.toString())
            setPasswordError()
        }

        viewModel.isValidLiveData.observe(viewLifecycleOwner) { isValid ->
            binding.btnLogin.isEnabled = isValid
        }
    }

    private fun saveData() {
        saveRememberMe()
        saveEmail()
    }

    private fun initListeners() {
        binding.btnLogin.setOnClickListener {
            synchronized(this) {
                viewModel.onLoginButtonClicked(
                    email = binding.etEmail.text.toString(),
                    password = binding.etPassword.text.toString(),
                    sessionManager = sessionManager
                )
            }

        }

        binding.btnRegister.setOnClickListener {
            val directions = LoginFragmentDirections.toRegisterFragment()
            findNavController().navigate(directions)
        }
    }

    private fun setEmailError() {
        viewModel.isValidEmail.observe(viewLifecycleOwner) { isValid ->
            if (!isValid) {
                binding.etEmail.error = getString(R.string.invalid_email_error_message)
            }
        }
    }

    private fun setPasswordError() {
        viewModel.isValidPassword.observe(viewLifecycleOwner) { isValid ->
            if (!isValid) {
                binding.etPassword.error = getString(R.string.invalid_password_error_message)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}