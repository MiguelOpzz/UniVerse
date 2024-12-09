import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.clerami.universe.data.remote.retrofit.ApiConfig
import com.clerami.universe.data.remote.response.Comment
import com.clerami.universe.data.remote.response.DeleteResponse
import com.clerami.universe.data.remote.response.Topic
import com.clerami.universe.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TopicDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val _topicDetails = MutableLiveData<Topic>()
    val topicDetails: LiveData<Topic> get() = _topicDetails

    private val _comments = MutableLiveData<List<Comment>>()
    val comments: LiveData<List<Comment>> get() = _comments

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage


    private val _deleteResponse = MutableLiveData<Boolean>()  // New LiveData for delete response
    val deleteResponse: LiveData<Boolean> get() = _deleteResponse

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("TopicPreferences", Context.MODE_PRIVATE)

    private val apiService = ApiConfig.getApiService(application)

    fun getTopicDetails(topicId: String) {
        ApiConfig.getApiService(getApplication()).getTopicById(topicId).enqueue(object : Callback<Topic> {
            override fun onResponse(call: Call<Topic>, response: Response<Topic>) {
                if (response.isSuccessful) {
                    _topicDetails.value = response.body()
                } else {
                    _errorMessage.value = "Failed to fetch topic details."
                }
            }

            override fun onFailure(call: Call<Topic>, t: Throwable) {
                Log.d("Topic", "Failed to Fetch Topic")
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

    fun createComment(topicId: String, replyText: String) {
        val token = SessionManager(getApplication()).getUserToken() ?: return
        val authorizationHeader = "Bearer $token"

        apiService.postComment(authorizationHeader, topicId, replyText).enqueue(object : Callback<Comment> {
            override fun onResponse(call: Call<Comment>, response: Response<Comment>) {
                if (response.isSuccessful) {
                    getComments(topicId)

                } else {
                    _errorMessage.value = "Failed to post comment: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<Comment>, t: Throwable) {
                _errorMessage.value = "Network error: ${t.message}"
            }
        })
    }


    fun isFavorite(topicId: String): Boolean {
        return sharedPreferences.getBoolean("isFavorite_$topicId", false)
    }

    fun setFavorite(topicId: String, isFavorite: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isFavorite_$topicId", isFavorite)
        editor.apply()
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
