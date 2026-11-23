package com.example.uthsmarttasks.auth.repository

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.uthsmarttasks.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth? = defaultFirebaseAuth()
) {
    val currentUser: FirebaseUser?
        get() = auth?.currentUser

    // Web client ID (client_type 3) do plugin google-services sinh ra từ google-services.json
    private fun getServerClientId(context: Context): String =
        context.getString(R.string.default_web_client_id)
    suspend fun signInWithGoogle(activityContext: Context): Result<FirebaseUser> {
        val firebaseAuth = auth
        val clientId = getServerClientId(activityContext)
        if (firebaseAuth == null || clientId.isBlank()) {
            return Result.failure(Exception("Chưa có cấu hình Google Sign-In. Bạn có thể dùng thử với chế độ khách."))
        }
        return try {
            val googleOption = GetSignInWithGoogleOption.Builder(clientId)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleOption)
                .build()

            val response = CredentialManager.create(activityContext)
                .getCredential(activityContext, request)
            val credential = response.credential
            if (credential !is CustomCredential ||
                credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                return Result.failure(Exception("Loại thông tin đăng nhập không hợp lệ."))
            }
            val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val user = firebaseAuth.signInWithCredential(firebaseCredential).await().user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Firebase không trả về người dùng sau khi đăng nhập."))
            }
        } catch (e: CancellationException) {
            // Hủy coroutine phải được truyền tiếp cho lifecycle, không đổi thành lỗi đăng nhập.
            throw e
        } catch (_: GetCredentialCancellationException) {
            Result.failure(Exception("Bạn đã huỷ đăng nhập bằng Google."))
        } catch (e: NoCredentialException) {
            Log.e(TAG, "No credential", e)
            Result.failure(
                Exception(
                    "Không tìm thấy thông tin đăng nhập Google khả dụng. " +
                        "Hãy kiểm tra tài khoản Google trên thiết bị và thử lại."
                )
            )
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential Manager error: ${e.type}", e)
            val message = e.errorMessage?.toString().orEmpty()
            when {
                e is GetCredentialProviderConfigurationException ||
                    Regex("\\b10\\b").containsMatchIn(message) ||
                    message.contains("developer", ignoreCase = true) ->
                    Result.failure(
                        Exception(
                            "Lỗi cấu hình Google Sign-In. Hãy kiểm tra vân tay SHA-1/SHA-256 " +
                                "trong Firebase Console và Google Play Services trên máy."
                        )
                    )

                message.contains("network", ignoreCase = true) ||
                    message.contains("timeout", ignoreCase = true) ->
                    Result.failure(
                        Exception("Không có kết nối mạng. Vui lòng kiểm tra Internet và thử lại.")
                    )

                else -> Result.failure(Exception("Không thể đăng nhập bằng Google. Vui lòng thử lại."))
            }
        } catch (e: GoogleIdTokenParsingException) {
            Log.e(TAG, "Invalid Google ID token", e)
            Result.failure(Exception("Google trả về ID token không hợp lệ."))
        } catch (e: FirebaseNetworkException) {
            Log.e(TAG, "Network error", e)
            Result.failure(Exception("Không có kết nối mạng. Vui lòng kiểm tra Internet và thử lại."))
        } catch (e: FirebaseTooManyRequestsException) {
            Log.e(TAG, "Too many Firebase requests", e)
            Result.failure(Exception(firebaseAuthErrorMessage("ERROR_TOO_MANY_REQUESTS")))
        } catch (e: FirebaseAuthException) {
            Log.e(TAG, "Firebase Auth error: ${e.errorCode}", e)
            Result.failure(Exception(firebaseAuthErrorMessage(e.errorCode)))
        } catch (e: Exception) {
            Log.e(TAG, "Firebase sign-in failed", e)
            Result.failure(Exception("Đăng nhập bằng Google thất bại. Vui lòng thử lại."))
        }
    }
    suspend fun signOut(context: Context) {
        auth?.signOut()
        try {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Failed to clear credential state", e)
        }
    }

    suspend fun updateDisplayName(name: String): Result<FirebaseUser> = runCatching {
        val user = auth?.currentUser ?: error("Vui lòng đăng nhập lại để sửa hồ sơ.")
        user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name.trim()).build()).await()
        user
    }

    private companion object {
        const val TAG = "AuthRepository"
    }
}

internal fun defaultFirebaseAuth(): FirebaseAuth? = try {
    FirebaseAuth.getInstance()
} catch (_: IllegalStateException) {
    null
}

internal fun firebaseAuthErrorMessage(errorCode: String?): String = when (errorCode) {
    "ERROR_NETWORK_REQUEST_FAILED" ->
        "Không có kết nối mạng. Vui lòng kiểm tra Internet và thử lại."

    "ERROR_INVALID_CREDENTIAL", "ERROR_INVALID_ID_TOKEN" ->
        "Thông tin đăng nhập Google không hợp lệ hoặc đã hết hạn."

    "ERROR_USER_DISABLED" ->
        "Tài khoản này đã bị vô hiệu hoá trong Firebase Authentication."

    "ERROR_TOO_MANY_REQUESTS" ->
        "Bạn đã thử quá nhiều lần. Vui lòng đợi một lúc rồi thử lại."

    "ERROR_OPERATION_NOT_ALLOWED" ->
        "Đăng nhập Google chưa được bật trong Firebase Console."

    else ->
        "Xác thực với Firebase thất bại. Vui lòng thử lại."
}
