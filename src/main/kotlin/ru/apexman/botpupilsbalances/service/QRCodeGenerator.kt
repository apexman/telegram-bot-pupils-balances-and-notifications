package ru.apexman.botpupilsbalances.service

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@Service
class QRCodeGenerator {

    fun generate(text: String): ByteArray {
        val barcodeWriter = QRCodeWriter()
        val bitMatrix: BitMatrix = barcodeWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200)
        val image = MatrixToImageWriter.toBufferedImage(bitMatrix)
        return toByteArray(image)
    }

    private fun toByteArray(image: BufferedImage): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        ImageIO.write(image, "png", byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

}