package com.prajwalprince94.expensetracker.data.receipt

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

/**
 * On-device ML Kit OCR Recognition engine for receipts and invoices.
 * Processes camera images or gallery photos completely offline and pipes recognized
 * text into the intelligent ReceiptTextParser.
 */
object ReceiptOcrEngine {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    /**
     * Asynchronously scans a Bitmap photo using on-device ML Kit Text Recognition.
     */
    fun scanBitmap(
        bitmap: Bitmap,
        onSuccess: (ParsedReceipt) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val parsed = ReceiptTextParser.parse(visionText.text)
                    onSuccess(parsed)
                }
                .addOnFailureListener { exception ->
                    onError(exception)
                }
        } catch (e: Exception) {
            onError(e)
        }
    }

    /**
     * Asynchronously scans an image Uri from Android storage or camera output.
     */
    fun scanImageUri(
        context: Context,
        uri: Uri,
        onSuccess: (ParsedReceipt) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val image = InputImage.fromFilePath(context, uri)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val parsed = ReceiptTextParser.parse(visionText.text)
                    onSuccess(parsed)
                }
                .addOnFailureListener { exception ->
                    onError(exception)
                }
        } catch (e: Exception) {
            onError(e)
        }
    }

    /**
     * Synchronously parses raw text from a preset or clipboard.
     */
    fun parseRawText(rawText: String): ParsedReceipt {
        return ReceiptTextParser.parse(rawText)
    }
}
