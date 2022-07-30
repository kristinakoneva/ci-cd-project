package infinumacademy.showsapp.kristinakoneva.shows_screen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import model.Show
import model.ShowsResponse
import model.TopRatedShowsResponse
import networking.ApiModule
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShowsViewModel : ViewModel() {

    private val _showsListLiveData = MutableLiveData<List<Show>>()
    val showsListLiveData: LiveData<List<Show>> = _showsListLiveData

    private val _showEmptyStateLiveData = MutableLiveData(false)
    val showEmptyStateLiveData: LiveData<Boolean> = _showEmptyStateLiveData

    private val _listShowsResultLiveData = MutableLiveData(true)
    val listShowsResultLiveData: LiveData<Boolean> = _listShowsResultLiveData

    private val _listTopRatedShowsResultLiveData = MutableLiveData(true)
    val listTopRatedShowsResultLiveData: LiveData<Boolean> = _listTopRatedShowsResultLiveData

    private val _topRatedShowsListLiveData = MutableLiveData<List<Show>>()
    val topRatedShowsListLiveData: LiveData<List<Show>> = _topRatedShowsListLiveData

    private val _showTopRatedLiveData = MutableLiveData(false)
    val showTopRatedLiveData: LiveData<Boolean> = _showTopRatedLiveData

    private val _apiCallInProgress = MutableLiveData(false)
    val apiCallInProgress: LiveData<Boolean> = _apiCallInProgress

    private val _apiCallForFetchingShowsInProgress = MutableLiveData(false)
    // val apiCallForFetchingShowsInProgress: LiveData<Boolean> = _apiCallForFetchingShowsInProgress

    private val _apiCallForFetchingTopRatedShowsInProgress = MutableLiveData(false)
    // val apiCallForFetchingTopRatedShowsInProgress: LiveData<Boolean> = _apiCallForFetchingTopRatedShowsInProgress

    init {
        fetchShows()
        fetchTopRatedShows()
    }

    fun updateShowTopRated(isChecked: Boolean) {
        _showTopRatedLiveData.value = isChecked
    }

    private fun fetchShows() {
        _apiCallInProgress.value = true
        _apiCallForFetchingShowsInProgress.value = true
        ApiModule.retrofit.fetchShows().enqueue(object : Callback<ShowsResponse> {
            override fun onResponse(call: Call<ShowsResponse>, response: Response<ShowsResponse>) {
                _listShowsResultLiveData.value = response.isSuccessful
                if (response.isSuccessful) {
                    _showsListLiveData.value = response.body()?.shows
                }
                _apiCallForFetchingShowsInProgress.value = false
                _apiCallInProgress.value = _apiCallForFetchingTopRatedShowsInProgress.value
            }

            override fun onFailure(call: Call<ShowsResponse>, t: Throwable) {
                _listShowsResultLiveData.value = false
                _apiCallForFetchingShowsInProgress.value = false
                _apiCallInProgress.value = _apiCallForFetchingTopRatedShowsInProgress.value
            }

        })
    }

    fun resetEmptyState() {
        _showEmptyStateLiveData.value = !_showEmptyStateLiveData.value!!
    }

    private fun fetchTopRatedShows() {
        _apiCallInProgress.value = true
        _apiCallForFetchingTopRatedShowsInProgress.value = true
        ApiModule.retrofit.fetchTopRatedShows().enqueue(object : Callback<TopRatedShowsResponse> {
            override fun onResponse(call: Call<TopRatedShowsResponse>, response: Response<TopRatedShowsResponse>) {
                _listTopRatedShowsResultLiveData.value = response.isSuccessful
                if (response.isSuccessful) {
                    _topRatedShowsListLiveData.value = response.body()!!.shows
                }
                _apiCallForFetchingTopRatedShowsInProgress.value = false
                _apiCallInProgress.value = _apiCallForFetchingShowsInProgress.value
            }

            override fun onFailure(call: Call<TopRatedShowsResponse>, t: Throwable) {
                _listTopRatedShowsResultLiveData.value = false
                _apiCallForFetchingTopRatedShowsInProgress.value = false
                _apiCallInProgress.value = _apiCallForFetchingShowsInProgress.value
            }

        })
    }
}