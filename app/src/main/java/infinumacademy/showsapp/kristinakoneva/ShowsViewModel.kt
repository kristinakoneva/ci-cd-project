package infinumacademy.showsapp.kristinakoneva

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

    private val _listShowsResultLiveData = MutableLiveData(false)
    val listShowsResultLiveData: LiveData<Boolean> = _listShowsResultLiveData

    private val _topRatedShowsListLiveData = MutableLiveData<List<Show>>()
    val topRatedShowsListLiveData: LiveData<List<Show>> = _topRatedShowsListLiveData

    private val _showTopRatedLiveData = MutableLiveData(false)
    val showTopRatedLiveData: LiveData<Boolean> = _showTopRatedLiveData

    private val _apiCallInProgress = MutableLiveData(false)
    val apiCallInProgress: LiveData<Boolean> = _apiCallInProgress

    fun updateShowTopRated(isChecked: Boolean) {
        _showTopRatedLiveData.value = isChecked
    }

    fun fetchShows() {
        _apiCallInProgress.value = true
        ApiModule.retrofit.fetchShows().enqueue(object : Callback<ShowsResponse> {
            override fun onResponse(call: Call<ShowsResponse>, response: Response<ShowsResponse>) {
                _listShowsResultLiveData.value = response.isSuccessful
                if (response.isSuccessful) {
                    _showsListLiveData.value = response.body()!!.shows
                }
                _apiCallInProgress.value = false
            }

            override fun onFailure(call: Call<ShowsResponse>, t: Throwable) {
                _listShowsResultLiveData.value = false
                _apiCallInProgress.value = false
            }

        })
    }

    fun resetEmptyState() {
        _showEmptyStateLiveData.value = !_showEmptyStateLiveData.value!!
    }

    fun fetchTopRatedShows() {
        _apiCallInProgress.value = true
        ApiModule.retrofit.fetchTopRatedShows().enqueue(object : Callback<TopRatedShowsResponse> {
            override fun onResponse(call: Call<TopRatedShowsResponse>, response: Response<TopRatedShowsResponse>) {
                if (response.isSuccessful) {
                    _topRatedShowsListLiveData.value = response.body()!!.shows
                }
                _apiCallInProgress.value = false
            }

            override fun onFailure(call: Call<TopRatedShowsResponse>, t: Throwable) {
                // TODO("Not yet implemented")
                _apiCallInProgress.value = false
            }

        })
    }
}