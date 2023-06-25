package infinumacademy.showsapp.kristinakoneva.showsscreen

import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.bottomsheet.BottomSheetDialog
import infinumacademy.showsapp.kristinakoneva.BuildConfig
import infinumacademy.showsapp.kristinakoneva.Constants
import infinumacademy.showsapp.kristinakoneva.NetworkLiveData
import infinumacademy.showsapp.kristinakoneva.R
import infinumacademy.showsapp.kristinakoneva.ShowsApplication
import infinumacademy.showsapp.kristinakoneva.UserInfo
import infinumacademy.showsapp.kristinakoneva.databinding.DialogChangeProfilePhotoOrLogoutBinding
import infinumacademy.showsapp.kristinakoneva.databinding.DialogChooseChangingPofilePhotoMethodBinding
import infinumacademy.showsapp.kristinakoneva.databinding.FragmentShowsBinding
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import infinumacademy.showsapp.kristinakoneva.model.Show
import infinumacademy.showsapp.kristinakoneva.networking.SessionManager

val Fragment.showsApp: ShowsApplication
    get() {
        return requireActivity().application as ShowsApplication
    }

@Suppress("TooManyFunctions")
class ShowsFragment : Fragment() {

    companion object {
        private const val BITMAP_COMPRESS_QUALITY = 50
    }

    private var _binding: FragmentShowsBinding? = null

    private val binding get() = _binding!!

    private lateinit var adapter: ShowsAdapter

    private val viewModel: ShowsViewModel by viewModels {
        ShowsViewModelFactory(showsApp.database)
    }

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(requireContext())
        sharedPreferences = requireContext().getSharedPreferences(Constants.SHOWS_APP, Context.MODE_PRIVATE)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentShowsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        NetworkLiveData.observe(viewLifecycleOwner) { isOnline ->
            if (isOnline) {
                viewModel.fetchShows()
                viewModel.fetchTopRatedShows()
            } else {
                viewModel.fetchShowsFromDatabase()
            }
        }
        if (!(NetworkLiveData.isNetworkAvailable())) {
            viewModel.fetchShowsFromDatabase()
        }

