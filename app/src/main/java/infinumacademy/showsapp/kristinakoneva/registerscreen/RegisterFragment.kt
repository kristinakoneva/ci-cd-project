package infinumacademy.showsapp.kristinakoneva.registerscreen

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import infinumacademy.showsapp.kristinakoneva.Constants
import infinumacademy.showsapp.kristinakoneva.R
import infinumacademy.showsapp.kristinakoneva.databinding.FragmentRegisterBinding
import infinumacademy.showsapp.kristinakoneva.loginscreen.animateBounceFromTop
import infinumacademy.showsapp.kristinakoneva.loginscreen.animateOvershootPopOut
import infinumacademy.showsapp.kristinakoneva.networking.SessionManager

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null

    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var sessionManager: SessionManager

    private val viewModel by viewModels<RegisterViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(requireContext())

        sharedPreferences = requireContext().getSharedPreferences(Constants.SHOWS_APP, Context.MODE_PRIVATE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.getRegistrationResultLiveData().observe(viewLifecycleOwner) { registrationSuccessful ->
            if (registrationSuccessful) {
                val directions = RegisterFragmentDirections.toLoginFragment(comingFromRegister = true)
                findNavController().navigate(directions)
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.unsuccessful_registration_msg),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.imgPlayBtnTitle.animateBounceFromTop()
        binding.tvShows.animateOvershootPopOut()
        observeLiveDataForValidation()
        displayLoadingScreen()
        initListeners()
    }

    private fun initListeners() {
        binding.btnRegister.setOnClickListener {
            viewModel.onRegisterButtonClicked(
                email = binding.etEmail.text.toString(),
                password = binding.etPassword.text.toString()
            )
        }
    }

    private fun displayLoadingScreen() {
        viewModel.apiCallInProgress.observe(viewLifecycleOwner) { isApiInProgress ->
            binding.loadingProgressOverlayContainer.loadingProgressOverlay.isVisible = isApiInProgress
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

        binding.tilRepeatPassword.editText?.doOnTextChanged { text, _, _, _ ->
            viewModel.onRepeatPasswordTextChanges(text?.toString())
            setRepeatPasswordError()
        }

        viewModel.isValidLiveData.observe(viewLifecycleOwner) { isValid ->
            binding.btnRegister.isEnabled = isValid
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

    private fun setRepeatPasswordError() {
        viewModel.isValidRepeatPassword.observe(viewLifecycleOwner) { isValid ->
            if (!isValid) {
                binding.etRepeatPassword.error = getString(R.string.passwords_do_not_match_error_message)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
