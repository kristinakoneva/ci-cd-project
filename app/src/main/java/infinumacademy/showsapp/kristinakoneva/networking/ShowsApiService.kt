package infinumacademy.showsapp.kristinakoneva.networking

import infinumacademy.showsapp.kristinakoneva.model.CreateReviewRequest
import infinumacademy.showsapp.kristinakoneva.model.CreateReviewResponse
import infinumacademy.showsapp.kristinakoneva.model.DisplayShowResponse
import infinumacademy.showsapp.kristinakoneva.model.LoginRequest
import infinumacademy.showsapp.kristinakoneva.model.LoginResponse
import infinumacademy.showsapp.kristinakoneva.model.RegisterRequest
import infinumacademy.showsapp.kristinakoneva.model.RegisterResponse
import infinumacademy.showsapp.kristinakoneva.model.ReviewsResponse
import infinumacademy.showsapp.kristinakoneva.model.ShowsResponse
import infinumacademy.showsapp.kristinakoneva.model.TopRatedShowsResponse
import infinumacademy.showsapp.kristinakoneva.model.UpdateProfilePhotoResponse
import infinumacademy.showsapp.kristinakoneva.model.UserInfoResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ShowsApiService {
    @POST("/users")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("/users/sign_in")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @GET("/shows")
    fun fetchShows(): Call<ShowsResponse>

    @GET("/shows/{show_id}/reviews")
    fun fetchReviewsAboutShow(@Path("show_id") showId: Int): Call<ReviewsResponse>

    @POST("/reviews")
    fun createReview(@Body request: CreateReviewRequest): Call<CreateReviewResponse>

    @GET("/shows/{id}")
    fun displayShow(@Path("id") showId: Int): Call<DisplayShowResponse>

    @GET("/shows/top_rated")
    fun fetchTopRatedShows(): Call<TopRatedShowsResponse>

    @Multipart
    @PUT("/users")
    fun updateProfilePhoto(@Part image: MultipartBody.Part): Call<UpdateProfilePhotoResponse>

    @GET("/users/me")
    fun getMyUserInfo(): Call<UserInfoResponse>
}
