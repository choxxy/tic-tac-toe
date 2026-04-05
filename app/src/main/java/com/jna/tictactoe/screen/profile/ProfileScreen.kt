package com.jna.tictactoe.screen.profile

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.jna.tictactoe.R
import com.jna.tictactoe.util.createTempUri


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userPreferences by viewModel.userPreferences.collectAsState()
    var nameInput by remember(userPreferences.name) { mutableStateOf(userPreferences.name) }
    val showDialog by viewModel.showImageSourceDialog.collectAsState()
    val context = LocalContext.current
    var tempUri by remember { mutableStateOf<Uri?>(null) }


    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            viewModel.onImageSelected(uri)
        }
    val cameraLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                viewModel.onImageSelected(tempUri)
            }
        }

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)


    if (showDialog) {
        ImageSourceDialog(
            onDismiss = { viewModel.onHideImageSourceDialog() },
            onCameraClick = {
                viewModel.onHideImageSourceDialog()
                if (cameraPermissionState.status.isGranted) {
                    tempUri = context.createTempUri()
                    cameraLauncher.launch(tempUri)
                } else {
                    cameraPermissionState.launchPermissionRequest()
                }
            },
            onGalleryClick = {
                viewModel.onHideImageSourceDialog()
                pickMedia.launch(
                    androidx.activity.result.PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Avatar
            Box(
                contentAlignment = Alignment.BottomEnd
            ) {
                AsyncImage(
                    model = userPreferences.profilePicturePath,
                    contentDescription = "User Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                    error = painterResource(id = R.drawable.ic_launcher_foreground)
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .clickable { viewModel.onShowImageSourceDialog() }
                        .padding(8.dp)

                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit Profile Picture",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }


            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = userPreferences.rank,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = nameInput,
                onValueChange = {
                    nameInput = it
                    viewModel.updateName(it)
                },
                label = { Text("Player Name") },
                textStyle = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Stats Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(24.dp)
            ) {
                Text(
                    text = "Statistics",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                        label = "Win Rate",
                        value = "${(userPreferences.winRate * 100).toInt()}%"
                    )
                    StatItem(label = "Rank", value = userPreferences.rank)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Light
        )
    }
}

@Composable
fun ImageSourceDialog(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Image Source") },
        text = { Text("Select a source for your profile picture.") },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(onClick = onCameraClick, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.CameraAlt, contentDescription = "Camera")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Camera")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onGalleryClick, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.PhotoLibrary, contentDescription = "Gallery")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gallery")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
