package infinumacademy.showsapp.kristinakoneva.show_details_screen

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
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
import infinumacademy.showsapp.kristinakoneva.Constants
import infinumacademy.showsapp.kristinakoneva.R
import infinumacademy.showsapp.kristinakoneva.ShowsApplication
import infinumacademy.showsapp.kristinakoneva.databinding.DialogAddReviewBinding
import infinumacademy.showsapp.kristinakoneva.databinding.FragmentShowDetailsBinding

class ShowDetailsFragment : Fragment() {

    private var _binding: FragmentShowDetailsBinding? = null

    private val binding get() = _binding!!

    private lateinit var adapter: ReviewsAdapter

    private val args by navArgs<ShowDetailsFragmentArgs>()

    private val viewModel: ShowDetailsViewModel by viewModels{
        ShowDetailsViewModelFactory((requireActivity().application as ShowsApplication).database)
    }

    private lateinit var sharedPreferences: SharedPreferences

    private val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .build()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        // network is available for use
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            viewModel.getShow(args.showId)
            viewModel.fetchReviewsAboutShow(args.showId)
        }

        // network capabilities have changed for the network
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            val unmetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        }

        // lost network connection
        override fun onLost(network: Network) {
            super.onLost(network)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences(Constants.SHOWS_APP, Context.MODE_PRIVATE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentShowDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val connectivityManager = requireContext().getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        // no network connection
        if (connectivityManager.activeNetwork == null) {

        }

        displayLoadingScreen()
        initBackButtonFromToolbar()
        initReviewsRecycler()
        initAddReviewButton()
        displayShow()
    }

    private fun displayLoadingScreen() {
        viewModel.apiCallInProgress.observe(viewLifecycleOwner) { apiInProgress ->
            binding.loadingProgressOverlay.isVisible = apiInProgress
        }
    }

    private fun displayShow() {
        viewModel.getShowResultLiveData.observe(viewLifecycleOwner) { isSuccessful ->
            if (isSuccessful) {
                viewModel.showLiveData.observe(viewLifecycleOwner) { show ->
                    binding.showName.text = show.title
                    binding.showDesc.text = show.description
                    binding.showImg.load(show.imageUrl)
                }
            } else {
                Toast.makeText(requireContext(), getString(R.string.error_fetching_show_msg), Toast.LENGTH_SHORT).show()
            }
        }

        setReviewsStatus()
        showReviews()
    }

    private fun showReviews() {
        viewModel.showLiveData.observe(viewLifecycleOwner) { show ->
            binding.groupShowReviews.isVisible = show.noOfReviews != 0
            binding.noReviews.isVisible = show.noOfReviews == 0
        }
    }

    private fun setReviewsStatus() {
        viewModel.getShowResultLiveData.observe(viewLifecycleOwner) { isSuccessful ->
            if (isSuccessful) {
                viewModel.showLiveData.observe(viewLifecycleOwner) { show ->
                    val numOfReviews = show.noOfReviews
                    val averageRating = show.averageRating
                    binding.ratingStatus.rating = String.format("%.2f", averageRating).toFloat()
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
                Toast.makeText(requireContext(), getString(R.string.error_fetching_reviews_msg), Toast.LENGTH_SHORT).show()
            }
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
            val showId = args.showId
            viewModel.addReview(rating, comment, showId)
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

