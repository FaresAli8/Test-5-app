package com.example.smartqr.utils

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

class QrAnalyzer(
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        val map = mapOf(
            DecodeHintType.POSSIBLE_FORMATS to arrayListOf(BarcodeFormat.QR_CODE)
        )
        setHints(map)
    }

    override fun analyze(image: ImageProxy) {
        val buffer = image.planes[0].buffer
        val data = toByteArray(buffer)
        val width = image.width
        val height = image.height

        // CameraX produces YUV, specifically NV21 or YUV_420_888
        // For simple scanning, the Y plane is enough (grayscale)
        val source = PlanarYUVLuminanceSource(
            data, width, height, 0, 0, width, height, false
        )
        
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

        try {
            val result = reader.decode(binaryBitmap)
            onQrCodeScanned(result.text)
        } catch (e: NotFoundException) {
            // No QR found, normal
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            image.close()
        }
    }

    private fun toByteArray(buffer: ByteBuffer): ByteArray {
        buffer.rewind()
        val data = ByteArray(buffer.remaining())
        buffer.get(data)
        return data
    }
}