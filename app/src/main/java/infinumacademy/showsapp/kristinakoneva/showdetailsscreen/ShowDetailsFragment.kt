package infinumacademy.showsapp.kristinakoneva.showdetailsscreen

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialog
import infinumacademy.showsapp.kristinakoneva.NetworkLiveData
import infinumacademy.showsapp.kristinakoneva.R
import infinumacademy.showsapp.kristinakoneva.UserInfo
import infinumacademy.showsapp.kristinakoneva.databinding.DialogAddReviewBinding
import infinumacademy.showsapp.kristinakoneva.databinding.FragmentShowDetailsBinding
import infinumacademy.showsapp.kristinakoneva.showsscreen.showsApp

@Suppress("TooManyFunctions")
class ShowDetailsFragment : Fragment() {

    companion object{
        private const val REVIEW_RATING_FORMAT = "%.2f"
    }

    private var _binding: FragmentShowDetailsBinding? = null

    private val binding get() = _binding!!

    private lateinit var adapter: ReviewsAdapter

    private val args by navArgs<ShowDetailsFragmentArgs>()

    private val viewModel: ShowDetailsViewModel by viewModels {
        ShowDetailsViewModelFactory(showsApp.database, args.showId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentShowDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        NetworkLiveData.observe(viewLifecycleOwner) { isOnline ->
            if (isOnline) {
                //connected
                viewModel.fetchShow()
                viewModel.fetchReviewsAboutShow()

            } else {
                //connection gone
                viewModel.fetchShowFromDatabase()
                viewModel.fetchReviewsFromDatabase()
            }
        }

        if (!(NetworkLiveData.isNetworkAvailable())) {
            viewModel.fetchShowFromDatabase()
            viewModel.fetchReviewsFromDatabase()
        }

        displayLoadingScreen()
        displayShow()
        initBackButtonFromToolbar()
        initReviewsRecycler()
        initAddReviewButton()
    }

    private fun displayLoadingScreen() {
        viewModel.apiCallInProgress.observe(viewLifecycleOwner) { isApiInProgress ->
            binding.loadingProgressOverlayContainer.loadingProgressOverlay.isVisible = isApiInProgress
        }
    }

    private fun displayShow() {
        viewModel.getShowResultLiveData.observe(viewLifecycleOwner) { isSuccessful ->
            if (isSuccessful) {
                viewModel.showLiveData.observe(viewLifecycleOwner) { show ->
                    binding.showName.text = show.title
                    binding.showDesc.text = show.description
                    binding.showImg.load(show.imageUrl) {
                        placeholder(R.drawable.show_image_placeholder)
                        error(R.drawable.show_image_placeholder)
                        fallback(R.drawable.show_image_placeholder)
                    }
                    setReviewsStatus()
                    showReviews()
                }
            } else {
                Toast.makeText(requireContext(), getString(R.string.error_fetching_show_msg), Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun showReviews() {
        viewModel.reviewsListLiveData.observe(viewLifecycleOwner) { reviews ->
            binding.reviewsRecycler.isVisible = !reviews.isNullOrEmpty()
            binding.noReviews.isVisible = reviews.isNullOrEmpty()
        }
    }

    private fun setReviewsStatus() {
        viewModel.getShowResultLiveData.observe(viewLifecycleOwner) { isSuccessful ->
            if (isSuccessful) {
                viewModel.showLiveData.observe(viewLifecycleOwner) { show ->
                    val numOfReviews = show.noOfReviews
                    val averageRating = show.averageRating
                    binding.ratingStatus.rating = String.format(REVIEW_RATING_FORMAT, averageRating).toFloat()
                    binding.reviewsStatus.text = getString(R.string.review_status, numOfReviews, averageRating)
                }
            } else {
                Toast.makeText(requireContext(), getString(R.string.error_fetching_show_msg), Toast.LENGTH_SHORT).show()
            }
        }

    }

    /*
    private fun setReviewsStatus() {
        val numOfReviews = viewModel.reviewsListLiveData.value?.size
        val averageRating = viewModel.getAverageReviewsRating().toFloat()
        viewModel.reviewsListLiveData.observe(viewLifecycleOwner){ reviewsList->
            if(reviewsList.isNotEmpty()){
                binding.ratingStatus.rating = String.format("%.2f", averageRating).toFloat()
                binding.reviewsStatus.text = getString(R.string.review_status, numOfReviews, averageRating)
            }
        }
    }*/

    private fun initBackButtonFromToolbar() {
        binding.showDetailsToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun populateRecyclerView() {
        viewModel.fetchReviewsResultLiveData.observe(viewLifecycleOwner) { isSuccessful ->
            if (isSuccessful) {
                viewModel.reviewsListLiveData.observe(viewLifecycleOwner) { reviewsList ->
                    adapter.addAllItems(reviewsList)
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_fetching_reviews_msg),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        viewModel.reviewsListLiveData.observe(viewLifecycleOwner) { reviewsList ->
            adapter.addAllItems(reviewsList)
        }
    }

    private fun initReviewsRecycler() {
        adapter = ReviewsAdapter(listOf())

        populateRecyclerView()

        binding.reviewsRecycler.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )

        binding.reviewsRecycler.adapter = adapter

        binding.reviewsRecycler.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )
    }

    private fun initAddReviewButton() {
        binding.btnWriteReview.setOnClickListener {
            showAddReviewBottomSheet()
        }
    }

    private fun showAddReviewBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val bottomSheetBinding = DialogAddReviewBinding.inflate(layoutInflater)
        dialog.setContentView(bottomSheetBinding.root)

        bottomSheetBinding.rbRating.setOnRatingBarChangeListener { _, _, _ ->
            bottomSheetBinding.btnSubmitReview.isEnabled = true
        }
        bottomSheetBinding.btnCloseDialog.setOnClickListener {
            dialog.dismiss()
        }

        bottomSheetBinding.btnSubmitReview.setOnClickListener {
            val rating = bottomSheetBinding.rbRating.rating.toInt()
            val comment = bottomSheetBinding.etComment.text.toString()
            if (NetworkLiveData.isNetworkAvailable()) {
                viewModel.addReview(rating, comment)
                viewModel.fetchShow()
                viewModel.fetchReviewsAboutShow()
            } else {
                var userId = UserInfo.id
                if (userId == null) {
                    userId = "123"
                }
                var userEmail = UserInfo.email
                if (userEmail == null) {
                    userEmail = getString(R.string.example_email)
                }
                val userImageUrl = UserInfo.imageUrl
                viewModel.addReviewToDatabase(rating, comment, userId, userEmail, userImageUrl)
                viewModel.fetchShowFromDatabase()
                viewModel.fetchReviewsFromDatabase()
            }
            populateRecyclerView()
            displayShow()
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
