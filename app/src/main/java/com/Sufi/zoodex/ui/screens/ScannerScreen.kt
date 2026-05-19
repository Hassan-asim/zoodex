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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.Sufi.zoodex.data.GameState
import com.Sufi.zoodex.ui.theme.*
import kotlinx.coroutines.delay
import java.io.File
import java.util.concurrent.ExecutorService
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

    var isSimulationMode by remember { mutableStateOf(!hasCameraPermission) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var captureTriggered by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            isSimulationMode = true
        }
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
                if (hasCameraPermission && !isSimulationMode) {
                    CameraScannerView(
                        onCaptured = { bitmap ->
                            capturedBitmap = bitmap
                        },
                        captureTriggered = captureTriggered,
                        onCaptureReset = { captureTriggered = false }
                    )
                } else {
                    SimulatedScannerView(
                        onCaptured = { bitmap ->
                            capturedBitmap = bitmap
                        },
                        captureTriggered = captureTriggered,
                        onCaptureReset = { captureTriggered = false }
                    )
                }

                // Cyber overlays (Reticle, scanning line, info panels)
                ScannerOverlay(
                    isSimulation = isSimulationMode,
                    onBack = onBack,
                    onToggleMode = {
                        if (hasCameraPermission) {
                            isSimulationMode = !isSimulationMode
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
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
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    ) { view ->
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(view.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraScannerView", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // Trigger photo capture
    LaunchedEffect(captureTriggered) {
        if (captureTriggered) {
            val capture = imageCapture
            if (capture != null) {
                val outputDirectory = context.cacheDir
                val photoFile = File(outputDirectory, "zoodex_scan_${System.currentTimeMillis()}.jpg")
                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                capture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                            if (bitmap != null) {
                                onCaptured(bitmap)
                            } else {
                                // Fallback
                                onCaptured(createSimulatedBitmap())
                            }
                            onCaptureReset()
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("CameraScannerView", "Capture failed: ${exception.message}", exception)
                            // Fallback to simulation
                            onCaptured(createSimulatedBitmap())
                            onCaptureReset()
                        }
                    }
                )
            } else {
                onCaptured(createSimulatedBitmap())
                onCaptureReset()
            }
        }
    }
}

@Composable
fun SimulatedScannerView(
    onCaptured: (Bitmap) -> Unit,
    captureTriggered: Boolean,
    onCaptureReset: () -> Unit
) {
    // Futuristic animated background representing simulation mode
    val infiniteTransition = rememberInfiniteTransition(label = "SimulatedScanner")
    
    val gridAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GridAlpha"
    )

    val scaleState by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Draw tech grid background
                val gridSize = 40.dp.toPx()
                val width = size.width
                val height = size.height
                
                // Draw horizontal lines
                var y = 0f
                while (y < height) {
                    drawLine(
                        color = CyberBlueStart.copy(alpha = gridAlpha),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                    y += gridSize
                }
                
                // Draw vertical lines
                var x = 0f
                while (x < width) {
                    drawLine(
                        color = CyberBlueStart.copy(alpha = gridAlpha),
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 1f
                    )
                    x += gridSize
                }

                // Draw secondary target lines
                drawCircle(
                    color = CyberBlueEnd.copy(alpha = gridAlpha * 0.4f),
                    center = center,
                    radius = 200f * scaleState,
                    style = Stroke(width = 2f)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                "SIMULATION LENS ACTIVE",
                style = MaterialTheme.typography.labelLarge,
                color = CyberBlueEnd,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Simulator mode allows capturing mock genetic code profiles without device camera constraints.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                fontSize = 11.sp
            )
        }
    }

    LaunchedEffect(captureTriggered) {
        if (captureTriggered) {
            delay(500) // Small delay to simulate camera snap
            onCaptured(createSimulatedBitmap())
            onCaptureReset()
        }
    }
}

@Composable
fun ScannerOverlay(
    isSimulation: Boolean,
    onBack: () -> Unit,
    onToggleMode: () -> Unit,
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
                    text = if (isSimulation) "SIMULATOR CAM" else "BIO-SCANNER",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    letterSpacing = 1.sp
                )

                // Toggle Mode Button
                Button(
                    onClick = onToggleMode,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GlassSurface,
                        contentColor = TextPrimary
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(0.1f))
                ) {
                    Text(
                        text = if (isSimulation) "USE REAL CAM" else "USE SIMULATOR",
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
                        .clickable { onTriggerCapture() },
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

// Generate beautiful simulated procedural bitmap representing wild biometric signature
private fun createSimulatedBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint()

    // 1. Dark obsidian background
    paint.color = android.graphics.Color.parseColor("#0A0A0E")
    canvas.drawRect(0f, 0f, 800f, 800f, paint)

    // 2. Neon digital telemetry circles
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 2f
    paint.color = android.graphics.Color.parseColor("#1A237E") // Deep blue
    canvas.drawCircle(400f, 400f, 320f, paint)
    
    paint.color = android.graphics.Color.parseColor("#00E676") // Green scanner ring
    paint.strokeWidth = 4f
    canvas.drawCircle(400f, 400f, 260f, paint)

    // 3. Draw cyber crosshair lines
    paint.strokeWidth = 2f
    paint.color = android.graphics.Color.parseColor("#00E676")
    canvas.drawLine(100f, 400f, 700f, 400f, paint)
    canvas.drawLine(400f, 100f, 400f, 700f, paint)

    // 4. Draw simulated animal visual node contours
    paint.style = Paint.Style.FILL
    paint.color = android.graphics.Color.parseColor("#29B6F6") // Cyan node
    canvas.drawCircle(400f, 300f, 16f, paint) // Head node
    
    canvas.drawCircle(350f, 450f, 20f, paint) // Left leg
    canvas.drawCircle(450f, 450f, 20f, paint) // Right leg

    paint.strokeWidth = 3f
    paint.style = Paint.Style.STROKE
    paint.color = android.graphics.Color.parseColor("#29B6F6")
    canvas.drawRect(320f, 320f, 480f, 440f, paint) // Body frame

    return bitmap
}
