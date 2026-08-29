package com.example.ui.dialogs

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CountryCurrencyCatalog
import com.example.domain.ParsedReceipt
import com.example.domain.ParsedReceiptItem
import com.example.domain.ReceiptOcrEngine
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.ExpenseRose
import com.example.ui.theme.LocalCustomColors
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScannerBottomSheet(
    currencySymbol: String = "$",
    onDismiss: () -> Unit,
    onApplyToTransaction: (Double, String, Long) -> Unit,
    onApplyToSplit: (Double, String, List<ParsedReceiptItem>) -> Unit
) {
    val context = LocalContext.current
    val customColors = LocalCustomColors.current

    var scannedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var ocrStatusMessage by remember { mutableStateOf<String?>(null) }

    // Extracted receipt state
    var merchantName by remember { mutableStateOf("") }
    var totalAmountText by remember { mutableStateOf("") }
    var detectedDateEpoch by remember { mutableStateOf(System.currentTimeMillis()) }
    var taxAmountText by remember { mutableStateOf("") }
    var tipAmountText by remember { mutableStateOf("") }
    var itemsList by remember { mutableStateOf(mutableListOf<ParsedReceiptItem>()) }
    var rawExtractedText by remember { mutableStateOf("") }

    var isManualTextMode by remember { mutableStateOf(false) }
    var manualTextInput by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "scan_laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    fun processImageBitmap(bitmap: Bitmap) {
        scannedBitmap = bitmap
        isProcessing = true
        ocrStatusMessage = "Analyzing receipt text with OCR engine..."

        try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text
                    rawExtractedText = text
                    if (text.isBlank()) {
                        ocrStatusMessage = "No clear text detected. Try capturing with better lighting."
                        isProcessing = false
                        return@addOnSuccessListener
                    }

                    val parsed = ReceiptOcrEngine.parseReceiptText(text)
                    merchantName = parsed.merchantName
                    totalAmountText = if (parsed.totalAmount > 0) String.format(Locale.US, "%.2f", parsed.totalAmount) else ""
                    detectedDateEpoch = parsed.dateEpoch
                    taxAmountText = if (parsed.taxAmount > 0) String.format(Locale.US, "%.2f", parsed.taxAmount) else ""
                    tipAmountText = if (parsed.tipAmount > 0) String.format(Locale.US, "%.2f", parsed.tipAmount) else ""
                    itemsList = parsed.items.toMutableList()
                    isProcessing = false
                    ocrStatusMessage = "Extracted ${parsed.items.size} line items successfully!"
                }
                .addOnFailureListener { e ->
                    isProcessing = false
                    ocrStatusMessage = "OCR scan failed: ${e.localizedMessage ?: "Unknown error"}"
                    Toast.makeText(context, "OCR failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            isProcessing = false
            ocrStatusMessage = "Error processing image: ${e.localizedMessage}"
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            processImageBitmap(bitmap)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                }
                processImageBitmap(bitmap)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not load image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = customColors.bentoCardBg,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outline) }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Live Receipt OCR Scanner",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Scan receipts, grocery bills, and invoices instantly",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(EmeraldAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = null,
                            tint = EmeraldAccent
                        )
                    }
                }
            }

            // Capture Actions: Camera Snap / Gallery / Text Paste
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { cameraLauncher.launch(null) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Take Photo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upload Image", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // Scanning Viewfinder / Preview if an image is loaded
            if (scannedBitmap != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldAccent.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                bitmap = scannedBitmap!!.asImageBitmap(),
                                contentDescription = "Scanned Receipt",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Overlay laser scanning animation when processing
                            if (isProcessing) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.4f))
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .align(Alignment.TopCenter)
                                        .offset(y = (laserOffset * 190).dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color.Transparent, EmeraldAccent, Color.White, EmeraldAccent, Color.Transparent)
                                            )
                                        )
                                )

                                Column(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(color = EmeraldAccent, modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Recognizing receipt text...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // OCR Status Message
            if (ocrStatusMessage != null && !isProcessing) {
                item {
                    Text(
                        text = ocrStatusMessage!!,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (ocrStatusMessage!!.startsWith("Extracted")) EmeraldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Manual Text Paste Toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { isManualTextMode = !isManualTextMode }) {
                        Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isManualTextMode) "Hide Raw Text" else "Paste Text or View OCR Stream", fontSize = 12.sp)
                    }
                }

                if (isManualTextMode) {
                    OutlinedTextField(
                        value = if (manualTextInput.isNotBlank()) manualTextInput else rawExtractedText,
                        onValueChange = {
                            manualTextInput = it
                            val parsed = ReceiptOcrEngine.parseReceiptText(it)
                            merchantName = parsed.merchantName
                            totalAmountText = if (parsed.totalAmount > 0) String.format(Locale.US, "%.2f", parsed.totalAmount) else ""
                            detectedDateEpoch = parsed.dateEpoch
                            taxAmountText = if (parsed.taxAmount > 0) String.format(Locale.US, "%.2f", parsed.taxAmount) else ""
                            itemsList = parsed.items.toMutableList()
                        },
                        label = { Text("Raw OCR / Receipt Text") },
                        placeholder = { Text("Paste receipt text lines with prices...") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    )
                }
            }

            // Parsed Receipt Details Card (Live Editable)
            item {
                Text(
                    text = "EXTRACTED RECEIPT DETAILS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Merchant Name
                        OutlinedTextField(
                            value = merchantName,
                            onValueChange = { merchantName = it },
                            label = { Text("Merchant / Store Name") },
                            placeholder = { Text("e.g. Grocery Store, Restaurant") },
                            leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = EmeraldAccent) },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Total Amount & Tax
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = totalAmountText,
                                onValueChange = { totalAmountText = it },
                                label = { Text("Total Amount ($currencySymbol)") },
                                placeholder = { Text("0.00") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                leadingIcon = { Text(currencySymbol, fontWeight = FontWeight.Bold, color = EmeraldAccent) },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = taxAmountText,
                                onValueChange = { taxAmountText = it },
                                label = { Text("Tax ($currencySymbol)") },
                                placeholder = { Text("0.00") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                modifier = Modifier.weight(0.8f)
                            )
                        }

                        // Date
                        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Date: ${sdf.format(Date(detectedDateEpoch))}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Line items section
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Itemized Items (${itemsList.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            TextButton(
                                onClick = {
                                    itemsList = (itemsList + ParsedReceiptItem("Item ${itemsList.size + 1}", 0.0, 1)).toMutableList()
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Add Item", fontSize = 11.sp)
                            }
                        }

                        if (itemsList.isEmpty()) {
                            Text(
                                text = "No line items detected yet. Take a photo of the receipt or add items manually.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            itemsList.forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = item.name,
                                        onValueChange = { newName ->
                                            val updated = itemsList.toMutableList()
                                            updated[index] = item.copy(name = newName)
                                            itemsList = updated
                                        },
                                        placeholder = { Text("Item Name") },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1.5f),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = if (item.price > 0) item.price.toString() else "",
                                        onValueChange = { newPrice ->
                                            val priceVal = newPrice.toDoubleOrNull() ?: 0.0
                                            val updated = itemsList.toMutableList()
                                            updated[index] = item.copy(price = priceVal)
                                            itemsList = updated
                                            // Recalculate total if needed
                                            val sum = updated.sumOf { it.price }
                                            if (sum > 0) totalAmountText = String.format(Locale.US, "%.2f", sum)
                                        },
                                        placeholder = { Text("0.00") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )

                                    IconButton(
                                        onClick = {
                                            val updated = itemsList.toMutableList()
                                            updated.removeAt(index)
                                            itemsList = updated
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = ExpenseRose, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Action Buttons
            item {
                val totalParsed = totalAmountText.toDoubleOrNull() ?: 0.0
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            val finalMerchant = merchantName.ifBlank { "Receipt Expense" }
                            onApplyToTransaction(totalParsed, finalMerchant, detectedDateEpoch)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                        enabled = totalParsed > 0
                    ) {
                        Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save as Expense (${CountryCurrencyCatalog.formatMoney(totalParsed, currencySymbol)})",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val finalMerchant = merchantName.ifBlank { "Split Receipt" }
                            onApplyToSplit(totalParsed, finalMerchant, itemsList)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF6366F1)),
                        enabled = totalParsed > 0
                    ) {
                        Icon(imageVector = Icons.Default.GroupAdd, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Split with Friends", fontWeight = FontWeight.Bold, color = Color(0xFF6366F1))
                    }
                }
            }
        }
    }
}
