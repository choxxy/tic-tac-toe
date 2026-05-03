package com.jna.tictactoe.screen.profile

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jna.tictactoe.data.PreferenceRepository
import com.jna.tictactoe.data.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
    private val application: Application
) : ViewModel() {

    private val _showImageSourceDialog = MutableStateFlow(false)
    val showImageSourceDialog: StateFlow<Boolean> = _showImageSourceDialog.asStateFlow()

    val userPreferences: StateFlow<UserPreferences> = preferenceRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun updateName(name: String) {
        viewModelScope.launch {
            preferenceRepository.updateName(name)
        }
    }

    fun onShowImageSourceDialog() {
        _showImageSourceDialog.value = true
    }

    fun onHideImageSourceDialog() {
        _showImageSourceDialog.value = false
    }

    fun onImageSelected(uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            val imagePath = saveImageToInternalStorage(uri)
            if (imagePath != null) {
                preferenceRepository.updateProfilePicturePath(imagePath)
            }
        }
        Log.d("ProfileViewModel", "Selected image uri: $uri")
    }

    private suspend fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = application.contentResolver.openInputStream(uri)
            // Create a file in internal storage
            val fileName = "profile_${System.currentTimeMillis()}.jpg"
            val file = File(application.filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            outputStream.close()
            inputStream?.close()
            file.absolutePath
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Failed to save image", e)
            null
        }
    }
}
