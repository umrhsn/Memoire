package com.umrhsn.mmoire.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.umrhsn.mmoire.R
import com.umrhsn.mmoire.models.BoardSize
import com.umrhsn.mmoire.ui.components.AppDialog
import com.umrhsn.mmoire.ui.components.AppHeader
import com.umrhsn.mmoire.ui.theme.MemoireTheme
import com.umrhsn.mmoire.utils.EXTRA_GAME_NAME
import com.umrhsn.mmoire.viewmodels.CreateViewModel
import kotlin.math.ceil

@Composable
fun CreateScreen(
    viewModel: CreateViewModel,
    boardSize: BoardSize,
    chosenImageUris: List<Uri>,
    onBackClicked: () -> Unit,
    onPlaceholderClicked: () -> Unit,
    onImageClicked: (Int) -> Unit,
    onRemoveImage: (Uri) -> Unit,
    onSaveClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var gameName by remember { mutableStateOf("") }
    val numImagesRequired = boardSize.getNumPairs()
    val context = LocalContext.current

    MemoireTheme {
        Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header (Shared)
                AppHeader(
                    title = "New Board",
                    navigationIcon = {
                        IconButton(onClick = onBackClicked) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    // Step 1: Info with Icon
                    SectionHeader(
                        icon = Icons.Default.Collections,
                        title = "Step 1: Choose Photos",
                        subtitle = "Select $numImagesRequired pairs for your board."
                    )

                    // Selection Progress Bar
                    val selectionProgress = chosenImageUris.size.toFloat() / numImagesRequired
                    LinearProgressIndicator(
                        progress = { selectionProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .height(8.dp),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                        color = if (selectionProgress >= 1f) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    // Grid Area
                    Box(modifier = Modifier.weight(1f)) {
                        if (chosenImageUris.isEmpty()) {
                            EmptySelectionState(onPlaceholderClicked)
                        } else {
                            DynamicImageGrid(
                                chosenImageUris = chosenImageUris,
                                numImagesRequired = numImagesRequired,
                                onPlaceholderClicked = onPlaceholderClicked,
                                onImageClicked = onImageClicked,
                                onRemoveImage = onRemoveImage
                            )
                        }
                    }

                    // Step 2: Naming
                    SectionHeader(
                        icon = Icons.Default.DriveFileRenameOutline,
                        title = "Step 2: Name Your Game",
                        subtitle = "Give it a unique ID to find it later."
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Bottom Control Section
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    shadowElevation = 24.dp
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .navigationBarsPadding()
                    ) {
                        OutlinedTextField(
                            value = gameName,
                            onValueChange = { input ->
                                if (input.length <= 14 && input.all { it.isLetterOrDigit() || it == '_' }) {
                                    gameName = input
                                }
                            },
                            label = { Text("Board Identity") },
                            placeholder = { Text("e.g., family_vacation") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !uiState.isUploading,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null) }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { onSaveClicked(gameName) },
                            modifier = Modifier
                                .height(64.dp)
                                .fillMaxWidth(),
                            enabled = chosenImageUris.size == numImagesRequired && 
                                      gameName.isNotBlank() && 
                                      gameName.length >= 3 && 
                                      !uiState.isUploading,
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                        ) {
                            if (uiState.isUploading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("SAVING BOARD...", fontWeight = FontWeight.Black)
                            } else {
                                Icon(Icons.Default.CloudUpload, contentDescription = null)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("CREATE & PLAY", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        if (uiState.isUploading) {
                            Spacer(modifier = Modifier.height(16.dp))
                            LinearProgressIndicator(
                                progress = { uiState.uploadProgress / 100f },
                                modifier = Modifier.fillMaxWidth(),
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }

            // Success Dialog
            if (uiState.isSuccess) {
                AppDialog(
                    onDismissRequest = {}, 
                    title = "Board Ready! 🎉",
                    icon = Icons.Default.CheckCircle
                ) {
                    Text(
                        text = "Your custom board '${uiState.gameName}' is ready to play. It has been saved to your local storage.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            val activity = context as? Activity
                            val resultData = Intent()
                            resultData.putExtra(EXTRA_GAME_NAME, uiState.gameName)
                            activity?.setResult(Activity.RESULT_OK, resultData)
                            activity?.finish()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("START GAME", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Name Taken Dialog
            if (uiState.nameTaken) {
                AppDialog(
                    onDismissRequest = { viewModel.resetState() },
                    title = "Name Exists 😕",
                    icon = Icons.Default.Warning
                ) {
                    Text(
                        text = "A board with the ID '${uiState.gameName}' is already saved. Try using a date or a unique suffix.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.resetState() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("OK, CHANGE NAME")
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.padding(top = 24.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptySelectionState(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "No Photos Yet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "Select images from your gallery to build your board.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("OPEN GALLERY", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun DynamicImageGrid(
    chosenImageUris: List<Uri>,
    numImagesRequired: Int,
    onPlaceholderClicked: () -> Unit,
    onImageClicked: (Int) -> Unit,
    onRemoveImage: (Uri) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val totalSlots = chosenImageUris.size + (if (chosenImageUris.size < numImagesRequired) 1 else 0)
        val spacing = 8.dp
        
        var bestCols = 1
        var bestCardSizeValue = 0f

        for (cols in 1..totalSlots) {
            val rows = ceil(totalSlots.toFloat() / cols).toInt()
            
            val availableWidth = maxWidth - (spacing * (cols - 1))
            val availableHeight = maxHeight - (spacing * (rows - 1))
            
            val cw = availableWidth.value / cols
            val ch = availableHeight.value / rows
            
            val s = if (cw < ch) cw else ch
            
            if (s > bestCardSizeValue) {
                bestCardSizeValue = s
                bestCols = cols
            }
        }

        val rows = ceil(totalSlots.toFloat() / bestCols).toInt()
        val bestCardSize = bestCardSizeValue.dp

        Column(
            modifier = Modifier.wrapContentSize(),
            verticalArrangement = Arrangement.spacedBy(spacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (r in 0 until rows) {
                Row(
                    modifier = Modifier.wrapContentWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (c in 0 until bestCols) {
                        val index = r * bestCols + c
                        if (index < totalSlots) {
                            Box(modifier = Modifier.size(bestCardSize)) {
                                if (index < chosenImageUris.size) {
                                    ImageItem(
                                        uri = chosenImageUris[index],
                                        onClick = { onImageClicked(index) },
                                        onRemove = { onRemoveImage(chosenImageUris[index]) }
                                    )
                                } else {
                                    PlaceholderItem(onClick = onPlaceholderClicked)
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.width(bestCardSize))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageItem(
    uri: Uri,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Delete Icon Badge
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .clickable { onRemove() },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                contentColor = Color.White,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceholderItem(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AddPhotoAlternate,
                contentDescription = "Add image",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
