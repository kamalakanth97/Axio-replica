package com.example.ui.dialogs

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ui.theme.AxioTealPrimary
import com.example.util.UpiIntentHelper
import com.example.util.UpiPaymentDetails
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

data class SampleUpiQr(
    val title: String,
    val merchantName: String,
    val vpa: String,
    val amount: Double,
    val note: String,
    val qrUri: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraQrScannerDialog(
    onDismiss: () -> Unit,
    onQrScanned: (UpiPaymentDetails) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (!granted) {
                Toast.makeText(context, "Camera permission needed to scan UPI QR codes. You can also pick from presets below!", Toast.LENGTH_LONG).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var isTorchOn by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var cameraProviderInstance by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    var manualQrInput by remember { mutableStateOf("") }
    var showManualInput by remember { mutableStateOf(false) }

    // Sample UPI QRs for rapid testing & emulator usage
    val sampleQrs = listOf(
        SampleUpiQr(
            "BharatPe Store",
            "Sharma Kirana & General Store",
            "bharatpe09812@icici",
            340.0,
            "Groceries Bill",
            "upi://pay?pa=bharatpe09812@icici&pn=Sharma%20Kirana&am=340.00&cu=INR&tn=Groceries"
        ),
        SampleUpiQr(
            "Swiggy Counter",
            "Swiggy Dineout",
            "swiggy.dine@icici",
            720.0,
            "Weekend Lunch",
            "upi://pay?pa=swiggy.dine@icici&pn=Swiggy%20Dineout&am=720.00&cu=INR&tn=Weekend%20Lunch"
        ),
        SampleUpiQr(
            "PhonePe Merchant",
            "Blue Tokai Coffee",
            "bluetokai@ybl",
            480.0,
            "Cappuccino & Croissant",
            "upi://pay?pa=bluetokai@ybl&pn=Blue%20Tokai%20Coffee&am=480.00&cu=INR&tn=Coffee%20Break"
        ),
        SampleUpiQr(
            "Paytm Merchant",
            "Apollo Pharmacy Ltd",
            "apollopharmacy@paytm",
            560.0,
            "Medicines",
            "upi://pay?pa=apollopharmacy@paytm&pn=Apollo%20Pharmacy&am=560.00&cu=INR&tn=Medicines"
        ),
        SampleUpiQr(
            "Friend QR",
            "Rahul Sharma",
            "rahul.sharma@okhdfcbank",
            250.0,
            "Lunch split",
            "upi://pay?pa=rahul.sharma@okhdfcbank&pn=Rahul%20Sharma&am=250.00&cu=INR&tn=Lunch%20Share"
        )
    )

    // Animated scan line
    val infiniteTransition = rememberInfiniteTransition(label = "scan_laser")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    DisposableEffect(Unit) {
        onDispose {
            try {
                cameraProviderInstance?.unbindAll()
                cameraExecutor.shutdown()
            } catch (_: Exception) {}
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        BackHandler { onDismiss() }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // 1. Camera Preview or Fallback
                if (hasCameraPermission) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                                // Use COMPATIBLE (TextureView) to prevent SurfaceView abandoned BufferQueue errors
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            }

                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                try {
                                    val cameraProvider = cameraProviderFuture.get()
                                    cameraProviderInstance = cameraProvider

                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }

                                    val scanner = BarcodeScanning.getClient()
                                    val imageAnalysis = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()

                                    var isHandled = false
                                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                        val mediaImage = imageProxy.image
                                        if (mediaImage != null && !isHandled) {
                                            val image = InputImage.fromMediaImage(
                                                mediaImage,
                                                imageProxy.imageInfo.rotationDegrees
                                            )
                                            scanner.process(image)
                                                .addOnSuccessListener { barcodes ->
                                                    for (barcode in barcodes) {
                                                        val rawValue = barcode.rawValue
                                                        if (!rawValue.isNullOrBlank() && !isHandled) {
                                                            val upiDetails = UpiIntentHelper.parseUpiUri(rawValue)
                                                            if (upiDetails != null) {
                                                                isHandled = true
                                                                onQrScanned(upiDetails)
                                                                break
                                                            }
                                                        }
                                                    }
                                                }
                                                .addOnCompleteListener {
                                                    imageProxy.close()
                                                }
                                        } else {
                                            imageProxy.close()
                                        }
                                    }

                                    cameraProvider.unbindAll()
                                    val camera = cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        imageAnalysis
                                    )
                                    cameraControl = camera.cameraControl
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }, ContextCompat.getMainExecutor(ctx))

                            previewView
                        }
                    )
                } else {
                // Camera Permission Placeholder
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Camera Permission Required",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Enable camera to scan physical BharatPe, PhonePe, Paytm, or Google Pay QR codes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = AxioTealPrimary)
                    ) {
                        Text("Grant Camera Access")
                    }
                }
            }

            // 2. Viewfinder Overlay with Animated Scan Line
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 140.dp, top = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(2.dp, Color(0xFF00B4D8).copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                ) {
                    // Corner reticles
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 6.dp.toPx()
                        val cornerLen = 28.dp.toPx()
                        val w = size.width
                        val h = size.height

                        // Top-Left
                        drawLine(Color(0xFF2DD4BF), Offset(0f, 0f), Offset(cornerLen, 0f), stroke)
                        drawLine(Color(0xFF2DD4BF), Offset(0f, 0f), Offset(0f, cornerLen), stroke)

                        // Top-Right
                        drawLine(Color(0xFF2DD4BF), Offset(w, 0f), Offset(w - cornerLen, 0f), stroke)
                        drawLine(Color(0xFF2DD4BF), Offset(w, 0f), Offset(w, cornerLen), stroke)

                        // Bottom-Left
                        drawLine(Color(0xFF2DD4BF), Offset(0f, h), Offset(cornerLen, h), stroke)
                        drawLine(Color(0xFF2DD4BF), Offset(0f, h), Offset(0f, h - cornerLen), stroke)

                        // Bottom-Right
                        drawLine(Color(0xFF2DD4BF), Offset(w, h), Offset(w - cornerLen, h), stroke)
                        drawLine(Color(0xFF2DD4BF), Offset(w, h), Offset(w, h - cornerLen), stroke)

                        // Animated Laser Line
                        val y = h * laserPosition
                        drawLine(
                            color = Color(0xFF14B8A6),
                            start = Offset(10.dp.toPx(), y),
                            end = Offset(w - 10.dp.toPx(), y),
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                }
            }

            // 3. Top Action Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(AxioTealPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("UPI", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "Scan Any UPI QR",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = {
                        isTorchOn = !isTorchOn
                        cameraControl?.enableTorch(isTorchOn)
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Torch",
                        tint = if (isTorchOn) Color(0xFFFBBF24) else Color.White
                    )
                }
            }

            // 4. Bottom Controls & Quick QR Simulators
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.95f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Test with Sample Indian UPI QRs:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF94A3B8)
                        )
                        TextButton(
                            onClick = { showManualInput = !showManualInput },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(if (showManualInput) "Hide Paste" else "Paste QR Text", fontSize = 11.sp, color = Color(0xFF2DD4BF))
                        }
                    }

                    if (showManualInput) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = manualQrInput,
                                onValueChange = { manualQrInput = it },
                                placeholder = { Text("upi://pay?pa=... or merchant@bank", fontSize = 11.sp, color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = AxioTealPrimary,
                                    unfocusedBorderColor = Color.Gray
                                ),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    val parsed = UpiIntentHelper.parseUpiUri(manualQrInput)
                                    if (parsed != null) {
                                        onQrScanned(parsed)
                                    } else {
                                        Toast.makeText(context, "Invalid UPI QR format", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AxioTealPrimary),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Text("Scan", fontSize = 12.sp)
                            }
                        }
                    }

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sampleQrs) { sample ->
                            ElevatedCard(
                                onClick = {
                                    val parsed = UpiIntentHelper.parseUpiUri(sample.qrUri)
                                    if (parsed != null) {
                                        onQrScanned(parsed)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1E293B))
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = sample.title,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "₹${sample.amount.toInt()} • ${sample.merchantName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF2DD4BF)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}
