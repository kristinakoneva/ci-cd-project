package infinumacademy.showsapp.kristinakoneva

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import model.CreateReviewRequest
import model.CreateReviewResponse
import model.DisplayShowResponse
import model.Review
import model.ReviewsResponse
import model.Show
import networking.ApiModule
import retrofit2.Callback
import retrofit2.Response

class ShowDetailsViewModel : ViewModel() {

    private val _reviewsListLiveData = MutableLiveData<List<Review>>(listOf())
    val reviewsListLiveData: LiveData<List<Review>> = _reviewsListLiveData

    private val _showLiveData = MutableLiveData<Show>()
    val showLiveData: LiveData<Show> = _showLiveData

    private val _apiCallInProgress = MutableLiveData(false)
    val apiCallInProgress: LiveData<Boolean> = _apiCallInProgress

    fun getAverageReviewsRating(): Double {
        return if (_reviewsListLiveData.value != null) {
            var total = 0.0
            for (review in _reviewsListLiveData.value!!) {
                total += review.rating
            }
            total / _reviewsListLiveData.value!!.size.toDouble()
        } else {
            0.0
        }
    }

    fun getShow(showId: Int) {
        _apiCallInProgress.value = true
        ApiModule.retrofit.displayShow(showId).enqueue(object : Callback<DisplayShowResponse> {
            override fun onResponse(call: retrofit2.Call<DisplayShowResponse>, response: Response<DisplayShowResponse>) {
                if (response.isSuccessful) {
                    _showLiveData.value = response.body()!!.show
                    _apiCallInProgress.value = false
                }
            }

            override fun onFailure(call: retrofit2.Call<DisplayShowResponse>, t: Throwable) {
                // TODO("Not yet implemented")
                _apiCallInProgress.value = false
            }

        })
    }

    fun addReview(rating: Int, comment: String?, showId: Int) {
        _apiCallInProgress.value = true
        val request = CreateReviewRequest(
            rating = rating,
            comment = comment,
            showId = showId
        )
        ApiModule.retrofit.createReview(request).enqueue(object : Callback<CreateReviewResponse> {
            override fun onResponse(call: retrofit2.Call<CreateReviewResponse>, response: Response<CreateReviewResponse>) {
                if (response.isSuccessful) {
                    _reviewsListLiveData.value = _reviewsListLiveData.value!! + response.body()!!.review
                }
                _apiCallInProgress.value = false
            }

            override fun onFailure(call: retrofit2.Call<CreateReviewResponse>, t: Throwable) {
                // TODO("Not yet implemented")
                _apiCallInProgress.value = false
            }

        })
    }

    fun fetchReviewsAboutShow(showId: Int) {
        _apiCallInProgress.value = true
        ApiModule.retrofit.fetchReviewsAboutShow(showId).enqueue(object : Callback<ReviewsResponse> {
            override fun onResponse(call: retrofit2.Call<ReviewsResponse>, response: Response<ReviewsResponse>) {
                if (response.isSuccessful) {
                    _reviewsListLiveData.value = response.body()!!.reviews
                }
                _apiCallInProgress.value = false
            }

            override fun onFailure(call: retrofit2.Call<ReviewsResponse>, t: Throwable) {
                // TODO("Not yet implemented")
                _apiCallInProgress.value = false
            }

        })
    }


}