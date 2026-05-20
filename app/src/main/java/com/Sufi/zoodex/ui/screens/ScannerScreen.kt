package com.Sufi.zoodex.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.Sufi.zoodex.data.GameState
import com.Sufi.zoodex.ui.theme.*
import com.Sufi.zoodex.util.GalleryUtils
import kotlinx.coroutines.delay
import java.io.File
import java.util.concurrent.Executors

@Composable
fun ScannerScreen(
    onBack: () -> Unit,
    onCapture: (List<Int>) -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var captureTriggered by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(hasCameraPermission) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Wrap in animated visibility for transitions
    AnimatedContent(
        targetState = capturedBitmap,
        transitionSpec = {
            slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
        },
        label = "ScannerState"
    ) { targetBitmap ->
        if (targetBitmap != null) {
            ScanAnalysisScreen(
                capturedBitmap = targetBitmap,
                onBack = { capturedBitmap = null },
                onAnimalsAnalyzed = { ids ->
                    onCapture(ids)
                }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianBlack)
            ) {
                if (hasCameraPermission) {
                    CameraScannerView(
                        onCaptured = { bitmap ->
                            capturedBitmap = bitmap
                        },
                        captureTriggered = captureTriggered,
                        onCaptureReset = { captureTriggered = false }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Camera permission is required for real scanning.",
                            color = TextPrimary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // Cyber overlays (Reticle, scanning line, info panels)
                ScannerOverlay(
                    onBack = onBack,
                    hasCameraPermission = hasCameraPermission,
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    onTriggerCapture = { captureTriggered = true }
                )
            }
        }
    }
}

@Composable
fun CameraScannerView(
    onCaptured: (Bitmap) -> Unit,
    captureTriggered: Boolean,
    onCaptureReset: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var cameraReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val capture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
        
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                capture
            )
            imageCapture = capture
            cameraReady = true
        } catch (e: Exception) {
            Log.e("CameraScannerView", "Use case binding failed", e)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
    }

    LaunchedEffect(captureTriggered) {
        if (!captureTriggered) return@LaunchedEffect
        
        // Ensure we have a valid capture instance
        val capture = imageCapture
        if (capture == null || !cameraReady) {
            Log.e("CameraScannerView", "Camera not ready for capture: capture=$capture, ready=$cameraReady")
            onCaptureReset()
            return@LaunchedEffect
        }
        
        val photoFile = File(context.cacheDir, "zoodex_scan_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        
        capture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    if (bitmap != null) {
                        GalleryUtils.saveImageToGallery(context, bitmap, "zoodex_scan_${System.currentTimeMillis()}.jpg")
                        onCaptured(bitmap)
                    } else {
                        Log.e("CameraScannerView", "Decoded bitmap is null from ${photoFile.absolutePath}")
                    }
                    onCaptureReset()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraScannerView", "Capture failed: ${exception.message}", exception)
                    onCaptureReset()
                }
            }
        )
    }
}

@Composable
fun ScannerOverlay(
    onBack: () -> Unit,
    hasCameraPermission: Boolean,
    onRequestPermission: () -> Unit,
    onTriggerCapture: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OverlayEffects")
    
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ScanLine"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Laser Scan Line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.005f)
                .align(Alignment.TopCenter)
                .offset(y = 20.dp) // placeholder logic for translation
                .let {
                    // Let's position it dynamically relative to scanLineY
                    it.drawBehind {
                        val yPos = size.height * scanLineY
                        drawLine(
                            color = AppleGreen.copy(0.8f),
                            start = Offset(0f, yPos),
                            end = Offset(size.width, yPos),
                            strokeWidth = 6f
                        )
                    }
                }
        )

        // Shutter / Header overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(GlassSurface)
                        .border(1.dp, Color.White.copy(0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextPrimary
                    )
                }

                // Title
                Text(
                    text = "BIO-SCANNER",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    letterSpacing = 1.sp
                )

                // Toggle Mode Button
                Button(
                    onClick = onRequestPermission,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GlassSurface,
                        contentColor = TextPrimary
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(0.1f))
                ) {
                    Text(
                        text = if (hasCameraPermission) "CAM READY" else "GRANT CAMERA",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Target Scope Bracket Visuals (Center)
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .align(Alignment.CenterHorizontally)
                    .drawBehind {
                        val thickness = 4.dp.toPx()
                        val len = 30.dp.toPx()
                        val color = AppleGreen

                        // Top Left Bracket
                        drawRect(color, Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(len, thickness))
                        drawRect(color, Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(thickness, len))

                        // Top Right Bracket
                        drawRect(color, Offset(size.width - len, 0f), size = androidx.compose.ui.geometry.Size(len, thickness))
                        drawRect(color, Offset(size.width - thickness, 0f), size = androidx.compose.ui.geometry.Size(thickness, len))

                        // Bottom Left Bracket
                        drawRect(color, Offset(0f, size.height - thickness), size = androidx.compose.ui.geometry.Size(len, thickness))
                        drawRect(color, Offset(0f, size.height - len), size = androidx.compose.ui.geometry.Size(thickness, len))

                        // Bottom Right Bracket
                        drawRect(color, Offset(size.width - len, size.height - thickness), size = androidx.compose.ui.geometry.Size(len, thickness))
                        drawRect(color, Offset(size.width - thickness, size.height - len), size = androidx.compose.ui.geometry.Size(thickness, len))
                    }
            )

            // Bottom Shutter controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ALIGN TARGET INSIDE SCOPE BRACKETS",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppleGreen,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Shutter Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(0.1f))
                        .border(4.dp, Color.White, CircleShape)
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(CyberGradient)
                        .clickable(enabled = hasCameraPermission) { 
                            Log.d("ScannerOverlay", "Shutter clicked")
                            onTriggerCapture() 
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ObsidianBlack.copy(0.4f))
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
