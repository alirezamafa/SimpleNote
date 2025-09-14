package simplenote.data.db

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import simplenote.ChangePasswordRequest
import simplenote.MessageResponse
import simplenote.RefreshRequest
import simplenote.RefreshResponse
import simplenote.RegisterRequest
import simplenote.RegisterResponse
import simplenote.TokenRequest
import simplenote.TokenResponse
import simplenote.UserInfo

interface AuthService {
    @POST("/api/auth/token/")
    suspend fun obtainToken(@Body body: TokenRequest): TokenResponse

    @POST("/api/auth/register/")
    suspend fun register(@Body body: RegisterRequest): RegisterResponse

    @POST("/api/auth/token/refresh/")
    suspend fun refreshToken(@Body body: RefreshRequest): RefreshResponse

    @GET("/api/auth/userinfo/")
    suspend fun userInfo(): UserInfo

    @POST("/api/auth/change-password/")
    suspend fun changePassword(@Body body: ChangePasswordRequest): MessageResponse
}