        displayLoadingScreen()
        showProfilePhoto()
        initShowsRecycler()
        displayState()
        initListeners()
    }

    private fun displayLoadingScreen() {
        viewModel.apiCallInProgress.observe(viewLifecycleOwner) { isApiInProgress ->
            binding.loadingProgressOverlayContainer.loadingProgressOverlay.isVisible = isApiInProgress
        }
    }

    private fun displayState() {
        if (NetworkLiveData.isNetworkAvailable()) {
            showShows()
        } else {
            viewModel.getShowsFromDB().observe(viewLifecycleOwner) { list ->
                if (list.isNullOrEmpty())
                    hideShows()
                else
                    showShows()
            }
        }
    }

    private fun initListeners() {
        binding.toolbar.setClickListener {
            openDialogForChangingProfilePicOrLoggingOut()
        }

        binding.chipTopRated.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateShowTopRated(isChecked)
        }
    }

    private fun bindDataToTheBottomSheetDialog(
        bottomSheetBinding: DialogChangeProfilePhotoOrLogoutBinding,
        email: String?,
        profilePhotoUrl: String?
    ) {
        bottomSheetBinding.profilePhoto.load(profilePhotoUrl) {
            transformations(CircleCropTransformation())
            placeholder(R.drawable.ic_profile_placeholder)
            error(R.drawable.ic_profile_placeholder)
            fallback(R.drawable.ic_profile_placeholder)
        }
        bottomSheetBinding.emailAddress.text = email
    }

    private fun openDialogForChangingProfilePicOrLoggingOut() {
        val dialog = BottomSheetDialog(requireContext())
        val bottomSheetBinding = DialogChangeProfilePhotoOrLogoutBinding.inflate(layoutInflater)
        dialog.setContentView(bottomSheetBinding.root)

        // User Interface
        var profilePhotoUrl: String? = null
        var email: String? = null
        if (NetworkLiveData.isNetworkAvailable()) {
            viewModel.getLatestUserInfo()
            viewModel.getLatestUserInfoResultLiveData.observe(viewLifecycleOwner) { isSuccessful ->
                if (isSuccessful) {
                    viewModel.currentUser.observe(viewLifecycleOwner) { user ->
                        profilePhotoUrl = user?.imageUrl ?: UserInfo.imageUrl
                        email = user?.email ?: UserInfo.email
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.error_fetching_user_info_msg),
                        Toast.LENGTH_SHORT
                    ).show()
                    profilePhotoUrl = UserInfo.imageUrl
                    email = UserInfo.email
                }

                bindDataToTheBottomSheetDialog(bottomSheetBinding, email, profilePhotoUrl)
            }
        } else {
            profilePhotoUrl = UserInfo.imageUrl
            email = UserInfo.email

            bindDataToTheBottomSheetDialog(bottomSheetBinding, email, profilePhotoUrl)
        }

        // Listeners
        bottomSheetBinding.btnChangeProfilePhoto.setOnClickListener {
            if (NetworkLiveData.isNetworkAvailable()) {
                openDialogForChoosingChangingProfilePhotoMethod()
            } else {
                // prevent changing the profile photo when the user has no internet connection
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_changing_pp_offline_msg),
                    Toast.LENGTH_LONG
                ).show()
            }
            dialog.dismiss()
        }

        bottomSheetBinding.btnLogout.setOnClickListener {
            showAreYouSureAlertDialog(dialog)
        }

        dialog.show()
    }

    private fun openDialogForChoosingChangingProfilePhotoMethod() {
        val dialog = BottomSheetDialog(requireContext())
        val bottomSheetBinding = DialogChooseChangingPofilePhotoMethodBinding.inflate(layoutInflater)
        dialog.setContentView(bottomSheetBinding.root)


        bottomSheetBinding.imgBtnCamera.setOnClickListener {
            takeImage()
            dialog.dismiss()
        }

        bottomSheetBinding.imgBtnGallery.setOnClickListener {
            selectImageFromGallery()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showAreYouSureAlertDialog(bottomSheetDialog: BottomSheetDialog) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.logout))
        builder.setMessage(getString(R.string.logging_out_alert_message))

        builder.setPositiveButton(getString(R.string.logout)) { dialog, _ ->
            sharedPreferences.edit {
                putBoolean(Constants.REMEMBER_ME, false)
                putString(Constants.EMAIL, null)
                putString(Constants.USER_ID, null)
                putString(Constants.IMAGE_URL, null)
            }
            sessionManager.clearSession()
            dialog.dismiss()
            bottomSheetDialog.dismiss()
            findNavController().navigate(R.id.toLoginFragment)
        }

        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun showShows() {
        viewModel.showTopRatedLiveData.observe(viewLifecycleOwner) { showTopRatedShows ->
            if (showTopRatedShows) {
                viewModel.listTopRatedShowsResultLiveData.observe(viewLifecycleOwner) { isSuccessful ->
                    if (isSuccessful) {
                        viewModel.topRatedShowsListLiveData.observe(viewLifecycleOwner) { topRatedShows ->
                            adapter.addAllItems(topRatedShows)
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.error_fetching_top_rated_shows_msg),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }

            } else {
                viewModel.listShowsResultLiveData.observe(viewLifecycleOwner) { isSuccessful ->
                    if (isSuccessful) {
                        viewModel.showsListLiveData.observe(viewLifecycleOwner) { showsList ->
                            showsList?.let {
                                adapter.addAllItems(showsList)
                            }

                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.error_fetching_shows_msg),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        binding.showsEmptyState.isVisible = false
        binding.showsRecycler.isVisible = true
    }

    private fun hideShows() {
        adapter.addAllItems(emptyList())
        binding.showsEmptyState.isVisible = true
        binding.showsRecycler.isVisible = false
    }

    private fun initShowsRecycler() {
        adapter = ShowsAdapter(emptyList()) { show ->
            showDetailsAbout(show)
        }

        binding.showsRecycler.layoutManager = LinearLayoutManager(requireContext())

        binding.showsRecycler.adapter = adapter
    }

    private fun showDetailsAbout(show: Show) {
        if (!NetworkLiveData.isNetworkAvailable()) {
            viewModel.getShowsFromDB().observe(viewLifecycleOwner) { list ->
                // prevent the user from entering ShowDetails screen
                // if there is no internet connection and the database is empty
                if (list.isNullOrEmpty()) {
                    displayState()
                } else {
                    val directions = ShowsFragmentDirections.toShowDetailsFragment(show.id.toInt())
                    findNavController().navigate(directions)
                }
            }
        } else {
            val directions = ShowsFragmentDirections.toShowDetailsFragment(show.id.toInt())
            findNavController().navigate(directions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun saveToInternalStorage(bitmap: Bitmap, emailAsFileName: String): String? {

        val wrapper = ContextWrapper(requireContext().applicationContext)
        var file = wrapper.getDir(getString(R.string.images), Context.MODE_PRIVATE)
        file = File(file, "$emailAsFileName.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, BITMAP_COMPRESS_QUALITY, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file.absolutePath
    }

    //    private fun loadImageFromStorage(path: String) {
    //        try {
    //            val f = File(path)
    //            val b = BitmapFactory.decodeStream(FileInputStream(f))
    //            binding.btnDialogChangeProfilePicOrLogout.load(b) {
    //                transformations(CircleCropTransformation())
    //            }
    //        } catch (e: FileNotFoundException) {
    //            e.printStackTrace()
    //        }
    //    }

    private fun showProfilePhoto() {
        var profilePhotoUrl: String? = null
        if (NetworkLiveData.isNetworkAvailable()) {
            viewModel.getLatestUserInfo()
            viewModel.getLatestUserInfoResultLiveData.observe(viewLifecycleOwner) { isSuccessful ->
                if (isSuccessful) {
                    viewModel.currentUser.observe(viewLifecycleOwner) { user ->
                        profilePhotoUrl = user?.imageUrl ?: UserInfo.imageUrl
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.error_fetching_user_info_msg),
                        Toast.LENGTH_SHORT
                    ).show()
                    profilePhotoUrl = UserInfo.imageUrl
                }
                binding.toolbar.setProfilePhoto(profilePhotoUrl)
            }
        } else {
            profilePhotoUrl = UserInfo.imageUrl
            binding.toolbar.setProfilePhoto(profilePhotoUrl)
        }

    }

    private fun saveProfilePhoto(uri: Uri) {
        val bitmap = getBitmapFromURI(requireContext(), uri)
        val email = UserInfo.email
        val ppPath = saveToInternalStorage(bitmap!!, email!!)
        ppPath?.let {
            viewModel.updateProfilePhoto(email, ppPath)
            viewModel.updateProfilePhotoResultLiveData.observe(viewLifecycleOwner) { isSuccessful ->
                if (isSuccessful) {
                    showProfilePhoto()
                    sharedPreferences.edit {
                        putString(Constants.IMAGE_URL, UserInfo.imageUrl)
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.error_changing_profile_photo_msg),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }

    private val takeImageResult = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            latestTmpUri?.let { uri ->
                saveProfilePhoto(uri)
            }
        }
    }

    private val selectImageFromGalleryResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                saveProfilePhoto(uri)
            }
        }

    private var latestTmpUri: Uri? = null

    private fun takeImage() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri
                takeImageResult.launch(uri)
            }
        }
    }

    private fun selectImageFromGallery() = selectImageFromGalleryResult.launch("image/*")

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png").apply {
            createNewFile()
            deleteOnExit()
        }

        return FileProvider.getUriForFile(
            requireActivity().applicationContext,
            "${BuildConfig.APPLICATION_ID}.provider",
            tmpFile
        )
    }

    private fun getBitmapFromURI(context: Context, uri: Uri?): Bitmap? {
        try {
            val input = context.contentResolver.openInputStream(uri!!) ?: return null
            return BitmapFactory.decodeStream(input)
        } catch (e: FileNotFoundException) {
        }
        return null
    }

    /*
    private fun rotateImageIfNecessary(path: String): Bitmap?{
        val bitmap = BitmapFactory.decodeFile(path)
        val file = File(path)
        val exif = ExifInterface(file.absoluteFile.toString())
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val matrix = Matrix()
        when(orientation){
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90F)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180F)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270F)
        }
        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0,0 , bitmap.width, bitmap.height, matrix, true)
        bitmap.recycle()
        return rotatedBitmap
    }*/
}
