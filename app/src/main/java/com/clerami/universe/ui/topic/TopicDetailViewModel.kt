import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.clerami.universe.data.remote.retrofit.ApiConfig
import com.clerami.universe.data.remote.response.Comment
import com.clerami.universe.data.remote.response.Topic
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TopicDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val _topicDetails = MutableLiveData<Topic>()
    val topicDetails: LiveData<Topic> get() = _topicDetails

    private val _comments = MutableLiveData<List<Comment>>()
    val comments: LiveData<List<Comment>> get() = _comments

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("TopicPreferences", Context.MODE_PRIVATE)

    fun getTopicDetails(topicId: String) {
        ApiConfig.getApiService(getApplication()).getTopicById(topicId).enqueue(object : Callback<Topic> {
            override fun onResponse(call: Call<Topic>, response: Response<Topic>) {
                if (response.isSuccessful) {
                    _topicDetails.value = response.body()
                }
            }

            override fun onFailure(call: Call<Topic>, t: Throwable) {
                Log.d("Topic","Failed to Fetch Topic")
            }
        })
    }

    fun getComments(topicId: String) {
        ApiConfig.getApiService(getApplication()).getComments(topicId).enqueue(object : Callback<List<Comment>> {
            override fun onResponse(call: Call<List<Comment>>, response: Response<List<Comment>>) {
                if (response.isSuccessful) {
                    _comments.value = response.body()
                }
            }

            override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
                Log.d("Comments","Failed to Fetch Comment")
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
}
