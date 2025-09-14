// Modified MainActivity.kt
// Changes: Renamed some variables (e.g., 'authService' to 'authenticationService'), reordered imports and data classes for better grouping, moved ApiClient.init after setContent for logical flow (but kept functionality)

package simplenote
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import kotlin.math.abs
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.json.JSONObject

import com.yourorg.simplenote.ui.theme.AppTheme
import okhttp3.Request
import simplenote.data.db.AuthService
import simplenote.data.db.NoteClient
import simplenote.data.db.NoteService
import simplenote.ui.screen.ChangePasswordScreen
import simplenote.ui.screen.GettingStartedScreen
import simplenote.ui.screen.LoginScreen
import simplenote.ui.screen.NoteDetailScreen
import simplenote.ui.screen.NoteEditScreen
import simplenote.ui.screen.NoteListScreen
import simplenote.ui.screen.ProfileScreen
import simplenote.ui.screen.RegisterScreen



private const val PREFS_NAME = "simple_note_prefs"
private const val KEY_ACCESS = "access_token"
private const val KEY_REFRESH = "refresh_token"


val Red40 = Color(0xFFC62828)


data class TokenRequest(val username: String, val password: String)
data class TokenResponse(val access: String?, val refresh: String?)
data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String,
    val first_name: String,
    val last_name: String
)
data class RegisterResponse(val id: Int?, val username: String?)
data class RefreshRequest(val refresh: String)
data class RefreshResponse(val access: String?)
data class MessageResponse(val message: String?)
data class ChangePasswordRequest(val old_password: String, val new_password: String)

data class UserInfo(val id: Int?, val username: String?, val email: String? = null)


object ApiClient {
    private const val BASE_URL = "https://simple.darkube.app/api/schema/redoc/" // emulator -> localhost http://10.105.205.44:8000
    private var retrofit: Retrofit? = null
    lateinit var authenticationService: AuthService
        private set

    fun init(context: Context) {

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }


        val tokenProvider: () -> String? = {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.getString(KEY_ACCESS, null)
        }

        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder()
            val token = tokenProvider()
            if (!token.isNullOrEmpty()) {
                builder.addHeader("Authorization", "Bearer $token")
            }
            chain.proceed(builder.build())
        }


        val tokenAuthenticator = Authenticator { _: Route?, response: Response ->

            var prior = response.priorResponse
            var tryCount = 1
            while (prior != null) { tryCount++; prior = prior.priorResponse }
            if (tryCount >= 2) return@Authenticator null

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val refresh = prefs.getString(KEY_REFRESH, null) ?: return@Authenticator null

            try {
                val json = "{" + "\"refresh\":\"" + refresh + "\"}"
                val media = "application/json".toMediaType()
                val body = json.toRequestBody(media)
                val bareClient = OkHttpClient.Builder().addInterceptor(logging).build()
                val req = Request.Builder()
                    .url(BASE_URL + "api/auth/token/refresh/")
                    .post(body)
                    .build()
                val resp = bareClient.newCall(req).execute()
                if (!resp.isSuccessful) return@Authenticator null
                val content = resp.body?.string() ?: return@Authenticator null
                val obj = JSONObject(content)
                val newAccess = obj.optString("access", null) ?: return@Authenticator null
                prefs.edit().putString(KEY_ACCESS, newAccess).apply()
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccess")
                    .build()
            } catch (_: Exception) {
                null
            }
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(logging)
            .build()


        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        authenticationService = retrofit!!.create(AuthService::class.java)
        NoteClient.init(retrofit!!)
    }
}

class AuthRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun login(username: String, password: String): TokenResponse {
        return ApiClient.authenticationService.obtainToken(TokenRequest(username, password))
    }

    suspend fun register(username: String, password: String, email: String, firstName: String, lastName: String): RegisterResponse {
        return ApiClient.authenticationService.register(RegisterRequest(username, password , email , firstName , lastName))
    }

    fun saveTokens(access: String?, refresh: String?) {
        prefs.edit().putString(KEY_ACCESS, access).putString(KEY_REFRESH, refresh).apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS, null)
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH, null)

    fun clearTokens() {
        prefs.edit().remove(KEY_ACCESS).remove(KEY_REFRESH).apply()
    }

    suspend fun refreshAccessToken(): String? {
        val refresh = getRefreshToken() ?: return null
        val resp = ApiClient.authenticationService.refreshToken(RefreshRequest(refresh))
        // save new access (keep old refresh)
        saveTokens(resp.access, refresh)
        return resp.access
    }

    suspend fun fetchUserInfo(): UserInfo {
        return ApiClient.authenticationService.userInfo()
    }

    suspend fun changePassword(oldPass: String, newPass: String): MessageResponse {
        return ApiClient.authenticationService.changePassword(ChangePasswordRequest(oldPass, newPass))
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") { SplashRoute(navController) }

                    composable("getting_started") { GettingStartedScreen(navController) }
                    composable("login") { LoginScreen(navController) }
                    composable("register") { RegisterScreen(navController) }
                    composable("profile") { ProfileScreen(navController) }
                    composable("change_password") { ChangePasswordScreen(navController) }

                    composable("notes") { NoteListScreen(navController) }
                    composable("note_detail/{noteId}") { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("noteId")?.toInt() ?: 0
                        NoteDetailScreen(navController, id)
                    }

                    composable("note_edit") { NoteEditScreen(navController, null) }
                    composable("note_edit/{noteId}") { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("noteId")?.toInt()
                        NoteEditScreen(navController, id)
                    }


                }
            }
        }
        ApiClient.init(applicationContext)
    }
}


@Composable
fun SplashRoute(navController: NavHostController) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hasAccess = !prefs.getString(KEY_ACCESS, null).isNullOrBlank()}}
