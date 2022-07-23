package infinumacademy.showsapp.kristinakoneva

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import infinumacademy.showsapp.kristinakoneva.databinding.DialogAddReviewBinding
import infinumacademy.showsapp.kristinakoneva.databinding.FragmentShowDetailsBinding

class ShowDetailsFragment : Fragment() {

    private var _binding: FragmentShowDetailsBinding? = null

    private val binding get() = _binding!!

    private lateinit var adapter: ReviewsAdapter

    private val args by navArgs<ShowDetailsFragmentArgs>()

    private val viewModel by viewModels<ShowDetailsViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentShowDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        showReviews()
        displayShow()
        initBackButtonFromToolbar()
        initReviewsRecycler()
        initAddReviewButton()
        setReviewsStatus()
    }

    private fun setReviewsStatus() {
        val numOfReviews =  viewModel.reviewsListLiveData.value?.size
        val averageRating = viewModel.getAverageReviewsRating()
        binding.ratingStatus.rating = String.format("%.2f", averageRating.toFloat()).toFloat()
        binding.reviewsStatus.text = getString(R.string.review_status, numOfReviews, averageRating.toFloat())
    }

    private fun initBackButtonFromToolbar() {
        binding.showDetailsToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun displayShow() {
        viewModel.displayShow(binding,args.show)
    }

    private fun populateRecyclerView(){
        viewModel.reviewsListLiveData.observe(viewLifecycleOwner){reviewsList->
            adapter.addAllItems(reviewsList)
        }
    }

    private fun initReviewsRecycler() {
        adapter = ReviewsAdapter(listOf())

        populateRecyclerView()

        binding.reviewsRecycler.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL, false
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

        setReviewsStatus()
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
            addReviewToList(bottomSheetBinding.rbRating.rating.toDouble(), bottomSheetBinding.etComment.text.toString())
            showReviews()
            setReviewsStatus()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showReviews() {
        viewModel.showReviews(binding)
    }

    private fun addReviewToList(rating: Double, comment: String) {
        val username = args.username
        viewModel.addReviewToList(rating,comment,username)
        populateRecyclerView()
        setReviewsStatus()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

