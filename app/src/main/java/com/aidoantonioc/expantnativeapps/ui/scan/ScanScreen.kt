package com.aidoantonioc.expantnativeapps.ui.scan

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.aidoantonioc.expantnativeapps.domain.model.TransactionDetails
import com.aidoantonioc.expantnativeapps.ui.util.formatRupiah
import java.io.File

@Composable
fun ScanRoute(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: ScanViewModel = viewModel(factory = ScanViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // rememberSaveable: survives rotation / process death while the camera app is open.
    var cameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) viewModel.onImageSelected(uri) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) cameraUri?.let(viewModel::onImageSelected) }

    LaunchedEffect(uiState) {
        if (uiState is ScanUiState.Saved) onSaved()
    }

    ScanScreen(
        uiState = uiState,
        onBack = onBack,
        onTakePhoto = {
            val uri = createCameraUri(context)
            cameraUri = uri
            cameraLauncher.launch(uri)
        },
        onPickFromGallery = {
            galleryLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onRetry = viewModel::retry,
        onReset = viewModel::reset,
        onSave = viewModel::save
    )
}

private fun createCameraUri(context: Context): Uri {
    val dir = File(context.cacheDir, "camera").apply { mkdirs() }
    val file = File(dir, "receipt_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    uiState: ScanUiState,
    onBack: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit,
    onRetry: () -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan receipt") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (uiState) {
                ScanUiState.Idle -> {
                    Text(
                        "Take a clear, well-lit photo of the receipt, with the whole receipt in frame.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Button(onClick = onTakePhoto, modifier = Modifier.fillMaxWidth()) {
                        Text("Take photo")
                    }
                    OutlinedButton(onClick = onPickFromGallery, modifier = Modifier.fillMaxWidth()) {
                        Text("Choose from gallery")
                    }
                }

                is ScanUiState.Scanning -> {
                    ReceiptPreview(uiState.imageUri)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator()
                        Text("Reading receipt… this can take a few seconds.")
                    }
                }

                is ScanUiState.Error -> {
                    ReceiptPreview(uiState.imageUri)
                    Text(uiState.message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
                        Text("Try again")
                    }
                    OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
                        Text("Choose another photo")
                    }
                }

                is ScanUiState.Review -> {
                    ReceiptPreview(uiState.imageUri)
                    ReviewContent(uiState.details)
                    Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                        Text("Save transaction")
                    }
                    OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
                        Text("Scan again")
                    }
                }

                ScanUiState.Saved -> CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun ReceiptPreview(uri: Uri) {
    AsyncImage(
        model = uri,
        contentDescription = "Receipt photo",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    )
}

@Composable
private fun ReviewContent(details: TransactionDetails) {
    Text(
        "Check the result. OCR can misread text, so verify the numbers before saving.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(details.transaction.merchantName, style = MaterialTheme.typography.titleLarge)
            Text(
                details.transaction.date,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                formatRupiah(details.transaction.totalAmount),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
    Text("Items (${details.items.size})", style = MaterialTheme.typography.titleMedium)
    if (details.items.isEmpty()) {
        Text("No items were detected.", style = MaterialTheme.typography.bodyMedium)
    }
    details.items.forEach { item ->
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "${item.qty} × ${formatRupiah(item.price)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(formatRupiah(item.subtotal), style = MaterialTheme.typography.bodyLarge)
        }
    }
}