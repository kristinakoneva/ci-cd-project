package infinumacademy.showsapp.kristinakoneva.showdetailsscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import db.ReviewEntity
import db.ShowsAppDatabase
import java.util.concurrent.Executors
import infinumacademy.showsapp.kristinakoneva.model.CreateReviewRequest
import infinumacademy.showsapp.kristinakoneva.model.CreateReviewResponse
import infinumacademy.showsapp.kristinakoneva.model.DisplayShowResponse
import infinumacademy.showsapp.kristinakoneva.model.Review
import infinumacademy.showsapp.kristinakoneva.model.ReviewsResponse
import infinumacademy.showsapp.kristinakoneva.model.Show
import infinumacademy.showsapp.kristinakoneva.model.User
import infinumacademy.showsapp.kristinakoneva.networking.ApiModule
import retrofit2.Callback
import retrofit2.Response

class ShowDetailsViewModel(
    private val database: ShowsAppDatabase,
    private val showId: Int
) : ViewModel() {

    private val _reviewsListLiveData = MutableLiveData<List<Review>>(listOf())
    var reviewsListLiveData: LiveData<List<Review>> = _reviewsListLiveData

    private val _showLiveData = MutableLiveData<Show>()
    var showLiveData: LiveData<Show> = _showLiveData

    private val _apiCallForFetchingShowInProgress = MutableLiveData(false)
    // val apiCallForFetchingShowInProgress: LiveData<Boolean> = _apiCallForFetchingShowInProgress

    private val _apiCallForFetchingReviewsInProgress = MutableLiveData(false)
    // val apiCallForFetchingReviewsInProgress: LiveData<Boolean> = _apiCallForFetchingReviewsInProgress

    private val _apiCallForCreatingReviewInProgress = MutableLiveData(false)
    // val apiCallForCreatingReviewInProgress: LiveData<Boolean> = _apiCallForCreatingReviewInProgress

    private val _apiCallInProgress = MutableLiveData(false)
    val apiCallInProgress: LiveData<Boolean> = _apiCallInProgress

    private val _getShowResultLiveData = MutableLiveData(true)
    val getShowResultLiveData: LiveData<Boolean> = _getShowResultLiveData

    private val _fetchReviewsResultLiveData = MutableLiveData(true)
    val fetchReviewsResultLiveData: LiveData<Boolean> = _fetchReviewsResultLiveData

    fun fetchShow() {
        _apiCallForFetchingShowInProgress.postValue(true)
        _apiCallInProgress.postValue(true)
        ApiModule.retrofit.displayShow(showId).enqueue(object : Callback<DisplayShowResponse> {
            override fun onResponse(
                call: retrofit2.Call<DisplayShowResponse>,
                response: Response<DisplayShowResponse>
            ) {
                _getShowResultLiveData.value = response.isSuccessful
                if (response.isSuccessful) {
                    _showLiveData.value = response.body()?.show
                }
                _apiCallForFetchingShowInProgress.value = false
                _apiCallInProgress.value =
                    _apiCallForFetchingReviewsInProgress.value!! || _apiCallForCreatingReviewInProgress.value!!
            }

            override fun onFailure(call: retrofit2.Call<DisplayShowResponse>, t: Throwable) {
                _getShowResultLiveData.value = false
                _apiCallForFetchingShowInProgress.value = false
                _apiCallInProgress.value =
                    _apiCallForFetchingReviewsInProgress.value!! || _apiCallForCreatingReviewInProgress.value!!
            }

        })
    }

    fun addReview(rating: Int, comment: String?) {
        _apiCallForCreatingReviewInProgress.value = true
        _apiCallInProgress.value = true
        val request = CreateReviewRequest(
            rating = rating,
            comment = comment,
            showId = showId
        )
        ApiModule.retrofit.createReview(request).enqueue(object : Callback<CreateReviewResponse> {
            override fun onResponse(
                call: retrofit2.Call<CreateReviewResponse>,
                response: Response<CreateReviewResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.review?.let { review ->
                        _reviewsListLiveData.value = _reviewsListLiveData.value?.plus(review)
                    }
                }
                _apiCallForCreatingReviewInProgress.value = false
                _apiCallInProgress.value =
                    _apiCallForFetchingShowInProgress.value!! || _apiCallForFetchingReviewsInProgress.value!!
            }

            override fun onFailure(call: retrofit2.Call<CreateReviewResponse>, t: Throwable) {
                _apiCallForCreatingReviewInProgress.value = false
                _apiCallInProgress.value =
                    _apiCallForFetchingShowInProgress.value!! || _apiCallForFetchingReviewsInProgress.value!!
            }

        })
    }

    fun fetchReviewsAboutShow() {
        _apiCallForFetchingReviewsInProgress.postValue(true)
        _apiCallInProgress.postValue(true)
        ApiModule.retrofit.fetchReviewsAboutShow(showId).enqueue(object : Callback<ReviewsResponse> {
            override fun onResponse(
                call: retrofit2.Call<ReviewsResponse>,
                response: Response<ReviewsResponse>
            ) {
                _fetchReviewsResultLiveData.value = response.isSuccessful
                if (response.isSuccessful) {
                    _reviewsListLiveData.value = response.body()?.reviews
                    saveReviewsToDatabase(response.body()?.reviews)
                }
                _apiCallForFetchingReviewsInProgress.value = false
                _apiCallInProgress.value =
                    _apiCallForFetchingShowInProgress.value!! || _apiCallForCreatingReviewInProgress.value!!
            }

            override fun onFailure(call: retrofit2.Call<ReviewsResponse>, t: Throwable) {
                _fetchReviewsResultLiveData.value = false
                _apiCallForFetchingReviewsInProgress.value = false
                _apiCallInProgress.value =
                    _apiCallForFetchingShowInProgress.value!! || _apiCallForCreatingReviewInProgress.value!!
            }

        })
    }

    private fun saveReviewsToDatabase(reviews: List<Review>?) {
        Executors.newSingleThreadExecutor().execute {
            database.reviewDao().insertAllReviews(reviews?.map { review ->
                ReviewEntity(
                    review.id.toInt(),
                    review.comment,
                    review.rating,
                    review.showId,
                    review.user.id,
                    review.user.email,
                    review.user.imageUrl
                )
            } ?: listOf())
        }
    }

    fun fetchShowFromDatabase() {
        showLiveData = database.showDao().getShow(showId.toString()).map { showEntity ->
            Show(
                showEntity.id,
                showEntity.averageRating,
                showEntity.description,
                showEntity.imageUrl,
                showEntity.noOfReviews,
                showEntity.title
            )
        }
    }

    fun fetchReviewsFromDatabase() {
        reviewsListLiveData = database.reviewDao().getAllReviews(showId).map { list ->
            list.map { reviewEntity ->
                Review(
                    reviewEntity.id.toString(),
                    reviewEntity.comment,
                    reviewEntity.rating,
                    reviewEntity.showId,
                    User(reviewEntity.userId, reviewEntity.userEmail, reviewEntity.userImageUrl)
                )
            }
        }
    }

    fun addReviewToDatabase(
        rating: Int,
        comment: String?,
        userId: String,
        userEmail: String,
        userImageUrl: String?
    ) {
        Executors.newSingleThreadExecutor().execute {
            database.reviewDao().insertAllReviews(
                listOf(
                    ReviewEntity(
                        0,
                        comment,
                        rating,
                        showId,
                        userId,
                        userEmail,
                        userImageUrl
                    )
                )
            )
        }
    }
}
