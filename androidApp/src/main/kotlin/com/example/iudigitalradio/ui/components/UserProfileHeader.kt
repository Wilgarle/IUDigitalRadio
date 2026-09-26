package com.example.iudigitalradio.ui.components

import android.Manifest
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.net.Uri
import com.example.iudigitalradio.ui.theme.CyanBright
import com.example.iudigitalradio.ui.theme.SurfaceVariant
import kotlinx.coroutines.launch

/**
 * RF-02: Avatar circular de usuario con captura de cámara.
 * RF-03: Solicita permiso CAMERA en runtime antes de abrir la cámara.
 *        Muestra Snackbar si el permiso es denegado.
 *
 * Usa ActivityResultContracts.TakePicturePreview (retorna Bitmap thumbnail).
 * Usa ActivityResultContracts.RequestPermission para CAMERA.
 */
@Composable
fun UserAvatarButton(
    userPhoto: Bitmap?,
    onPhotoTaken: (Any?) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onNavigateToProfile: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()

    // RF-02: Lanzador de la cámara — ActivityResultContracts.TakePicturePreview
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let { onPhotoTaken(it) }
    }

    // RF-03: Lanzador del permiso CAMERA en runtime
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            // RF-03: Snackbar si el permiso es denegado
            scope.launch {
                snackbarHostState.showSnackbar(
                    message     = "Permiso de cámara denegado",
                    actionLabel = "OK"
                )
            }
        }
    }

    // RF-02: Lanzador de la galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onPhotoTaken(it) } // Ojo: onPhotoTaken ahora recibe Uri o Bitmap. Tendremos que usar Uri, Bitmap, o Any. Usaremos Bitmap internamente, así que necesitaremos convertir Uri a Bitmap.
    }

    var showOptionsDialog by remember { mutableStateOf(false) }

    if (showOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showOptionsDialog = false },
            title = { Text("Foto de Perfil", color = CyanBright, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = { 
                        showOptionsDialog = false
                        permissionLauncher.launch(Manifest.permission.CAMERA) 
                    }) {
                        Text("Tomar foto con la cámara", color = MaterialTheme.colorScheme.onSurface)
                    }
                    TextButton(onClick = { 
                        showOptionsDialog = false
                        galleryLauncher.launch("image/*") 
                    }) {
                        Text("Elegir de la galería", color = MaterialTheme.colorScheme.onSurface)
                    }
                    if (userPhoto != null) {
                        TextButton(onClick = { 
                            showOptionsDialog = false
                            onPhotoTaken(null) // Para eliminar, pasamos null. Tendremos que cambiar la firma de onPhotoTaken
                        }) {
                            Text("Eliminar foto", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOptionsDialog = false }) {
                    Text("Cancelar", color = CyanBright)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    val actualModifier = if (modifier == Modifier) Modifier.size(44.dp) else modifier

    Box(
        modifier = actualModifier
            .clip(CircleShape)
            .border(2.dp, CyanBright, CircleShape)
            .background(SurfaceVariant, CircleShape)
            .clickable { showOptionsDialog = true },
        contentAlignment = Alignment.Center
    ) {
        if (userPhoto != null) {
            // RF-02: Imagen circular con fillMaxSize para ocupar todo el círculo correctamente
            androidx.compose.foundation.Image(
                bitmap             = userPhoto.asImageBitmap(),
                contentDescription = "Foto de usuario",
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize().clip(CircleShape)
            )
        } else {
            Icon(
                imageVector        = Icons.Filled.Person,
                contentDescription = "Avatar — toca para tomar foto",
                tint               = CyanBright,
                modifier           = Modifier.fillMaxSize(0.6f)
            )
        }
    }
}

/**
 * Botón separado de cámara (pequeño ícono overlay en el avatar).
 * Se puede usar junto con UserAvatarButton para mostrar el ícono de cámara encima.
 */
@Composable
fun CameraIconOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(18.dp)
            .background(CyanBright, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = Icons.Filled.CameraAlt,
            contentDescription = "Tomar foto",
            tint               = SurfaceVariant,
            modifier           = Modifier.size(12.dp)
        )
    }
}
