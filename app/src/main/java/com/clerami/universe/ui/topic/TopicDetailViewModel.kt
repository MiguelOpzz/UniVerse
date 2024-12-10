import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.clerami.universe.data.local.FavoritePost
import com.clerami.universe.data.remote.retrofit.ApiConfig
import com.clerami.universe.data.remote.response.Comment
import com.clerami.universe.data.remote.response.CommentRequest
import com.clerami.universe.data.remote.response.CommentResponse
import com.clerami.universe.data.remote.response.DeleteResponse
import com.clerami.universe.data.remote.response.Topic
import com.clerami.universe.data.remote.response.UpdateResponse
import com.clerami.universe.data.remote.response.UpdateTopicRequest
import com.clerami.universe.data.repository.FavoriteRepository
import com.clerami.universe.utils.SessionManager
import gen._base._base_java__assetres.srcjar.R.id.title
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class TopicDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val favoriteRepository = FavoriteRepository(application.applicationContext)

    private val _topicDetails = MutableLiveData<Topic>()
    val topicDetails: LiveData<Topic> get() = _topicDetails

    private val _comments = MutableLiveData<List<Comment>>()
    val comments: LiveData<List<Comment>> get() = _comments

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _updateResponse = MutableLiveData<Boolean>()
    val updateResponse: LiveData<Boolean> get() = _updateResponse

    private val _deleteResponse = MutableLiveData<Boolean>()
    val deleteResponse: LiveData<Boolean> get() = _deleteResponse

    private val _tags = MutableLiveData<List<String>>()
    val tags: LiveData<List<String>> get() = _tags
    private val _newCommentLiveData = MutableLiveData<Comment>()
    val newCommentLiveData: LiveData<Comment> get() = _newCommentLiveData

    private val _attachmentsUrls = MutableLiveData<List<String>>()
    val attachmentsUrls: LiveData<List<String>> get() = _tags


    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("TopicPreferences", Context.MODE_PRIVATE)

    private val apiService = ApiConfig.getApiService(application)

    fun getTopicDetails(topicId: String) {
        apiService.getTopicById(topicId).enqueue(object : Callback<Topic> {
            override fun onResponse(call: Call<Topic>, response: Response<Topic>) {
                if (response.isSuccessful) {
                    _topicDetails.value = response.body()
                    _tags.value = response.body()?.tags ?: emptyList()
                    _attachmentsUrls.value = response.body()?.tags?: emptyList()
                } else {
                    _errorMessage.value = "Failed to fetch topic details."
                }
            }

            override fun onFailure(call: Call<Topic>, t: Throwable) {
                _errorMessage.value = "Network error: ${t.message}"
            }
        })
    }

    fun getComments(topicId: String) {
        ApiConfig.getApiService(getApplication()).getComments(topicId).enqueue(object : Callback<List<Comment>> {
            override fun onResponse(call: Call<List<Comment>>, response: Response<List<Comment>>) {
                if (response.isSuccessful) {
                    _comments.value = response.body() ?: emptyList()
                } else {
                    _errorMessage.value = "Failed to fetch comments."
                }
            }

            override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
                Log.d("Comments", "Failed to Fetch Comment")
                _errorMessage.value = "Network error: ${t.message}"
            }
        })
    }

    fun createComment(token: String, topicId: String, commentText: CommentRequest) {
        val call = apiService.postComment("Bearer $token", topicId, commentText)

        call.enqueue(object : Callback<CommentResponse> {
            override fun onResponse(call: Call<CommentResponse>, response: Response<CommentResponse>) {
                if (response.isSuccessful) {
                    val commentResponse = response.body()
                    val newComment = commentResponse?.comment
                    newComment?.let {
                        // Notify the Activity that a new comment was added
                        _newCommentLiveData.postValue(it)
                    }
                } else {
                    // Handle error
                    println("Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                // Handle failure
                println("Request failed: ${t.message}")
            }
        })
    }



    suspend fun isFavorite(topicId: String): Boolean {
        return favoriteRepository.getFavoritePostByTopicId(topicId) != null
    }

    suspend fun setFavorite(topicId: String, isFavorite: Boolean) {
        val topicDetails = topicDetails.value ?: return
        val favoritePost = FavoritePost(
            topicId = topicId,
            title = topicDetails.title,
            description = topicDetails.description ?: ""
        )
        if (isFavorite) {
            favoriteRepository.insertFavoritePost(favoritePost)
        } else {
            favoriteRepository.deleteFavoritePost(topicId)
        }
    }

    fun isLiked(topicId: String): Boolean {
        return sharedPreferences.getBoolean("isLiked_$topicId", false)
    }

    fun setLiked(topicId: String, isLiked: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLiked_$topicId", isLiked)
        editor.apply()
    }

    fun deleteTopic(topicId: String) {
        val token = SessionManager(getApplication()).getUserToken() ?: return
        val authorizationHeader = "Bearer $token"

        apiService.deleteTopic(authorizationHeader,topicId).enqueue(object : Callback<DeleteResponse> {
            override fun onResponse(call: Call<DeleteResponse>, response: Response<DeleteResponse>) {
                if (response.isSuccessful) {
                    _deleteResponse.value = true  // Indicate success
                } else {
                    _deleteResponse.value = false  // Indicate failure
                }
            }

            override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                _deleteResponse.value = false  // Indicate failure due to network error
            }
        })
    }

}
