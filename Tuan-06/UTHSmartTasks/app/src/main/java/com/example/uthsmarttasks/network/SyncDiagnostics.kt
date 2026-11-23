package com.example.uthsmarttasks.network

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestoreException

internal object SyncDiagnostics {
    fun report(operation: String, error: Exception) {
        val code = (error as? FirebaseFirestoreException)?.code ?: return
        if (code !in setOf(
                FirebaseFirestoreException.Code.PERMISSION_DENIED,
                FirebaseFirestoreException.Code.INTERNAL,
                FirebaseFirestoreException.Code.DATA_LOSS
            )) return

        // Lỗi gốc có thể chứa đường dẫn tài khoản hoặc dữ liệu task.
        val report = IllegalStateException("Firestore $operation: ${code.name}")
        report.stackTrace = error.stackTrace
        FirebaseCrashlytics.getInstance().recordException(report)
    }
}
