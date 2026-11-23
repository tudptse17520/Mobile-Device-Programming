package com.example.productapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productapp.model.Product
import com.example.productapp.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Trạng thái UI cho màn hình Product.
 */
sealed class ProductUiState {
    /** Đang tải dữ liệu từ API */
    data object Loading : ProductUiState()

    /** Tải thành công, chứa dữ liệu product */
    data class Success(val product: Product) : ProductUiState()

    /** Tải thất bại, chứa thông báo lỗi */
    data class Error(val message: String) : ProductUiState()
}

/**
 * ViewModel quản lý logic gọi API và cập nhật trạng thái UI.
 * Sử dụng StateFlow để Compose tự động recompose khi state thay đổi.
 */
class ProductViewModel : ViewModel() {

    // MutableStateFlow chỉ ViewModel mới sửa được
    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Loading)

    // StateFlow public cho UI observe
    val uiState: StateFlow<ProductUiState> = _uiState

    init {
        // Tự động gọi API khi ViewModel được tạo
        fetchProduct()
    }

    /**
     * Gọi API lấy thông tin sản phẩm.
     * Sử dụng viewModelScope để coroutine tự hủy khi ViewModel bị destroy.
     */
    fun fetchProduct() {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            try {
                val product = RetrofitClient.apiService.getProduct()
                _uiState.value = ProductUiState.Success(product)
            } catch (e: Exception) {
                _uiState.value = ProductUiState.Error(
                    e.message ?: "Không thể tải dữ liệu sản phẩm"
                )
            }
        }
    }
}
