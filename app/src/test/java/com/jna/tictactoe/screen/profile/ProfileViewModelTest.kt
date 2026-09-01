package com.jna.tictactoe.screen.profile

import android.app.Application
import com.jna.tictactoe.data.PreferenceRepository
import com.jna.tictactoe.data.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var preferenceRepository: PreferenceRepository

    @Mock
    private lateinit var application: Application

    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        `when`(preferenceRepository.userPreferencesFlow).thenReturn(flowOf(UserPreferences()))
        viewModel = ProfileViewModel(preferenceRepository, application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `updateName with valid name updates repository`() = runTest {
        val validName = "Valid Name"
        viewModel.updateName(validName)
        runCurrent()
        
        assertNull(viewModel.nameError.value)
        verify(preferenceRepository).updateName(validName)
    }

    @Test
    fun `updateName with empty name sets error and does not update repository`() = runTest {
        val emptyName = ""
        viewModel.updateName(emptyName)
        runCurrent()
        
        assertEquals("Name cannot be empty", viewModel.nameError.value)
        verify(preferenceRepository, never()).updateName(anyString())
    }

    @Test
    fun `updateName with blank name sets error and does not update repository`() = runTest {
        val blankName = "   "
        viewModel.updateName(blankName)
        runCurrent()
        
        assertEquals("Name cannot be empty", viewModel.nameError.value)
        verify(preferenceRepository, never()).updateName(anyString())
    }

    @Test
    fun `updateName with too long name sets error and does not update repository`() = runTest {
        // 64 bytes (UTF-8)
        val tooLongName = "a".repeat(64)
        viewModel.updateName(tooLongName)
        runCurrent()
        
        assertEquals("Name is too long", viewModel.nameError.value)
        verify(preferenceRepository, never()).updateName(anyString())
    }
    
    @Test
    fun `updateName with 63 bytes name updates repository`() = runTest {
        val validLongName = "a".repeat(63)
        viewModel.updateName(validLongName)
        runCurrent()
        
        assertNull(viewModel.nameError.value)
        verify(preferenceRepository).updateName(validLongName)
    }

    @Test
    fun `updateName with special characters exceeding 63 bytes sets error`() = runTest {
        // Emoji like 🚀 is 4 bytes in UTF-8.
        // 16 * 4 = 64 bytes
        val emojiName = "🚀".repeat(16)
        viewModel.updateName(emojiName)
        runCurrent()
        
        assertEquals("Name is too long", viewModel.nameError.value)
        verify(preferenceRepository, never()).updateName(anyString())
    }
}
